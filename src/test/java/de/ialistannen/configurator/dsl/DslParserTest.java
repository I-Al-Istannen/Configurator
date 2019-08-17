package de.ialistannen.configurator.dsl;

import static org.assertj.core.api.Assertions.assertThat;

import de.ialistannen.configurator.context.Action;
import de.ialistannen.configurator.dsl.comparison.ComparisonAstNode;
import de.ialistannen.configurator.dsl.script.JavaScriptScript;
import de.ialistannen.configurator.util.ParseException;
import de.ialistannen.configurator.util.StringReader;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DslParserTest {

  @ParameterizedTest(name = "{0} should parse to {1}")
  @CsvSource({
      "{{$hey}}, hey",
      "{{$hey you}}, hey you",
      "{{$hey_you}}, hey_you",
      "{{$1234}}, 1234",
  })
  public void parseVariable(String input, String expectedName) throws ParseException {
    assertThat(getParsedResult(input)).isEqualTo(
        wrapInBlock(new VariableAstNode(expectedName, Collections.emptyList()))
    );
  }

  @ParameterizedTest(name = "{0} should parse to {1}")
  @CsvSource({
      "{$hey}}, {$hey}}",
      "{hey you}}, {hey you}}",
      "hey_you}}, hey_you}}",
      "1234, 1234",
      "{!{hey_you}}, {!{hey_you}}",
      "{{hey_you}}, {{hey_you}}",
  })
  public void parseLiteral(String input, String expected) throws ParseException {
    if (expected.contains("{{")) {
      assertThat(getParsedResult(input)).isEqualTo(wrapInBlock(
          wrapInBlock(new LiteralAstNode("{{"), new LiteralAstNode(expected.substring(2)))
      ));
      return;
    }
    assertThat(getParsedResult(input)).isEqualTo(
        wrapInBlock(new LiteralAstNode(expected))
    );
  }

  @ParameterizedTest(name = "{0} should parse to {1}")
  @CsvSource({
      "{{!echo}}, echo",
      "{{!echo you}}, echo you",
      "{{!echo 'Hey'}}, 'echo ''Hey'''",
      "{{!echo \"Hey\"}}, echo \"Hey\"",
      "{{!echo \"Hey you\"}}, echo \"Hey you\"",
      "{{!echo 'Hey you'}}, 'echo ''Hey you'''",
      "{{!echo Hey you}}, echo Hey you",
      "{{!echo Hey y'ou}}, echo Hey y'ou", // not what a shell would do, but might be nicer
  })
  public void parseShellCommand(String input, String expected) throws ParseException {
    assertThat(getParsedResult(input)).isEqualTo(
        wrapInBlock(new ShellCommandAstNode(expected))
    );
  }

  @ParameterizedTest(name = "{0} should assign {1} to {2}")
  @CsvSource({
      "'# a = wow', a, wow",
      "'# a = ''wow''', a, '''wow'''",
      "'# a = 20', a, 20",
      "'#  a = 20', a, 20",
  })
  public void parseAssignmentToLiteral(String input, String expectedName, String literalValue)
      throws ParseException {
    assertThat(getParsedResult(input)).isEqualTo(
        wrapInBlock(new AssignmentAstNode(expectedName, new LiteralAstNode(literalValue)))
    );
  }

  @ParameterizedTest(name = "{0} should assign {1} to {2}")
  @CsvSource({
      "'# a = {{!wow}}', a, wow",
      "'# a = {{!''wow''}}', a, '''wow'''",
      "'# a = {{!wow you there}}', a, wow you there",
      "'# a = {{!''wow you there''}}', a, '''wow you there'''",
      "'# a = {{!20}}', a, 20",
      "'#  a = {{!20}}', a, 20",
  })
  public void parseAssignmentToShell(String input, String expectedName, String command)
      throws ParseException {
    assertThat(getParsedResult(input)).isEqualTo(wrapInBlock(new AssignmentAstNode(
        expectedName, new ShellCommandAstNode(command)
    )));
  }

  @ParameterizedTest(name = "{0} should assign {1} to {2}")
  @CsvSource({
      "'# a = {{$wow}}', a, wow",
      "'# a = {{$''wow''}}', a, '''wow'''",
      "'# a = {{$20}}', a, 20",
  })
  public void parseAssignmentToVariable(String input, String expectedName, String varName)
      throws ParseException {
    assertThat(getParsedResult(input)).isEqualTo(
        wrapInBlock(new AssignmentAstNode(
            expectedName,
            new VariableAstNode(varName, Collections.emptyList()))
        )
    );
  }

  @Test
  public void parseAction() throws ParseException {
    String name = "This is mine!";
    String content = "hello world\n"
        + "'hey'\n";
    String input = "# action " + name + "\n"
        + content
        + "# end action";

    AstNode expectedContent = wrapInBlock(
        new LiteralAstNode(content.split("\\n")[0] + "\n"),
        new LiteralAstNode(content.split("\\n")[1] + "\n")
    );
    assertThat(getParsedResult(input)).isEqualTo(
        wrapInBlock(new ActionAstNode(new Action(name, expectedContent)))
    );
  }

  @Test
  public void parseJsScript() throws ParseException {
    String content = "hello world\n"
        + "'hey'\n";
    String input = "# script js\n"
        + content
        + "# end script";
    assertThat(getParsedResult(input)).isEqualTo(
        wrapInBlock(new ScriptAstNode(new JavaScriptScript(content)))
    );
  }

  @Test
  public void handleEmptyLineInAssignment() throws ParseException {
    String input = "\n# user = test";
    AstNode result = getParsedResult(input);
    assertThat(result).isEqualTo(wrapInBlock(
        new LiteralAstNode("\n"),
        new AssignmentAstNode("user", new LiteralAstNode("test"))
    ));
  }

  @Test
  public void handleCommandWithTrailingSpaces() throws ParseException {
    String spaces = "    ";
    String input = spaces + "# user = test";

    AstNode result = getParsedResult(input);

    assertThat(result).isEqualTo(wrapInBlock(wrapInBlock(
        new LiteralAstNode(spaces),
        new AssignmentAstNode("user", new LiteralAstNode("test"))
    )));
  }

  @Test
  public void parseIf() throws ParseException {
    String input = "# if (hey) == (you)\n"
        + "hey\n"
        + "# end if";
    AstNode result = getParsedResult(input);
    assertThat(result).isEqualTo(wrapInBlock(
        new IfAstNode(
            new ComparisonAstNode(new LiteralAstNode("hey"), new LiteralAstNode("you"), null),
            wrapInBlock(new LiteralAstNode("hey"))
        )
    ));
  }

  @ParameterizedTest(name = "\"{0}\" should parse to \"{1}\" with args \"{2}\"")
  @CsvSource({
      "'# call (Test) ()', Test, ",
      "'# call (Test) (a)', Test, a",
      "'# call (Test) (a b c)', Test, a b c",
      "'# call (Test me) (a b c)', Test me, a b c",
      "'# call (Test me) (''a b c'')', Test me, '''a b c'''",
  })
  public void parseCallAction(String text, String name, String argString) throws ParseException {
    AstNode result = getParsedResult(text);
    assertThat(result).isEqualTo(wrapInBlock(
        new ActionCallAstNode(name, argString == null ? "" : argString)
    ));
  }

  private AstNode getParsedResult(String input) throws ParseException {
    return new DslParser(new StringReader("Command prefix: #\n" + input)).parse();
  }

  private AstNode wrapInBlock(AstNode... other) {
    return new BlockAstNode(Arrays.asList(other));
  }
}
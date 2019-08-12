package de.ialistannen.configurator.dsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
      "{{hey_you}}, null",
  })
  public void parseVariable(String input, String expectedName) throws ParseException {
    if (expectedName.equals("null")) {
      assertThatExceptionOfType(ParseException.class)
          .isThrownBy(() -> getParsedResult(input));
      return;
    }
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
  })
  public void parseLiteral(String input, String expected) throws ParseException {
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
  public void handleEmptyLineInAssignment() throws ParseException {
    String input = "\n# user = test";
    AstNode result = getParsedResult(input);
    assertThat(result).isEqualTo(wrapInBlock(
        new LiteralAstNode("\n"),
        new AssignmentAstNode("user", new LiteralAstNode("test"))
    ));
  }

  private AstNode getParsedResult(String input) throws ParseException {
    return new DslParser(new StringReader("Command prefix: #\n" + input)).parse();
  }

  private AstNode wrapInBlock(AstNode... other) {
    return new BlockAstNode(Arrays.asList(other));
  }
}
package de.ialistannen.configurator.dsl;

import static de.ialistannen.configurator.output.ColoredOutput.colorErr;

import de.ialistannen.configurator.context.Action;
import de.ialistannen.configurator.dsl.comparison.ComparisonAstNode;
import de.ialistannen.configurator.dsl.script.JavaScriptScript;
import de.ialistannen.configurator.output.TerminalColor;
import de.ialistannen.configurator.util.ParseException;
import de.ialistannen.configurator.util.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * A parser for the configurator DSL.
 */
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class DslParser {

  private final StringReader input;
  private String commandPrefix;
  private final boolean reportErrors;

  /**
   * Creates a new parser for the given input.
   *
   * @param input the input
   * @param reportErrors whether to report parse errors
   */
  public DslParser(StringReader input, boolean reportErrors) {
    this.input = Objects.requireNonNull(input, "input can not be null!");
    this.reportErrors = reportErrors;
  }

  /**
   * Parses the source to an {@link AstNode}.
   *
   * @return the parsed ast node
   * @throws ParseException if the input is invalid
   */
  public AstNode parse() throws ParseException {
    return parse(true);
  }

  /**
   * Parses the source to an {@link AstNode}.
   *
   * @param withNewline whether to include a trailing newline
   * @return the parsed ast node
   * @throws ParseException if the input is invalid
   */
  private AstNode parse(boolean withNewline) throws ParseException {
    if (commandPrefix == null) {
      parseCommandPrefix();
    }

    List<AstNode> children = new ArrayList<>();

    while (input.canRead()) {
      children.add(parseSection(withNewline));
    }

    return new BlockAstNode(children);
  }

  private void parseCommandPrefix() throws ParseException {
    input.assertRead("Command prefix");
    input.readWhile(Character::isWhitespace);
    input.assertRead(": ");

    commandPrefix = input.readLine().replace(System.lineSeparator(), "");
    if (commandPrefix.isEmpty()) {
      throw new ParseException(input, "Expected the command prefix");
    }
  }

  /**
   * Parses a line or block, depending on the next chars.
   *
   * @param withNewline whether to include a trailing newline
   * @return the parse result
   */
  private AstNode parseSection(boolean withNewline) throws ParseException {
    Optional<AstNode> command = tryParseCommand();
    if (command.isPresent()) {
      return command.get();
    }

    String line;
    if (withNewline) {
      line = input.readLineIncludingNewline();
    } else {
      line = input.readLine();
    }
    StringReader reader = new StringReader(line);

    List<AstNode> children = new ArrayList<>();

    while (reader.canRead()) {
      readLiteral(reader).ifPresent(children::add);

      if (reader.peek(2).equals("{{")) {
        reader.assertRead("{{");
        Optional<AstNode> innerResult = tryParse(() -> readInnerExpression(reader), reader);
        if (innerResult.isPresent()) {
          children.add(innerResult.get());
          reader.assertRead("}}");
        } else {
          children.add(new LiteralAstNode("{{"));
        }
      }
    }

    if (children.size() == 1) {
      return children.get(0);
    }

    return new BlockAstNode(children);
  }

  private Optional<AstNode> tryParseCommand() {
    int start = input.getPosition();
    String whitespace = input.readWhile(it -> it == ' ' || it == '\t');

    if (input.peek(commandPrefix.length()).equals(commandPrefix)) {
      return tryParse(this::parseCommand, input).map(it -> {
        if (whitespace.isEmpty()) {
          return it;
        }
        return new BlockAstNode(Arrays.asList(new LiteralAstNode(whitespace), it));
      });
    } else {
      input.reset(start);
    }
    return Optional.empty();
  }

  private AstNode parseCommand() throws ParseException {
    input.assertRead(commandPrefix);

    input.readWhile(Character::isWhitespace);
    String firstWord = input.peekWhile(it -> !Character.isWhitespace(it));
    switch (firstWord) {
      case "action":
        return readAction("action", false);
      case "action*":
        return readAction("action*", true);
      case "script":
        return readScript();
      case "if":
        return readIf();
      case "call":
        input.assertRead("call");
        return readCall(input);
      case "execute":
        return readExecute();
      case "reload":
        return readReload();
      default:
        return readAssignment();
    }
  }

  private AstNode readReload() throws ParseException {
    return readNamedEnclosed(
        "reload",
        (name, content) -> {
          AstNode inner = new DslParser(
              new StringReader(content), commandPrefix, reportErrors
          ).parse();
          return new ReloadActionAstNode(new Action(name, inner, false));
        }
    );
  }

  private AstNode readExecute() throws ParseException {
    return readNamedEnclosed(
        "execute",
        (ignored, content) -> {
          AstNode script = new DslParser(
              new StringReader(content), commandPrefix, reportErrors
          ).parse();
          return new ExecuteFileAstNode(script);
        }
    );
  }

  private AstNode readCall(StringReader input) throws ParseException {
    input.readWhile(Character::isWhitespace);
    String actionName = input.readEnclosedByParentheses();
    input.readWhile(Character::isWhitespace);
    return new ActionCallAstNode(actionName, input.readEnclosedByParentheses());
  }

  private AstNode readIf() throws ParseException {
    return readNamedEnclosed(
        "if",
        (condition, content) -> {
          AstNode contentNode = new DslParser(
              new StringReader(content), commandPrefix, reportErrors
          )
              .parse(false);
          ComparisonAstNode comparison = new DslParser(
              new StringReader(condition), commandPrefix, reportErrors
          ).readComparison();
          return new IfAstNode(comparison, contentNode);
        }
    );
  }

  private ComparisonAstNode readComparison() throws ParseException {
    AstNode left = parseSectionOnString(input.readEnclosedByParentheses(), false);

    input.readWhile(Character::isWhitespace);
    String operator = input.readWhile(it -> !Character.isWhitespace(it));
    input.readWhile(Character::isWhitespace);

    BiFunction<String, String, Boolean> comparison;
    switch (operator) {
      case "==":
        comparison = String::equalsIgnoreCase;
        break;
      case "===":
        comparison = String::equals;
        break;
      case "~=":
        comparison = String::matches;
        break;
      case "!=":
        comparison = (a, b) -> !a.equals(b);
        break;
      case ">":
        comparison = (a, b) -> Integer.parseInt(a) > Integer.parseInt(b);
        break;
      case "<":
        comparison = (a, b) -> Integer.parseInt(a) < Integer.parseInt(b);
        break;
      case "||":
        comparison = (a, b) -> Boolean.parseBoolean(a) || Boolean.parseBoolean(b);
        break;
      case "&&":
        comparison = (a, b) -> Boolean.parseBoolean(a) && Boolean.parseBoolean(b);
        break;
      default:
        throw new ParseException(input, "Unknown comparison");
    }
    AstNode right = parseSectionOnString(input.readEnclosedByParentheses(), false);

    return new ComparisonAstNode(left, right, comparison);
  }

  private AstNode parseSectionOnString(String input, boolean newline) throws ParseException {
    return new DslParser(new StringReader(input), commandPrefix, reportErrors)
        .parseSection(newline);
  }

  private AstNode readScript() throws ParseException {
    return readNamedEnclosed(
        "script",
        (lang, content) -> {
          if (!lang.equals("js")) {
            throw new ParseException(input, "Unknown language");
          }
          return new ScriptAstNode(new JavaScriptScript(content));
        }
    );
  }

  private AstNode readNamedEnclosed(String start, NamedSectionParser creator)
      throws ParseException {
    return readNamedEnclosed(start, creator, input);
  }

  private AstNode readNamedEnclosed(String start, NamedSectionParser creator, StringReader input)
      throws ParseException {
    input.assertRead(start);
    input.readWhile(it -> it == ' ' || it == '\t');
    final String END_MARKER = commandPrefix + " end " + start;

    String name = input.readLine();
    String content = input.readUntil(END_MARKER);

    input.assertRead(END_MARKER);
    if (input.peek(System.lineSeparator().length()).equals(System.lineSeparator())) {
      input.assertRead(System.lineSeparator());
    }

    return creator.create(name, content);
  }

  private AstNode readAction(String keyword, boolean hideFromRunAll) throws ParseException {
    return readNamedEnclosed(keyword,
        (name, content) -> {
          AstNode inner = new DslParser(
              new StringReader(content), commandPrefix, reportErrors
          ).parse();
          return new ActionAstNode(new Action(name, inner, hideFromRunAll));
        }
    );
  }

  private AstNode readAssignment() throws ParseException {
    input.readWhile(Character::isWhitespace);
    String name = input.readWhile(it -> !Character.isWhitespace(it));
    input.readWhile(Character::isWhitespace);

    input.assertRead("= ");
    AstNode value = parseSection(false);

    return new AssignmentAstNode(name, value);
  }

  private Optional<AstNode> readLiteral(StringReader input) {
    String readText = input.readUntil("{{");
    if (readText.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new LiteralAstNode(readText));
  }

  private AstNode readInnerExpression(StringReader input) throws ParseException {
    AstNode result;

    switch (input.readChar()) {
      case '$':
        result = readVariable(input);
        break;
      case '!':
        result = readShellCommand(input);
        break;
      case '|':
        result = readCall(input);
        break;
      default:
        throw new ParseException(input, "Unknown command");
    }

    return result;
  }

  private AstNode readShellCommand(StringReader input) {
    return new ShellCommandAstNode(input.readUntil("}}"));
  }

  private AstNode readVariable(StringReader input) {
    String innerContent = input.readUntil("}}");

    String[] parts = innerContent.split(",");
    List<String> extraArgs = Arrays.asList(parts).subList(1, parts.length);

    return new VariableAstNode(parts[0], extraArgs);
  }

  private Optional<AstNode> tryParse(ParsingSupplier supplier, StringReader reader) {
    int start = reader.getPosition();
    try {
      return Optional.ofNullable(supplier.parse());
    } catch (ParseException e) {
      if (reportErrors) {
        colorErr(TerminalColor.RED + "Parse error: " + e.getMessage());
      }
      reader.reset(start);
      return Optional.empty();
    }
  }

  private interface ParsingSupplier {

    AstNode parse() throws ParseException;
  }

  private interface NamedSectionParser {

    AstNode create(String name, String content) throws ParseException;
  }
}

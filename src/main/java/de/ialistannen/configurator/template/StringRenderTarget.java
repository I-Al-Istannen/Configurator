package de.ialistannen.configurator.template;

import de.ialistannen.configurator.context.Action;
import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.context.RenderedAction;
import de.ialistannen.configurator.dsl.ActionAstNode;
import de.ialistannen.configurator.dsl.ActionCallAstNode;
import de.ialistannen.configurator.dsl.AssignmentAstNode;
import de.ialistannen.configurator.dsl.AstNode;
import de.ialistannen.configurator.dsl.AstVisitor;
import de.ialistannen.configurator.dsl.BlockAstNode;
import de.ialistannen.configurator.dsl.DslParser;
import de.ialistannen.configurator.dsl.ExecuteFileAstNode;
import de.ialistannen.configurator.dsl.IfAstNode;
import de.ialistannen.configurator.dsl.LiteralAstNode;
import de.ialistannen.configurator.dsl.ReloadActionAstNode;
import de.ialistannen.configurator.dsl.ScriptAstNode;
import de.ialistannen.configurator.dsl.ShellCommandAstNode;
import de.ialistannen.configurator.dsl.VariableAstNode;
import de.ialistannen.configurator.dsl.comparison.ComparisonAstNode;
import de.ialistannen.configurator.rendering.RenderTarget;
import de.ialistannen.configurator.rendering.StringRenderedObject;
import de.ialistannen.configurator.util.Pair;
import de.ialistannen.configurator.util.ParseException;
import de.ialistannen.configurator.util.ProcessUtils;
import de.ialistannen.configurator.util.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Renders a string to an ast.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class StringRenderTarget implements RenderTarget<StringRenderedObject> {

  private final AstNode ast;

  /**
   * A render target that renders a given String in the common DSL format.
   *
   * @param source the source string
   * @param reportParseErrors whether to report parse errors
   * @throws ParseException if the string contains errors
   */
  public StringRenderTarget(String source, boolean reportParseErrors) throws ParseException {
    ast = new DslParser(new StringReader(source), reportParseErrors).parse();
  }

  /**
   * A render target that renders a given String in the common DSL format.
   *
   * @param source the source string
   * @param commandPrefix the command prefix
   * @param reportParseErrors whether to report parse errors
   * @throws ParseException if the string contains errors
   */
  private StringRenderTarget(String source, String commandPrefix, boolean reportParseErrors)
      throws ParseException {
    ast = new DslParser(new StringReader(source), commandPrefix, reportParseErrors).parse();
  }

  @Override
  public Pair<StringRenderedObject, RenderContext> render(RenderContext context) {
    RenderVisitor visitor = new RenderVisitor(context);
    StringRenderedObject result = new StringRenderedObject(ast.accept(visitor));
    return new Pair<>(result, visitor.getContext());
  }

  /**
   * Creates a string render target that handles a single line.
   *
   * @param line the line
   * @param reportParseErrors whether to report parse errors
   * @return the render target
   * @throws ParseException if an error occurs
   */
  public static StringRenderTarget singleLine(String line, boolean reportParseErrors)
      throws ParseException {
    return new StringRenderTarget(line, "#", reportParseErrors);
  }

  /**
   * Renders the target ast.
   *
   * @param ast the ast
   * @return the rendered output
   */
  public static StringRenderTarget fromAst(AstNode ast) {
    return new StringRenderTarget(ast);
  }

  /**
   * Renders the ast to a string, using an initial context.
   */
  private static class RenderVisitor implements AstVisitor<String> {

    private RenderContext context;

    RenderVisitor(RenderContext context) {
      this.context = context;
    }

    RenderContext getContext() {
      return context;
    }

    @Override
    public String visitLiteral(LiteralAstNode node) {
      return node.getText();
    }

    @Override
    public String visitVariable(VariableAstNode node) {
      String value = context.<String>getValueOpt(node.getName())
          .orElseThrow(
              () -> new NoSuchElementException("Unknown variable: '" + node.getName() + "'")
          );
      if (node.getExtraArguments().contains("rgb")) {
        String withoutPound = value.replace("#", "");
        int colorInt = Integer.parseInt(withoutPound, 16);
        int red = (colorInt & 0xFF0000) >> 16;
        int green = (colorInt & 0x00FF00) >> 8;
        int blue = colorInt & 0x0000FF;
        return red + ", " + green + ", " + blue;
      }
      return value;
    }

    @Override
    public String visitShellCommand(ShellCommandAstNode node) {
      return ProcessUtils.runWithShellForOutput(node.getCommand());
    }

    @Override
    public String visitBlock(BlockAstNode node) {
      return node.getChildren().stream()
          .map(it -> it.accept(this))
          .collect(Collectors.joining());
    }

    @Override
    public String visitAssignment(AssignmentAstNode node) {
      context = context.storeValue(node.getName(), node.getValue().accept(this));
      return "";
    }

    @Override
    public String visitAction(ActionAstNode node) {
      Action action = node.getAction();
      String content = action.getContent().accept(this);

      context = context.storeAction(new RenderedAction(
          action.getName(), action.getSanitizedName(), content, action.isHideFromRunAll()
      ));
      return "";
    }

    @Override
    public String visitScript(ScriptAstNode node) {
      context = node.getScript().execute(context);
      return "";
    }

    @Override
    public String visitComparisonAstNode(ComparisonAstNode node) {
      String left = node.getLeft().accept(this);
      String right = node.getRight().accept(this);
      return node.getComparisonFunction().apply(left, right).toString();
    }

    @Override
    public String visitIfAstNode(IfAstNode node) {
      ComparisonAstNode condition = node.getCondition();
      String result = condition.accept(this);
      if (result.equals("true")) {
        return node.getIfContent().accept(this);
      } else if (node.getElseContent() != null) {
        return node.getElseContent().accept(this);
      }
      return "";
    }

    @Override
    public String visitActionCall(ActionCallAstNode node) {
      String name = new Action(node.getName(), new LiteralAstNode(""), false).getSanitizedName();
      Path actionsDir = Paths.get(context.<String>getValue("actions_dir"));
      Path actionFile = actionsDir.resolve(name);
      return actionFile.toAbsolutePath().toString() + " " + node.getArgumentString();
    }

    @Override
    public String visitExecuteFile(ExecuteFileAstNode node) {
      String file = node.getContent().accept(this);
      context = context.storePostScript(file);
      return file;
    }

    @Override
    public String visitReloadAction(ReloadActionAstNode node) {
      Action action = node.getAction();
      String content = action.getContent().accept(this);

      context = context.storeReloadAction(new RenderedAction(
          action.getName(), action.getSanitizedName(), content, action.isHideFromRunAll()
      ));
      return "";
    }
  }
}

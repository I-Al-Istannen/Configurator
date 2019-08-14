package de.ialistannen.configurator.template;

import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.dsl.ActionAstNode;
import de.ialistannen.configurator.dsl.AssignmentAstNode;
import de.ialistannen.configurator.dsl.AstNode;
import de.ialistannen.configurator.dsl.AstVisitor;
import de.ialistannen.configurator.dsl.BlockAstNode;
import de.ialistannen.configurator.dsl.DslParser;
import de.ialistannen.configurator.dsl.IfAstNode;
import de.ialistannen.configurator.dsl.LiteralAstNode;
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
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Renders a string to an ast.
 */
public class StringRenderTarget implements RenderTarget<StringRenderedObject> {

  private final AstNode ast;

  /**
   * A render target that renders a given String in the common DSL format.
   *
   * @param source the source string
   * @throws ParseException if the string contains errors
   */
  public StringRenderTarget(String source) throws ParseException {
    ast = new DslParser(new StringReader(source)).parse();
  }

  @Override
  public Pair<StringRenderedObject, RenderContext> render(RenderContext context) {
    RenderVisitor visitor = new RenderVisitor(context);
    StringRenderedObject result = new StringRenderedObject(ast.accept(visitor));
    return new Pair<>(result, visitor.getContext());
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
      return context.<String>getValueOpt(node.getName())
          .orElseThrow(
              () -> new NoSuchElementException("Unknown variable: '" + node.getName() + "'")
          );
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
      context = context.storeAction(node.getAction());
      return "";
    }

    @Override
    public String visitScript(ScriptAstNode node) {
      context = node.getScript().execute(context);
      return "";
    }

    @Override
    public String acceptComparisonAstNode(ComparisonAstNode node) {
      String left = node.getLeft().accept(this);
      String right = node.getRight().accept(this);
      return node.getComparisonFunction().apply(left, right).toString();
    }

    @Override
    public String acceptIfAstNode(IfAstNode node) {
      ComparisonAstNode condition = node.getCondition();
      String result = condition.accept(this);
      if (result.equals("true")) {
        return node.getContent().accept(this);
      }
      return "";
    }
  }
}

package de.ialistannen.configurator.dsl;

import de.ialistannen.configurator.dsl.comparison.ComparisonAstNode;

/**
 * An ast visitor that may traverse the whole tree.
 *
 * @param <T> the return type of the visitor
 */
public interface AstVisitor<T> {

  /**
   * Called for each literal.
   *
   * @param node the literal
   * @return the return value this visitor chose
   */
  T visitLiteral(LiteralAstNode node);

  /**
   * Called for each variable.
   *
   * @param node the variable
   * @return the return value this visitor chose
   */
  T visitVariable(VariableAstNode node);

  /**
   * Called for each shell command.
   *
   * @param node the command
   * @return the return value this visitor chose
   */
  T visitShellCommand(ShellCommandAstNode node);

  /**
   * Called for each block.
   *
   * @param node the block
   * @return the return value this visitor chose
   */
  T visitBlock(BlockAstNode node);

  /**
   * Called for each assignment.
   *
   * @param node the assignment
   * @return the return value this visitor chose
   */
  T visitAssignment(AssignmentAstNode node);

  /**
   * Called for each action.
   *
   * @param node the action
   * @return the return value this visitor chose
   */
  T visitAction(ActionAstNode node);

  /**
   * Called for each script.
   *
   * @param node the script
   * @return the return value this visitor chose
   */
  T visitScript(ScriptAstNode node);

  /**
   * Called for each comparison node.
   *
   * @param node the comparison node
   * @return the return value this visitor chose
   */
  T visitComparisonAstNode(ComparisonAstNode node);

  /**
   * Called for each if node.
   *
   * @param node the if node
   * @return the return value this visitor chose
   */
  T visitIfAstNode(IfAstNode node);

  /**
   * Called for each action call node.
   *
   * @param node the action call node
   * @return the return value this visitor chose
   */
  T visitActionCall(ActionCallAstNode node);

  /**
   * Called for each execute file node.
   *
   * @param node the execute file node
   * @return the return value this visitor chose
   */
  T visitExecuteFile(ExecuteFileAstNode node);
}

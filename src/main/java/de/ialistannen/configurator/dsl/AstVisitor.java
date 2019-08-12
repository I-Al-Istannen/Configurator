package de.ialistannen.configurator.dsl;

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
}

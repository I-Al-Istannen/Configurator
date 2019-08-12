package de.ialistannen.configurator.dsl;

import lombok.Data;

/**
 * Assigns a value to a variable, possibly declaring it.
 */
@Data
public class AssignmentAstNode implements AstNode {

  private String name;
  private AstNode value;

  /**
   * Creates a new assignment ast node.
   *
   * @param name the name of the variable
   * @param value the value of the variable
   */
  public AssignmentAstNode(String name, AstNode value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitAssignment(this);
  }
}

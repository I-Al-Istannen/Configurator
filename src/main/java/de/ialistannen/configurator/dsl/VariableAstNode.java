package de.ialistannen.configurator.dsl;

import java.util.List;
import lombok.Data;

/**
 * A variable ast node.
 */
@Data
public class VariableAstNode implements AstNode {

  private String name;
  private List<String> extraArguments;

  /**
   * Crreates a new variable ast node.
   *
   * @param name the name of the variable
   * @param extraArguments extra arguments for e.g. formatting
   */
  public VariableAstNode(String name, List<String> extraArguments) {
    this.name = name;
    this.extraArguments = extraArguments;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitVariable(this);
  }
}

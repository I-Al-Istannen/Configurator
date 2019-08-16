package de.ialistannen.configurator.dsl;

import lombok.Data;

/**
 * Calls an action.
 */
@Data
public class ActionCallAstNode implements AstNode {

  private final String name;
  private final String argumentString;

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitActionCall(this);
  }
}

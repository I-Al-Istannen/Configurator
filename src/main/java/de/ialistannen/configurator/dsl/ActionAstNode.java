package de.ialistannen.configurator.dsl;

import de.ialistannen.configurator.context.Action;
import lombok.Data;

/**
 * An {@link AstNode} that stores an {@link Action}.
 */
@Data
public class ActionAstNode implements AstNode {

  private final Action action;

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitAction(this);
  }
}

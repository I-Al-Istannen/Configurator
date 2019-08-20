package de.ialistannen.configurator.dsl;

import de.ialistannen.configurator.context.Action;
import lombok.Data;

/**
 * An ast node for a reload action
 */
@Data
public class ReloadActionAstNode implements AstNode {

  private final Action action;

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitReloadAction(this);
  }
}

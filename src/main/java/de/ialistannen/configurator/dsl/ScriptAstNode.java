package de.ialistannen.configurator.dsl;

import de.ialistannen.configurator.dsl.script.Script;
import lombok.Data;

/**
 * An {@link AstNode} that holds a {@link Script}.
 */
@Data
public class ScriptAstNode implements AstNode {

  private final Script script;

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitScript(this);
  }
}

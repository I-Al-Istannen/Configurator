package de.ialistannen.configurator.dsl;

public class ScriptAstNode implements AstNode {

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return null;
  }
}

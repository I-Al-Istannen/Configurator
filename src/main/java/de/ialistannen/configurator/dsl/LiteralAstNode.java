package de.ialistannen.configurator.dsl;

import lombok.Data;

/**
 * An ast that always evaluates to a fixed string.
 */
@Data
public class LiteralAstNode implements AstNode {

  private String text;

  public LiteralAstNode(String text) {
    this.text = text;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitLiteral(this);
  }
}

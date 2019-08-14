package de.ialistannen.configurator.dsl;

import de.ialistannen.configurator.dsl.comparison.ComparisonAstNode;
import lombok.Data;

/**
 * An ast node that represents an if.
 */
@Data
public class IfAstNode implements AstNode {

  private final ComparisonAstNode condition;
  private final AstNode content;

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.acceptIfAstNode(this);
  }
}

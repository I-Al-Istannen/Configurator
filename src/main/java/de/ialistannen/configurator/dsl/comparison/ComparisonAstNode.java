package de.ialistannen.configurator.dsl.comparison;

import de.ialistannen.configurator.dsl.AstNode;
import de.ialistannen.configurator.dsl.AstVisitor;
import java.util.function.BiFunction;
import lombok.Data;

/**
 * An ast node that represents a boolean comparison.
 */
@Data
public class ComparisonAstNode implements AstNode {

  private AstNode left;
  private AstNode right;
  private BiFunction<String, String, Boolean> comparisonFunction;

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.acceptComparisonAstNode(this);
  }
}

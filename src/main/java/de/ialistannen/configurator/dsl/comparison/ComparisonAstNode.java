package de.ialistannen.configurator.dsl.comparison;

import de.ialistannen.configurator.dsl.AstNode;
import de.ialistannen.configurator.dsl.AstVisitor;
import java.util.function.BiFunction;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * An ast node that represents a boolean comparison.
 *
 * <p><br>Note that the equals function of this node ignores the comparison function.</p>
 */
@Data
@EqualsAndHashCode(exclude = {"comparisonFunction"})
public class ComparisonAstNode implements AstNode {

  private final AstNode left;
  private final AstNode right;
  private final BiFunction<String, String, Boolean> comparisonFunction;

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitComparisonAstNode(this);
  }
}

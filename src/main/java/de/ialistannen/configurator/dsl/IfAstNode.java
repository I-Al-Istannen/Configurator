package de.ialistannen.configurator.dsl;

import de.ialistannen.configurator.dsl.comparison.ComparisonAstNode;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * An ast node that represents an if.
 */
@Data
@AllArgsConstructor
public class IfAstNode implements AstNode {

  private final ComparisonAstNode condition;
  private final AstNode ifContent;
  private final AstNode elseContent;

  /**
   * Creates a new if without any else.
   *
   * @param condition the condition
   * @param ifContent the if content
   */
  public IfAstNode(ComparisonAstNode condition, AstNode ifContent) {
    this(condition, ifContent, null);
  }

  /**
   * Returns the else content. Might be null.
   *
   * @return the else content or null
   */
  public AstNode getElseContent() {
    return elseContent;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitIfAstNode(this);
  }
}

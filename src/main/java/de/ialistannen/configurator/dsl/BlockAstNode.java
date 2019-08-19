package de.ialistannen.configurator.dsl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;

/**
 * A node that collects multiple other nodes together. A scope/block in other languages.
 */
@Data
public class BlockAstNode implements AstNode {

  private final List<AstNode> children;

  public BlockAstNode(List<AstNode> children) {
    this.children = new ArrayList<>(children);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitBlock(this);
  }

  /**
   * Returns all children.
   *
   * @return all children
   */
  public List<AstNode> getChildren() {
    return Collections.unmodifiableList(children);
  }

}

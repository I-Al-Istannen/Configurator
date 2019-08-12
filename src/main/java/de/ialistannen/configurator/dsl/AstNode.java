package de.ialistannen.configurator.dsl;

/**
 * The main type for the DSL..
 */
public interface AstNode {

  /**
   * Accepts a visitor.
   *
   * @param visitor the visitor to accept
   * @param <T> the type of the visitor
   * @return the result of accepting the visitor
   */
  <T> T accept(AstVisitor<T> visitor);
}

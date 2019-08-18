package de.ialistannen.configurator.dsl;

import lombok.Data;

/**
 * Executes the input as a file (via the shell, so shebangs are supported).
 */
@Data
public class ExecuteFileAstNode implements AstNode {

  private final AstNode content;

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitExecuteFile(this);
  }
}

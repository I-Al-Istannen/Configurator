package de.ialistannen.configurator.dsl;

import lombok.Data;

/**
 * An ast node that runs a shell command.
 */
@Data
public class ShellCommandAstNode implements AstNode {

  private final String command;

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitShellCommand(this);
  }
}

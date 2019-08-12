package de.ialistannen.configurator.dsl;

import lombok.Data;

/**
 * An ast node that runs a shell command.
 */
@Data
public class ShellCommandAstNode implements AstNode {

  private String command;

  /**
   * Creates a new shell command node.
   *
   * @param command the command to run
   */
  public ShellCommandAstNode(String command) {
    this.command = command;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visitShellCommand(this);
  }
}

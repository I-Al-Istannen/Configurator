package de.ialistannen.configurator.execution;

import static de.ialistannen.configurator.output.ColoredOutput.colorOut;
import static de.ialistannen.configurator.output.TerminalColor.BRIGHT_MAGENTA;
import static de.ialistannen.configurator.output.TerminalColor.GRAY;
import static de.ialistannen.configurator.output.TerminalColor.GREEN;

import de.ialistannen.configurator.context.Action;
import de.ialistannen.configurator.context.RenderContext;

/**
 * An {@link ActionDistributor} that prints what it would do.
 */
public class DryActionDistributor implements ActionDistributor {

  private boolean printFileContents;

  /**
   * Creates a new dry action distributor.
   *
   * @param printFileContents whether to print the file contents
   */
  public DryActionDistributor(boolean printFileContents) {
    this.printFileContents = printFileContents;
  }

  @Override
  public void distributeActions(RenderContext context) {
    for (Action action : context.getAllActions()) {
      colorOut(BRIGHT_MAGENTA + "Action: " + GREEN + action.getName());
      if (printFileContents) {
        colorOut(GRAY + action.getContent());
      }
    }
  }
}

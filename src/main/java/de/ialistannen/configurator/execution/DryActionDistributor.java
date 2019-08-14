package de.ialistannen.configurator.execution;

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
      System.out.println("Action: " + action.getName());
      if (printFileContents) {
        System.out.println(action.getContent());
      }
    }
  }
}

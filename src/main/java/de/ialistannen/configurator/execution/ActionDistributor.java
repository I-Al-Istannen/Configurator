package de.ialistannen.configurator.execution;

import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.exception.DistributionException;

/**
 * Takes care of distributing the rendered actions.
 */
public interface ActionDistributor {

  /**
   * Distributes the actions.
   *
   * @param context the o´context to take them from
   * @throws DistributionException if an error occurs while distributing an action
   */
  void distributeActions(RenderContext context) throws DistributionException;
}

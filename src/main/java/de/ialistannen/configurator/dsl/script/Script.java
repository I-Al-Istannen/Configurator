package de.ialistannen.configurator.dsl.script;

import de.ialistannen.configurator.context.RenderContext;

/**
 * A script that can modify the render context.
 */
public interface Script {

  /**
   * Executes the script.
   *
   * @param initial the initial context
   * @return the resulting context
   */
  RenderContext execute(RenderContext initial);
}

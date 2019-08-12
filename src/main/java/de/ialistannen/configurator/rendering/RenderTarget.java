package de.ialistannen.configurator.rendering;

import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.util.Pair;

/**
 * An object that can be rendered.
 *
 * @param <T> the type of the rendered object
 */
public interface RenderTarget<T extends RenderedObject> {

  /**
   * Renders this target.
   *
   * @param context the context to use
   * @return the rendered object and the resulting context
   */
  Pair<T, RenderContext> render(RenderContext context);
}

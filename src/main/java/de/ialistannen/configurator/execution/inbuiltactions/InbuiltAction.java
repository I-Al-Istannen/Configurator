package de.ialistannen.configurator.execution.inbuiltactions;

import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.context.RenderedAction;
import java.nio.file.Path;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

/**
 * An action that has full write and read access to the context.
 */
@RequiredArgsConstructor
public abstract class InbuiltAction {

  private final Function<RenderedAction, Path> pathResolver;

  /**
   * Converts this action to an {@link RenderedAction}.
   *
   * @param context the render context
   * @return the action
   */
  public abstract RenderedAction render(RenderContext context);

  /**
   * Resolves the path to an action.
   *
   * @param action the action
   * @return the path to the action
   */
  protected Path resolveActionPath(RenderedAction action) {
    return pathResolver.apply(action);
  }
}

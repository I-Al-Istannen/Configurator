package de.ialistannen.configurator.phases;

import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.rendering.FileRenderedObject;
import de.ialistannen.configurator.rendering.RenderTarget;
import de.ialistannen.configurator.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Renders multiple targets in succession.
 */
public class MultiTargetRenderer {

  /**
   * A renderer that does nothing.
   */
  public static final MultiTargetRenderer NOP_RENDERER = new MultiTargetRenderer(
      Collections.emptyList());

  private List<RenderTarget<FileRenderedObject>> targets;

  /**
   * Creates a new multi target renderer.
   *
   * @param targets the render targets to execute
   */
  public MultiTargetRenderer(List<? extends RenderTarget<FileRenderedObject>> targets) {
    this.targets = new ArrayList<>(targets);
  }

  /**
   * Renders al objects.
   *
   * @param context the initial context
   * @return the rendered objects and the resulting context
   */
  public Pair<List<FileRenderedObject>, RenderContext> render(RenderContext context) {
    List<FileRenderedObject> renderedObjects = new ArrayList<>();

    RenderContext currentContext = context;

    for (RenderTarget<FileRenderedObject> target : targets) {
      Pair<FileRenderedObject, RenderContext> render = target.render(currentContext);
      renderedObjects.add(render.getFirst());
      currentContext = render.getSecond();
    }

    return new Pair<>(renderedObjects, currentContext);
  }
}

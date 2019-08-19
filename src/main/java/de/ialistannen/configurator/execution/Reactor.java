package de.ialistannen.configurator.execution;

import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.phases.MultiTargetRenderer;
import de.ialistannen.configurator.phases.Phase;
import de.ialistannen.configurator.rendering.FileRenderedObject;
import de.ialistannen.configurator.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages rendering the phases in the correct order.
 */
public class Reactor {

  private final List<Phase> phases;
  private final Map<String, MultiTargetRenderer> targets;

  /**
   * Creates a new reactor.
   *
   * @param phases the phases to render
   * @param targets the available render targets
   */
  public Reactor(List<Phase> phases, Map<String, MultiTargetRenderer> targets) {
    this.phases = new ArrayList<>(phases);
    this.targets = new HashMap<>(targets);
  }

  /**
   * Cycles through all phases and renders all targets.
   *
   * @param context the context to start with
   * @return the resulting rendered objects and the new context
   */
  public Pair<List<FileRenderedObject>, RenderContext> renderAll(RenderContext context) {
    List<FileRenderedObject> renderedObjects = new ArrayList<>();

    RenderContext currentContext = context;

    for (Phase phase : phases) {
      MultiTargetRenderer renderer = targets.getOrDefault(
          phase.identifier(),
          MultiTargetRenderer.NOP_RENDERER
      );
      Pair<List<FileRenderedObject>, RenderContext> result = renderer.render(currentContext);

      renderedObjects.addAll(result.getFirst());
      currentContext = result.getSecond();
    }

    return new Pair<>(renderedObjects, currentContext);
  }
}

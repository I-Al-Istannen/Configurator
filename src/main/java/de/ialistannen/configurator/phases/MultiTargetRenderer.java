package de.ialistannen.configurator.phases;

import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.rendering.FileRenderedObject;
import de.ialistannen.configurator.rendering.RenderTarget;
import de.ialistannen.configurator.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Renders multiple targets in succession.
 */
public class MultiTargetRenderer {

  /**
   * A renderer that does nothing.
   */
  public static final MultiTargetRenderer NOP_RENDERER = new MultiTargetRenderer(
      Collections.emptyList()
  );

  private ExecutorService threadpool;

  private List<RenderTarget<FileRenderedObject>> targets;

  /**
   * Creates a new multi target renderer.
   *
   * @param targets the render targets to execute
   */
  public MultiTargetRenderer(List<? extends RenderTarget<FileRenderedObject>> targets) {
    this.targets = new ArrayList<>(targets);
    this.threadpool = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors(),
        r -> {
          Thread thread = new Thread(r, "MultiRenderWorker");
          thread.setDaemon(true);
          return thread;
        }
    );
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
    try {
      List<Future<Pair<FileRenderedObject, RenderContext>>> futures = threadpool.invokeAll(
          targets.stream()
              .map(target -> (Callable<Pair<FileRenderedObject, RenderContext>>)
                  () -> target.render(context)
              )
              .collect(Collectors.toList())
      );
      for (Future<Pair<FileRenderedObject, RenderContext>> future : futures) {
        Pair<FileRenderedObject, RenderContext> pair = future.get();
        renderedObjects.add(pair.getFirst());
        currentContext = pair.getSecond().merge(currentContext);
      }
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    return new Pair<>(renderedObjects, currentContext);
  }
}

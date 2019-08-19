package de.ialistannen.configurator.rendering;

import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.util.Pair;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;

/**
 * A {@link RenderTarget} that knows where to place the output.
 */
@RequiredArgsConstructor
public class FileRenderTarget implements RenderTarget<FileRenderedObject> {

  private final RenderTarget<? extends RenderedObject> underlying;
  private final RenderTarget<? extends RenderedObject> targetPathTarget;

  @Override
  public Pair<FileRenderedObject, RenderContext> render(RenderContext context) {
    Pair<? extends RenderedObject, RenderContext> rendered = this.underlying.render(context);
    RenderedObject renderedPath = this.targetPathTarget.render(context).getFirst();
    String pathString = renderedPath.asString();

    if (pathString.trim().isEmpty()) {
      throw new IllegalArgumentException("Empty path given...");
    }

    Path targetPath = Paths.get(pathString);

    return new Pair<>(
        new FileRenderedObject(targetPath, rendered.getFirst()),
        rendered.getSecond()
    );
  }
}

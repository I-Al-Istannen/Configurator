package de.ialistannen.configurator.rendering;

import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.util.Pair;
import java.nio.file.Path;

/**
 * A {@link RenderTarget} that knows where to place the output.
 */
public class FileRenderTarget implements RenderTarget<FileRenderedObject> {

  private RenderTarget<? extends RenderedObject> underlying;
  private Path targetPath;

  public FileRenderTarget(RenderTarget<? extends RenderedObject> underlying, Path targetPath) {
    this.underlying = underlying;
    this.targetPath = targetPath;
  }

  @Override
  public Pair<FileRenderedObject, RenderContext> render(RenderContext context) {
    Pair<? extends RenderedObject, RenderContext> rendered = this.underlying.render(context);

    return new Pair<>(
        new FileRenderedObject(targetPath, rendered.getFirst()),
        rendered.getSecond()
    );
  }
}

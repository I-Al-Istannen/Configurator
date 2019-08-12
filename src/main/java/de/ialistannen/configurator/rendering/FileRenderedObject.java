package de.ialistannen.configurator.rendering;

import java.nio.file.Path;
import lombok.Data;

/**
 * A rendered file.
 */
@Data
public class FileRenderedObject implements RenderedObject {

  private Path targetPath;
  private RenderedObject content;

  /**
   * Creates a new object.
   *
   * @param targetPath the target path
   * @param content the content
   */
  public FileRenderedObject(Path targetPath, RenderedObject content) {
    this.targetPath = targetPath;
    this.content = content;
  }

  @Override
  public String asString() {
    return content.asString();
  }
}

package de.ialistannen.configurator.rendering;

import java.nio.file.Path;
import lombok.Data;

/**
 * A rendered file.
 */
@Data
public class FileRenderedObject implements RenderedObject {

  private final Path targetPath;
  private final RenderedObject content;

  @Override
  public String asString() {
    return content.asString();
  }
}

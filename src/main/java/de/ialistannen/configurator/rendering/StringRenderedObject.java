package de.ialistannen.configurator.rendering;

import lombok.Data;

/**
 * A {@link RenderedObject} that just returns a string.
 */
@Data
public class StringRenderedObject implements RenderedObject {

  private final String content;

  @Override
  public String asString() {
    return content;
  }
}

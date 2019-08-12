package de.ialistannen.configurator.rendering;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A {@link RenderedObject} that just returns a string.
 */
@EqualsAndHashCode
@ToString
public class StringRenderedObject implements RenderedObject {

  private String content;

  public StringRenderedObject(String content) {
    this.content = content;
  }

  @Override
  public String asString() {
    return content;
  }
}

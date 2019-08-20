package de.ialistannen.configurator.context;

import lombok.Data;

/**
 * A rendered {@link Action}.
 */
@Data
public class RenderedAction {

  private final String name;
  private final String sanitizedName;
  private final String content;
  private final boolean hideFromRunAll;

}

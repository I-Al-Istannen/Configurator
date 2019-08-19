package de.ialistannen.configurator.context;

import de.ialistannen.configurator.dsl.AstNode;
import lombok.Data;

/**
 * An action with a name and source code.
 */
@Data
public class Action {

  private final String name;
  private final AstNode content;
  private final boolean hideFromRunAll;

  /**
   * Returns a sanitized version of the name that can be used as a file name.
   *
   * @return the sanitized name
   */
  public String getSanitizedName() {
    return name.replaceAll("\\s", "_").replace(":", "_");
  }
}

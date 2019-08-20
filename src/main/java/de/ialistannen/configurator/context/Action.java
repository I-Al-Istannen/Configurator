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
    return sanitizeName(name);
  }

  /**
   * Returns a sanitized version of the name that can be used as a file name.
   *
   * @param name the name to sanitize
   * @return the sanitized name
   */
  public static String sanitizeName(String name) {
    return name.replaceAll("\\s", "_").replace(":", "_");
  }
}

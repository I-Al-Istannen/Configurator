package de.ialistannen.configurator.util;

/**
 * An exception that occurred during parsing.
 */
public class ParseException extends Exception {

  private static final int CONTEXT_LENGTH = 10;

  /**
   * Creates a new parse exception.
   *
   * <p>The message is set to a small context string (the last X characters of the input), a marker
   * to indicate the position and the detail message.</p>
   *
   * @param reader the string reader
   * @param detail the detail error message
   */
  public ParseException(StringReader reader, String detail) {
    super(getContext(reader, detail));
  }

  private static String getContext(StringReader input, String detail) {
    int start = input.getPosition();
    start = Math.max(start - CONTEXT_LENGTH, 0);
    String contextString = input.getUnderlying().substring(start, input.getPosition());

    if (!detail.isEmpty()) {
      return detail + " at '" + contextString + "<---[HERE]";
    }

    return contextString + "<---[HERE]";
  }
}

package de.ialistannen.configurator.output;

import java.io.PrintStream;
import lombok.Setter;

/**
 * Helps printing colored output.
 */
public class ColoredOutput {

  @Setter
  private static boolean stripColour;

  /**
   * Prints a colored string.
   *
   * @param content the string to print
   */
  public static void colorOut(String content) {
    print(content, System.out);
  }

  /**
   * Prints a colored string to {@link System#err}.
   *
   * @param content the string to print
   */
  public static void colorErr(String content) {
    print(content, System.err);
  }

  private static void print(String content, PrintStream out) {
    String result = content;
    if (stripColour) {
      for (TerminalColor color : TerminalColor.values()) {
        result = result.replace(color.toString(), "");
      }
    } else {
      result = result + TerminalColor.RESET;
    }

    out.println(result);
  }
}

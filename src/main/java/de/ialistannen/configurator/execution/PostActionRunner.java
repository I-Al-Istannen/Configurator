package de.ialistannen.configurator.execution;

import static de.ialistannen.configurator.output.ColoredOutput.colorOut;
import static de.ialistannen.configurator.output.TerminalColor.BRIGHT_MAGENTA;
import static de.ialistannen.configurator.output.TerminalColor.DIM;
import static de.ialistannen.configurator.output.TerminalColor.GRAY;
import static de.ialistannen.configurator.output.TerminalColor.GREEN;

import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.util.ProcessUtils;
import lombok.RequiredArgsConstructor;

/**
 * Executes all post actions.
 */
@RequiredArgsConstructor
public class PostActionRunner {

  private static final int ABBREVIATE_LENGTH = 50;
  private final boolean dry;
  private final boolean printFullScripts;

  /**
   * Runs all post actions. Actions will be run in a random order, but the phase ordering is still
   * respected.
   *
   * @param context the context
   */
  public void run(RenderContext context) {
    for (String script : context.getAllPostScripts()) {
      if (!dry) {
        colorOut(BRIGHT_MAGENTA + "Running " + GREEN + abbreviate(script));
        ProcessUtils.runAsFileWithShell(script);
      } else {
        colorOut(BRIGHT_MAGENTA + "Would run " + DIM + GRAY + abbreviateIfNeeded(script));
      }
    }
  }

  private String abbreviateIfNeeded(String whole) {
    if (printFullScripts) {
      return whole;
    }
    return abbreviate(whole);
  }

  private String abbreviate(String whole) {
    return whole.substring(0, Math.min(whole.length(), ABBREVIATE_LENGTH))
        .replaceAll("\\n", " ⏎ ");
  }
}

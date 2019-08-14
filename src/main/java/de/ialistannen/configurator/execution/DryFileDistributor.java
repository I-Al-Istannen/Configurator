package de.ialistannen.configurator.execution;

import static de.ialistannen.configurator.output.ColoredOutput.colorOut;
import static de.ialistannen.configurator.output.TerminalColor.BRIGHT_MAGENTA;
import static de.ialistannen.configurator.output.TerminalColor.GRAY;
import static de.ialistannen.configurator.output.TerminalColor.GREEN;

import de.ialistannen.configurator.rendering.FileRenderedObject;
import java.util.List;

/**
 * A {@link FileDistributor} that just prints what it would do.
 */
public class DryFileDistributor implements FileDistributor {

  private boolean printContents;

  /**
   * Creates a new dry distributor.
   *
   * @param printContents whether to also print the file's contents or just their target path
   */
  public DryFileDistributor(boolean printContents) {
    this.printContents = printContents;
  }

  @Override
  public void distributeFiles(List<FileRenderedObject> renderedObjects) {
    for (FileRenderedObject renderedObject : renderedObjects) {
      colorOut(
          BRIGHT_MAGENTA + "Moving a file to " + GREEN + renderedObject.getTargetPath()
              .toAbsolutePath()
      );
      if (printContents) {
        if (renderedObject.asString().isEmpty()) {
          colorOut(GRAY + "Empty.");
        } else {
          colorOut(
              GRAY + "'" + renderedObject.asString() + "'"
          );
        }
      }
    }
  }
}

package de.ialistannen.configurator.execution;

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
      System.out.println("Moving a file to " + renderedObject.getTargetPath().toAbsolutePath());
      if (printContents) {
        System.out.println(renderedObject.asString());
      }
    }
  }
}

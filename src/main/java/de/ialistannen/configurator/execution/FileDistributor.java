package de.ialistannen.configurator.execution;

import de.ialistannen.configurator.exception.DistributionException;
import de.ialistannen.configurator.rendering.FileRenderedObject;
import java.util.List;

/**
 * Distributes {@link FileRenderedObject}s to their target place.
 */
public interface FileDistributor {

  /**
   * Distributes the objects to their corresponding files. How this is done (symlink, copy, dry,
   * etc.) is up to the implementing class.
   *
   * @param renderedObjects the rendered objects
   * @throws DistributionException if an error occurs
   */
  void distributeFiles(List<FileRenderedObject> renderedObjects) throws DistributionException;
}

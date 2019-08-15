package de.ialistannen.configurator.execution;

import static de.ialistannen.configurator.output.ColoredOutput.colorOut;
import static de.ialistannen.configurator.output.TerminalColor.BRIGHT_MAGENTA;
import static de.ialistannen.configurator.output.TerminalColor.GRAY;
import static de.ialistannen.configurator.output.TerminalColor.GREEN;
import static de.ialistannen.configurator.output.TerminalColor.MAGENTA;

import de.ialistannen.configurator.exception.DistributionException;
import de.ialistannen.configurator.rendering.FileRenderedObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Distributes all rendered objects into their assigned place.
 */
@RequiredArgsConstructor
public class FileSystemFileDistributor implements FileDistributor {

  private final boolean dry;
  private final boolean printFileContents;

  @Override
  public void distributeFiles(List<FileRenderedObject> renderedObjects)
      throws DistributionException {
    for (FileRenderedObject object : renderedObjects) {
      writeObject(object);
    }
  }

  private void writeObject(FileRenderedObject object) throws DistributionException {
    try {
      Path parentDir = object.getTargetPath().getParent();
      if (Files.notExists(parentDir)) {
        if (!dry) {
          Files.createDirectories(parentDir);
        } else {
          colorOut(BRIGHT_MAGENTA + "Creating dir " + GREEN + parentDir.toAbsolutePath());
        }
      }
      String content = object.getContent().asString();

      if (!dry) {
        Files.write(object.getTargetPath(), content.getBytes(StandardCharsets.UTF_8));
      } else {
        colorOut(MAGENTA + "Writing file to " + GREEN + object.getTargetPath());
        if (printFileContents) {
          colorOut(GRAY + object.getContent().asString());
        }
      }
    } catch (IOException e) {
      throw new DistributionException("Error writing file", e);
    }
  }
}

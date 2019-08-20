package de.ialistannen.configurator.util;

import static de.ialistannen.configurator.output.ColoredOutput.colorOut;
import static de.ialistannen.configurator.output.TerminalColor.BRIGHT_MAGENTA;
import static de.ialistannen.configurator.output.TerminalColor.GREEN;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import lombok.experimental.UtilityClass;

/**
 * Contains some utility functions for dealing with files.
 */
@UtilityClass
public class FileUtils {

  /**
   * Deletes a directory if dry is not set. If dry is set, it prints what it would do to delete it.
   *
   * @param directory the directory to delete
   * @param dry whether to only output the actions
   * @throws IOException if an error occurs
   */
  public static void deleteDirectory(Path directory, boolean dry) throws IOException {
    colorOut(BRIGHT_MAGENTA + "Deleting actions dir");
    Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (!dry) {
          Files.delete(file);
        } else {
          colorOut(BRIGHT_MAGENTA + "Deleting file " + GREEN + file.toAbsolutePath());
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (!dry) {
          Files.delete(dir);
        } else {
          colorOut(BRIGHT_MAGENTA + "Deleting dir " + GREEN + dir.toAbsolutePath());
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }

}

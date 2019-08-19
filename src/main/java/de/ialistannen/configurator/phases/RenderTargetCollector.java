package de.ialistannen.configurator.phases;

import de.ialistannen.configurator.rendering.FileRenderTarget;
import de.ialistannen.configurator.rendering.RenderTarget;
import de.ialistannen.configurator.template.StringRenderTarget;
import de.ialistannen.configurator.util.ParseException;
import de.ialistannen.configurator.util.StringReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Collects file render targets for a phase.
 */
public class RenderTargetCollector {

  /**
   * Collects all targets from a given folder (and subfolders).
   *
   * @param start the start path
   * @return all found targets in a map with the associated phase as key
   * @throws IOException if an error occurs reading or processing the files
   */
  public Map<String, MultiTargetRenderer> collectTargets(Path start) throws IOException {
    Map<String, List<FileRenderTarget>> renderTargets = new HashMap<>();

    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (!file.toString().endsWith(".conf")) {
          return FileVisitResult.CONTINUE;
        }
        try {
          handleFile(file);
        } catch (ParseException e) {
          throw new IOException("Error parsing file '" + file + "'", e);
        }
        return FileVisitResult.CONTINUE;
      }

      private void handleFile(Path file) throws IOException, ParseException {
        List<String> lines = Files.readAllLines(file);
        String phaseName = extractPhaseName(lines);
        RenderTarget<?> targetPathTarget = StringRenderTarget.singleLine(extractTargetPath(lines));

        String fileContents = lines.stream()
            .skip(2)
            .collect(Collectors.joining(System.lineSeparator()));
        FileRenderTarget renderTarget = new FileRenderTarget(
            new StringRenderTarget(fileContents),
            targetPathTarget
        );

        renderTargets.computeIfAbsent(phaseName, (key) -> new ArrayList<>())
            .add(renderTarget);
      }
    });

    return renderTargets.entrySet().stream()
        .collect(Collectors.toMap(
            Entry::getKey,
            it -> new MultiTargetRenderer(it.getValue())
        ));
  }

  private String extractPhaseName(List<String> lines) throws ParseException {
    StringReader reader = new StringReader(lines.get(0));

    reader.assertRead("Phase");
    reader.readWhile(Character::isWhitespace);
    reader.assertRead(": ");
    return reader.readPhrase();
  }

  private String extractTargetPath(List<String> lines) throws ParseException {
    StringReader reader = new StringReader(lines.get(1));

    reader.assertRead("Target path");
    reader.readWhile(Character::isWhitespace);
    reader.assertRead(": ");
    return reader.readPhrase();
  }
}

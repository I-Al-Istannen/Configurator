package de.ialistannen.configurator.execution;

import static de.ialistannen.configurator.output.ColoredOutput.colorOut;
import static de.ialistannen.configurator.output.TerminalColor.BRIGHT_MAGENTA;
import static de.ialistannen.configurator.output.TerminalColor.GRAY;
import static de.ialistannen.configurator.output.TerminalColor.GREEN;
import static de.ialistannen.configurator.output.TerminalColor.MAGENTA;

import de.ialistannen.configurator.context.Action;
import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.exception.DistributionException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;

/**
 * Distributes the actions into their own files.
 */
@RequiredArgsConstructor
public class DirBasedActionDistributor implements ActionDistributor {

  private static final String BASE_DIR_KEY = "actions_dir";

  private final boolean dry;
  private final boolean printFileContents;

  @Override
  public void distributeActions(RenderContext context) throws DistributionException {
    if (context.getAllActions().isEmpty()) {
      return;
    }
    String dir = context.<String>getValueOpt(BASE_DIR_KEY)
        .orElseThrow(() -> new RuntimeException("Could not find '$" + BASE_DIR_KEY));
    Path baseDir = Paths.get(dir);
    try {
      distribute(context, baseDir);
    } catch (IOException e) {
      throw new DistributionException("Error distributing actions", e);
    }
  }

  private void distribute(RenderContext context, Path baseDir) throws IOException {
    if (Files.notExists(baseDir)) {
      if (!dry) {
        Files.createDirectories(baseDir);
      } else {
        colorOut(BRIGHT_MAGENTA + "Creating dir " + GREEN + baseDir);
      }
    }

    if (!Files.isDirectory(baseDir) && !dry) {
      throw new IOException(baseDir.toAbsolutePath() + " is no directory");
    }

    for (Action action : context.getAllActions()) {
      Path actionPath = baseDir.resolve(action.getSanitizedName());
      if (!dry) {
        Files.write(actionPath, action.getContent().getBytes(StandardCharsets.UTF_8));
        makeExecutable(actionPath);
      } else {
        colorOut(
            MAGENTA + "Writing " + GREEN + action.getName()
                + MAGENTA + " to " + GREEN + actionPath.toAbsolutePath()
        );
        if (printFileContents) {
          colorOut(GRAY + action.getContent());
        }
      }
    }
  }

  private void makeExecutable(Path path) throws IOException {
    Set<PosixFilePermission> existingPerms = new HashSet<>(Files.getPosixFilePermissions(path));
    existingPerms.add(PosixFilePermission.OWNER_EXECUTE);

    Files.setPosixFilePermissions(path, existingPerms);
  }
}

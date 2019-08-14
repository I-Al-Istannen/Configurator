package de.ialistannen.configurator.execution;

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

/**
 * Distributes the actions into their own files.
 */
public class DirBasedActionDistributor implements ActionDistributor {

  private static final String BASE_DIR_KEY = "actions_dir";

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
      Files.createDirectories(baseDir);
    }

    if (!Files.isDirectory(baseDir)) {
      throw new IOException(baseDir.toAbsolutePath() + " is no directory");
    }

    for (Action action : context.getAllActions()) {
      Path actionPath = baseDir.resolve(action.getSanitizedName());
      Files.write(actionPath, action.getContent().getBytes(StandardCharsets.UTF_8));
      makeExecutable(actionPath);
    }
  }

  private void makeExecutable(Path path) throws IOException {
    Set<PosixFilePermission> existingPerms = new HashSet<>(Files.getPosixFilePermissions(path));
    existingPerms.add(PosixFilePermission.OWNER_EXECUTE);

    Files.setPosixFilePermissions(path, existingPerms);
  }
}

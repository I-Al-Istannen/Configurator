package de.ialistannen.configurator.execution;

import static de.ialistannen.configurator.output.ColoredOutput.colorOut;
import static de.ialistannen.configurator.output.TerminalColor.BRIGHT_MAGENTA;
import static de.ialistannen.configurator.output.TerminalColor.GRAY;
import static de.ialistannen.configurator.output.TerminalColor.GREEN;
import static de.ialistannen.configurator.output.TerminalColor.MAGENTA;

import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.context.RenderedAction;
import de.ialistannen.configurator.exception.DistributionException;
import de.ialistannen.configurator.execution.inbuiltactions.ReloadAction;
import de.ialistannen.configurator.execution.inbuiltactions.RunAllAction;
import de.ialistannen.configurator.util.FileUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

/**
 * Distributes the actions into their own files.
 */
@RequiredArgsConstructor
public class DirBasedActionDistributor implements ActionDistributor {

  private static final String BASE_DIR_KEY = "actions_dir";

  private final boolean dry;
  private final boolean printFileContents;
  private final boolean preserveActionsDir;

  @Override
  public void distributeActions(RenderContext context) throws DistributionException {
    if (context.getAllActions().isEmpty()) {
      return;
    }
    String dir = context.<String>getValueOpt(BASE_DIR_KEY)
        .orElseThrow(() -> new RuntimeException("Could not find '$" + BASE_DIR_KEY));
    Path baseDir = Paths.get(dir);
    try {
      if (!preserveActionsDir) {
        FileUtils.deleteDirectory(baseDir, dry);
      }
      distribute(addInbuiltActions(context, baseDir), baseDir);
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

    for (RenderedAction action : context.getAllActions()) {
      Path actionPath = resolveActionPath(baseDir, action);
      String content = action.getContent();

      if (!dry) {
        Files.write(actionPath, content.getBytes(StandardCharsets.UTF_8));
        makeExecutable(actionPath);
      } else {
        colorOut(
            MAGENTA + "Writing " + GREEN + action.getName()
                + MAGENTA + " to " + GREEN + actionPath.toAbsolutePath()
        );
        if (printFileContents) {
          colorOut(GRAY + content);
        }
      }
    }
  }

  private RenderContext addInbuiltActions(RenderContext context, Path baseDir) {
    Function<RenderedAction, Path> resolver = action -> resolveActionPath(baseDir, action);

    context = context.storeAction(new ReloadAction(resolver).render(context));
    context = context.storeAction(new RunAllAction(resolver).render(context));

    return context;
  }

  private Path resolveActionPath(Path baseDir, RenderedAction action) {
    return baseDir.resolve(action.getSanitizedName());
  }

  private void makeExecutable(Path path) throws IOException {
    Set<PosixFilePermission> existingPerms = new HashSet<>(Files.getPosixFilePermissions(path));
    existingPerms.add(PosixFilePermission.OWNER_EXECUTE);

    Files.setPosixFilePermissions(path, existingPerms);
  }
}

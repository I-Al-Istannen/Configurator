package de.ialistannen.configurator.execution;

import static de.ialistannen.configurator.output.ColoredOutput.colorOut;
import static de.ialistannen.configurator.output.TerminalColor.BRIGHT_MAGENTA;
import static de.ialistannen.configurator.output.TerminalColor.GRAY;
import static de.ialistannen.configurator.output.TerminalColor.GREEN;
import static de.ialistannen.configurator.output.TerminalColor.MAGENTA;

import de.ialistannen.configurator.context.Action;
import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.dsl.AstNode;
import de.ialistannen.configurator.dsl.DslParser;
import de.ialistannen.configurator.exception.DistributionException;
import de.ialistannen.configurator.template.StringRenderTarget;
import de.ialistannen.configurator.util.ParseException;
import de.ialistannen.configurator.util.StringReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
        deleteDirectory(baseDir);
      }
      distribute(generateRunScript(context, baseDir), baseDir);
    } catch (IOException e) {
      throw new DistributionException("Error distributing actions", e);
    }
  }

  private void deleteDirectory(Path baseDir) throws IOException {
    colorOut(BRIGHT_MAGENTA + "Deleting actions dir");
    Files.walkFileTree(baseDir, new SimpleFileVisitor<Path>() {
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
      Path actionPath = resolveActionPath(baseDir, action);
      String content = StringRenderTarget.fromAst(action.getContent())
          .render(context)
          .getFirst()
          .asString();

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

  private Path resolveActionPath(Path baseDir, Action action) {
    return baseDir.resolve(action.getSanitizedName());
  }

  private void makeExecutable(Path path) throws IOException {
    Set<PosixFilePermission> existingPerms = new HashSet<>(Files.getPosixFilePermissions(path));
    existingPerms.add(PosixFilePermission.OWNER_EXECUTE);

    Files.setPosixFilePermissions(path, existingPerms);
  }

  private RenderContext generateRunScript(RenderContext context, Path baseDir)
      throws DistributionException {
    StringBuilder runAction = new StringBuilder();
    runAction.append("#/bin/env bash").append(System.lineSeparator())
        .append("# vim: ft=sh").append(System.lineSeparator());

    List<Action> allActions = context.getAllActions()
        .stream()
        .sorted(Comparator.comparing(Action::getName))
        .collect(Collectors.toList());

    List<String> actionNames = allActions
        .stream()
        .map(Action::getName)
        .sorted()
        .collect(Collectors.toList());
    runAction.append(buildRunScriptRofiInvocation(actionNames));
    runAction.append(System.lineSeparator());

    runAction.append(buildRunScriptCase(allActions, baseDir));

    AstNode actionContent;
    try {
      actionContent = new DslParser(new StringReader(runAction.toString()), "#").parse();
    } catch (ParseException e) {
      throw new DistributionException("Could not create aggregate runner actionA", e);
    }
    return context.storeAction(new Action("Run action", actionContent));
  }

  private String buildRunScriptRofiInvocation(List<String> names) {
    StringBuilder result = new StringBuilder("CHOICE=$((");
    String separator = "`";
    for (String name : names) {
      result.append("echo -n '").append(name).append(separator).append("' ").append("; ");
    }
    result.append(") | rofi -sep '")
        .append(separator)
        .append("' -dmenu -p '' -matching fuzzy -i -no-custom -scroll-method 1)");
    return result.toString();
  }

  private String buildRunScriptCase(List<Action> actions, Path baseDir) {
    StringBuilder result = new StringBuilder()
        .append("if [ $? -eq 0 ]; then")
        .append(System.lineSeparator())
        .append("    case $CHOICE in")
        .append(System.lineSeparator());

    for (Action action : actions) {
      result.append("        '")
          .append(action.getName())
          .append("')").append(System.lineSeparator())
          .append("            (sleep 0.2 && ").append(resolveActionPath(baseDir, action))
          .append(") &")
          .append(System.lineSeparator())
          .append("            ;;").append(System.lineSeparator());
    }
    result.append("    esac").append(System.lineSeparator())
        .append("    exit").append(System.lineSeparator())
        .append("else").append(System.lineSeparator())
        .append("    echo \"Rofi returned failure\"").append(System.lineSeparator())
        .append("    exit").append(System.lineSeparator())
        .append("fi")
        .append(System.lineSeparator());
    return result.toString();
  }
}

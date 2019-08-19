package de.ialistannen.configurator;

import static de.ialistannen.configurator.output.ColoredOutput.colorErr;
import static de.ialistannen.configurator.output.ColoredOutput.colorOut;
import static de.ialistannen.configurator.output.TerminalColor.BLUE;
import static de.ialistannen.configurator.output.TerminalColor.BOLD;
import static de.ialistannen.configurator.output.TerminalColor.BRIGHT_BLUE;
import static de.ialistannen.configurator.output.TerminalColor.DIM;
import static de.ialistannen.configurator.output.TerminalColor.GREEN;
import static de.ialistannen.configurator.output.TerminalColor.MAGENTA;
import static de.ialistannen.configurator.output.TerminalColor.RED;
import static de.ialistannen.configurator.output.TerminalColor.RESET;
import static de.ialistannen.configurator.output.TerminalColor.UNDERLINE;

import de.ialistannen.configurator.config.Config;
import de.ialistannen.configurator.context.PhaseContext;
import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.exception.DistributionException;
import de.ialistannen.configurator.execution.ActionDistributor;
import de.ialistannen.configurator.execution.DirBasedActionDistributor;
import de.ialistannen.configurator.execution.FileDistributor;
import de.ialistannen.configurator.execution.FileSystemFileDistributor;
import de.ialistannen.configurator.execution.PostActionRunner;
import de.ialistannen.configurator.execution.Reactor;
import de.ialistannen.configurator.output.ColoredOutput;
import de.ialistannen.configurator.phases.MultiTargetRenderer;
import de.ialistannen.configurator.phases.RenderTargetCollector;
import de.ialistannen.configurator.rendering.FileRenderedObject;
import de.ialistannen.configurator.util.Pair;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Configurator {

  private static final String DRY_RUN = "d";
  private static final String PRINT_CONTENTS = "f";
  private static final String STRIP_COLOR = "n";
  private static final String PRINT_CONTEXT = "c";
  private static final String PRESERVE_ACTIONS_DIR = "p";
  private static final String TARGET_DIR = "t";
  private static final String HELP = "h";

  public static void main(String[] args) {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd;
    try {
      cmd = parser.parse(getOptions(), args);
    } catch (ParseException e) {
      colorErr(RED + e.getMessage());
      printHelp();
      return;
    }

    if (cmd.hasOption(HELP)) {
      printHelp();
      return;
    }

    ColoredOutput.setStripColour(cmd.hasOption(STRIP_COLOR));

    Path basePath;
    if (cmd.hasOption(TARGET_DIR)) {
      basePath = Paths.get(cmd.getOptionValue(TARGET_DIR));
      if (Files.notExists(basePath) || !Files.isDirectory(basePath)) {
        throw panic(
            RED + "The given target path "
                + BLUE + UNDERLINE + basePath + RESET + RED
                + " is no directory or does not exist!"
        );
      }
    } else {
      basePath = getOwnPath();
    }
    Path configPath = basePath.resolve(".configurator");

    if (!Files.isRegularFile(configPath)) {
      throw panic(
          RED + "Config file "
              + BLUE + UNDERLINE + configPath.toAbsolutePath() + RESET + RED
              + " not found!"
      );
    }

    try {
      Config config = Config.loadConfig(
          String.join(System.lineSeparator(), Files.readAllLines(configPath))
      );

      RenderTargetCollector targetCollector = new RenderTargetCollector();
      Map<String, MultiTargetRenderer> targets = targetCollector.collectTargets(basePath);
      Reactor reactor = new Reactor(config.getPhasesAsObject(), targets);

      Pair<List<FileRenderedObject>, RenderContext> rendered = reactor.renderAll(
          new PhaseContext()
      );

      boolean dry = cmd.hasOption(DRY_RUN);
      boolean printFileContents = cmd.hasOption(PRINT_CONTENTS);
      boolean preserveActionsDir = cmd.hasOption(PRESERVE_ACTIONS_DIR);

      if (dry) {
        String dryHeader = " _____                     _   _               ____  _\n"
            + "| ____|_  _____  ___ _   _| |_(_) ___  _ __   |  _ \\| | __ _ _ __\n"
            + "|  _| \\ \\/ / _ \\/ __| | | | __| |/ _ \\| '_ \\  | |_) | |/ _` | '_ \\\n"
            + "| |___ >  <  __/ (__| |_| | |_| | (_) | | | | |  __/| | (_| | | | |\n"
            + "|_____/_/\\_\\___|\\___|\\__,_|\\__|_|\\___/|_| |_| |_|   |_|\\__,_|_| |_|\n";
        printHeader(dryHeader);
      }

      ActionDistributor actionDistributor = new DirBasedActionDistributor(
          dry,
          printFileContents,
          preserveActionsDir
      );
      FileDistributor fileDistributor = new FileSystemFileDistributor(dry, printFileContents);

      fileDistributor.distributeFiles(rendered.getFirst());
      actionDistributor.distributeActions(rendered.getSecond());

      if (dry) {
        String actionsHeader = " ____           _     ____            _       _\n"
            + "|  _ \\ ___  ___| |_  / ___|  ___ _ __(_)_ __ | |_ ___\n"
            + "| |_) / _ \\/ __| __| \\___ \\ / __| '__| | '_ \\| __/ __|\n"
            + "|  __/ (_) \\__ \\ |_   ___) | (__| |  | | |_) | |_\\__ \\\n"
            + "|_|   \\___/|___/\\__| |____/ \\___|_|  |_| .__/ \\__|___/\n"
            + "                                       |_|\n";
        printHeader(actionsHeader);
      }

      new PostActionRunner(dry, printFileContents).run(rendered.getSecond());

      if (cmd.hasOption(PRINT_CONTEXT)) {
        String contextHeader = "  ____            _            _\n"
            + " / ___|___  _ __ | |_ _____  _| |_\n"
            + "| |   / _ \\| '_ \\| __/ _ \\ \\/ / __|\n"
            + "| |__| (_) | | | | ||  __/>  <| |_\n"
            + " \\____\\___/|_| |_|\\__\\___/_/\\_\\\\__|\n";
        printHeader(contextHeader);
        colorOut(BLUE.toString() + BOLD + UNDERLINE + "Values:");
        String values = rendered.getSecond().getAllValues().entrySet()
            .stream()
            .sorted(Entry.comparingByKey())
            .map(entry -> MAGENTA + entry.getKey() + "=" + GREEN + entry.getValue())
            .collect(Collectors.joining(", "));
        colorOut(values);
        colorOut(DIM.toString() + UNDERLINE + repeat(" ", 40));
        colorOut(BRIGHT_BLUE.toString() + BOLD + UNDERLINE + "Actions:");
        String actionNames = rendered.getSecond()
            .getAllActions()
            .stream()
            .map(act -> act.getName() + (act.isHideFromRunAll() ? "(\uD83D\uDC7B)" : ""))
            .sorted()
            .collect(Collectors.joining(", "));
        colorOut(MAGENTA + actionNames);
        colorOut(DIM.toString() + UNDERLINE + repeat(" ", 40));
        colorOut(BRIGHT_BLUE.toString() + BOLD + UNDERLINE + "Post scripts:");
        String postScriptStarts = rendered.getSecond()
            .getAllPostScripts()
            .stream()
            .map(it -> it.replaceFirst("#.+", ""))
            .map(it -> it.substring(0, Math.min(it.length(), 10)).trim())
            .sorted()
            .collect(Collectors.joining(", "));
        colorOut(MAGENTA + postScriptStarts);
      }
    } catch (DistributionException | IOException e) {
      if (e.getCause() != null) {
        colorErr(RED + e.getMessage() + ": ");
        colorErr("\t" + RED + e.getCause().getMessage());
      } else {
        colorErr(RED + e.getMessage());
      }
    }
  }

  private static void printHelp() {
    new HelpFormatter().printHelp(
        "configurator",
        "A simple program to help organize dotfiles.",
        getOptions(),
        "Made by <I Al Istannen>",
        true
    );
  }

  private static RuntimeException panic(String message) {
    colorErr(message);
    System.exit(1);
    // never reache
    return new RuntimeException();
  }

  private static void printHeader(String header) {
    colorOut(RED.toString() + BOLD + "\n" + header + "\n");
  }

  private static String repeat(String string, int amount) {
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < amount; i++) {
      result.append(string);
    }
    return result.toString();
  }

  private static Options getOptions() {
    Options options = new Options();
    options.addOption(Option.builder(DRY_RUN)
        .longOpt("dry")
        .hasArg(false)
        .desc("Whether the program should run without altering files.")
        .type(Boolean.class)
        .build()
    );
    options.addOption(Option.builder(PRINT_CONTENTS)
        .longOpt("print-contents")
        .hasArg(false)
        .desc("Whether the program should print the whole file contents when running in dry mode.")
        .type(Boolean.class)
        .build()
    );
    options.addOption(Option.builder(STRIP_COLOR)
        .longOpt("strip-color")
        .hasArg(false)
        .desc("If present the program will not color its output.")
        .type(Boolean.class)
        .build()
    );
    options.addOption(Option.builder(PRINT_CONTEXT)
        .longOpt("print-context")
        .hasArg(false)
        .desc(
            "If present the program will print the final context after all rendering steps were completed."
        )
        .type(Boolean.class)
        .build()
    );
    options.addOption(Option.builder(PRESERVE_ACTIONS_DIR)
        .longOpt("preserve-actions-dir")
        .hasArg(false)
        .desc(
            "If present the program will not delete the action dir and preserve manually added actions"
        )
        .type(Boolean.class)
        .build()
    );
    options.addOption(Option.builder(TARGET_DIR)
        .argName("target directory")
        .longOpt("target-dir")
        .hasArg(true)
        .desc(
            "The path to the configuration directory. If not given the current working directory will be used"
        )
        .type(String.class)
        .build()
    );
    options.addOption(Option.builder(HELP)
        .longOpt("help")
        .hasArg(false)
        .desc("Prints the help")
        .type(Boolean.class)
        .build()
    );
    return options;
  }

  private static Path getOwnPath() {
    try {
      return Paths.get(
          Configurator.class.getProtectionDomain().getCodeSource().getLocation().toURI()
      );
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}

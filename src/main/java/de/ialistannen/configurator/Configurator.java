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
import static de.ialistannen.configurator.output.TerminalColor.UNDERLINE;

import de.ialistannen.configurator.config.Config;
import de.ialistannen.configurator.context.Action;
import de.ialistannen.configurator.context.PhaseContext;
import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.exception.DistributionException;
import de.ialistannen.configurator.execution.ActionDistributor;
import de.ialistannen.configurator.execution.DirBasedActionDistributor;
import de.ialistannen.configurator.execution.FileDistributor;
import de.ialistannen.configurator.execution.FileSystemFileDistributor;
import de.ialistannen.configurator.execution.Reactor;
import de.ialistannen.configurator.output.ColoredOutput;
import de.ialistannen.configurator.phases.MultiTargetRenderer;
import de.ialistannen.configurator.phases.RenderTargetCollector;
import de.ialistannen.configurator.rendering.FileRenderedObject;
import de.ialistannen.configurator.util.Pair;
import java.io.IOException;
import java.net.URI;
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

  public static void main(String[] args) throws IOException {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd;
    try {
      cmd = parser.parse(getOptions(), args);
    } catch (ParseException e) {
      colorErr(RED + e.getMessage());
      new HelpFormatter().printHelp(
          "configurator",
          "A simple program to help organize dotfiles.",
          getOptions(),
          "Created mostly to play around a bit.",
          true
      );
      return;
    }

    ColoredOutput.setStripColour(cmd.hasOption(STRIP_COLOR));

    Path basePath = getOwnPath();
    Path configPath = basePath.resolve(".configurator.yaml");

    Config config = Config.loadConfig(
        String.join(System.lineSeparator(), Files.readAllLines(configPath))
    );

    RenderTargetCollector targetCollector = new RenderTargetCollector();
    Map<String, MultiTargetRenderer> targets = targetCollector.collectTargets(basePath);
    Reactor reactor = new Reactor(config.getPhasesAsObject(), targets);

    Pair<List<FileRenderedObject>, RenderContext> rendered = reactor.renderAll(
        new PhaseContext()
    );

    try {
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
    } catch (DistributionException e) {
      e.printStackTrace();
    }

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
          .map(Action::getName)
          .sorted()
          .collect(Collectors.joining(", "));
      colorOut(MAGENTA + actionNames);
    }
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
        .argName("Dry run")
        .longOpt("dry")
        .hasArg(false)
        .desc("Whether the program should run without altering files.")
        .type(Boolean.class)
        .build()
    );
    options.addOption(Option.builder(PRINT_CONTENTS)
        .argName("Print contents")
        .longOpt("print-contents")
        .hasArg(false)
        .desc("Whether the program should print the whole file contents when running in dry mode.")
        .type(Boolean.class)
        .build()
    );
    options.addOption(Option.builder(STRIP_COLOR)
        .argName("Strip color")
        .longOpt("strip-color")
        .hasArg(false)
        .desc("If present the program will not color its output.")
        .type(Boolean.class)
        .build()
    );
    options.addOption(Option.builder(PRINT_CONTEXT)
        .argName("Prints the context")
        .longOpt("print-context")
        .hasArg(false)
        .desc(
            "If present the program will print the final context after all rendering steps were completed."
        )
        .type(Boolean.class)
        .build()
    );
    options.addOption(Option.builder(PRESERVE_ACTIONS_DIR)
        .argName("Does not clear the actions dir of any existing actions.")
        .longOpt("preserve-actions-dir")
        .hasArg(false)
        .desc(
            "If present the program will not delete the action dir to preserve existing actions"
        )
        .type(Boolean.class)
        .build()
    );
    return options;
  }

  private static Path getOwnPath() {
    try {
//      return Paths.get(
//          Configurator.class.getProtectionDomain().getCodeSource().getLocation().toURI()
//      );
      return Paths.get(
          new URI("file:/home/i_al_istannen/configurator")
      );
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}

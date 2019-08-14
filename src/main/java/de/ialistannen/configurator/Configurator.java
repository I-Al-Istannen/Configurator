package de.ialistannen.configurator;

import static de.ialistannen.configurator.output.ColoredOutput.colorErr;
import static de.ialistannen.configurator.output.ColoredOutput.colorOut;
import static de.ialistannen.configurator.output.TerminalColor.GRAY;
import static de.ialistannen.configurator.output.TerminalColor.RED;

import de.ialistannen.configurator.config.Config;
import de.ialistannen.configurator.context.PhaseContext;
import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.exception.DistributionException;
import de.ialistannen.configurator.execution.ActionDistributor;
import de.ialistannen.configurator.execution.DirBasedActionDistributor;
import de.ialistannen.configurator.execution.DryActionDistributor;
import de.ialistannen.configurator.execution.DryFileDistributor;
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
  private static final String STRIP_COLOR = "c";

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

    colorOut(GRAY + "Final context is " + rendered.getSecond());
    new DryFileDistributor(cmd.hasOption(PRINT_CONTENTS)).distributeFiles(rendered.getFirst());

    try {
      ActionDistributor distributor;
      if (cmd.hasOption(DRY_RUN)) {
        distributor = new DryActionDistributor(cmd.hasOption(PRINT_CONTENTS));
      } else {
        distributor = new DirBasedActionDistributor();
      }
      distributor.distributeActions(rendered.getSecond());
    } catch (DistributionException e) {
      e.printStackTrace();
    }
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

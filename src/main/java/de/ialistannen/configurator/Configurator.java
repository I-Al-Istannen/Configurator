package de.ialistannen.configurator;

import de.ialistannen.configurator.config.Config;
import de.ialistannen.configurator.context.PhaseContext;
import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.execution.DryFileDistributor;
import de.ialistannen.configurator.execution.Reactor;
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
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Configurator {

  private static final String DRY_RUN = "d";
  private static final String PRINT_CONTENTS = "f";

  public static void main(String[] args) throws IOException {
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

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      e.printStackTrace();
      return;
    }

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

    System.out.println("Final context is " + rendered.getSecond());
    new DryFileDistributor(cmd.hasOption(PRINT_CONTENTS)).distributeFiles(rendered.getFirst());
  }

  private static Path getOwnPath() {
    try {
//      return Paths.get(
//          Configurator.class.getProtectionDomain().getCodeSource().getLocation().toURI()
//      );
      return Paths.get(
          new URI("file:/home/i_al_istannen/Programming/Random/Configurator/src/test/resources")
      );
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}

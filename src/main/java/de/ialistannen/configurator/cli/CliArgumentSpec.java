package de.ialistannen.configurator.cli;

import static de.ialistannen.configurator.output.TerminalColor.BLUE;
import static de.ialistannen.configurator.output.TerminalColor.RED;
import static de.ialistannen.configurator.output.TerminalColor.RESET;
import static de.ialistannen.configurator.output.TerminalColor.UNDERLINE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

/**
 * A simple program to help organize dotfiles.
 *
 * Made by [I Al Istannen]
 */
@CommandLineArguments(programName = "Configurator", missionStatement = "Configuration made complex")
abstract class CliArgumentSpec {

  /**
   * Whether the program should run without altering files..
   */
  @Parameter(shortName = 'd', longName = "dry")
  public abstract boolean dry();

  /**
   * Whether the program should print the whole file contents when running in dry mode.
   */
  @Parameter(shortName = 'f', longName = "print-contents")
  public abstract boolean printFileContents();

  /**
   * If present the program will not color its output.
   */
  @Parameter(shortName = 'n', longName = "strip-color")
  public abstract boolean stripColor();

  /**
   * If present the program will print the final context after all rendering steps were completed.
   */
  @Parameter(shortName = 'c', longName = "print-context")
  public abstract boolean printContext();

  /**
   * If present the program will not delete the action dir and preserve manually added actions
   */
  @Parameter(shortName = 'p', longName = "preserve-actions-dir")
  public abstract boolean preserveActionsDir();

  /**
   * Whether the program should report all parse errors. A parse error might just be some token that
   * matches by chance and not an actual error.
   */
  @Parameter(shortName = 'r', longName = "report-parse-errors")
  public abstract boolean reportParseErrors();

  /**
   * The path to the configuration directory. If not given the current working directory will be
   * used.
   */
  @Parameter(shortName = 't', longName = "target-dir", optional = true, mappedBy = DirectoryMapper.class)
  public abstract Optional<Path> targetDir();

  static class DirectoryMapper implements Supplier<Function<String, Path>> {

    @Override
    public Function<String, Path> get() {
      return pathName -> {
        Path path = Paths.get(pathName);
        if (Files.notExists(path) || !Files.isDirectory(path)) {
          throw new IllegalArgumentException(
              RED + "The given target path "
                  + BLUE + UNDERLINE + path + RESET + RED
                  + " is no directory or does not exist!"
                  + RESET
          );
        }
        return path;
      };
    }
  }
}

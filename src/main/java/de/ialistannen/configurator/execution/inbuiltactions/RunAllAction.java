package de.ialistannen.configurator.execution.inbuiltactions;

import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.context.RenderedAction;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generates a launcher for all options.
 */
public class RunAllAction extends InbuiltAction {

  /**
   * Creates a new run all action.
   *
   * @param pathResolver the action path resolver
   */
  public RunAllAction(Function<RenderedAction, Path> pathResolver) {
    super(pathResolver);
  }

  @Override
  public Optional<RenderedAction> render(RenderContext context) {
    if (context.getAllActions().isEmpty() && context.getAllReloadActions().isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new RenderedAction(
        "Run action",
        "Run_action",
        generateRunScript(context),
        false
    ));
  }

  private String generateRunScript(RenderContext context) {
    StringBuilder runAction = new StringBuilder();
    runAction.append("#/bin/env bash").append(System.lineSeparator())
        .append("# vim: ft=sh").append(System.lineSeparator());

    List<RenderedAction> allActions = context.getAllActions()
        .stream()
        .filter(it -> !it.isHideFromRunAll())
        .sorted(Comparator.comparing(RenderedAction::getName))
        .collect(Collectors.toList());

    List<String> actionNames = allActions
        .stream()
        .map(RenderedAction::getName)
        .sorted()
        .collect(Collectors.toList());
    runAction.append(buildRunScriptRofiInvocation(actionNames));
    runAction.append(System.lineSeparator());

    runAction.append(buildRunScriptCase(allActions));

    return runAction.toString();
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

  private String buildRunScriptCase(List<RenderedAction> actions) {
    StringBuilder result = new StringBuilder()
        .append("if [ $? -eq 0 ]; then")
        .append(System.lineSeparator())
        .append("    case $CHOICE in")
        .append(System.lineSeparator());

    for (RenderedAction action : actions) {
      result.append("        '")
          .append(action.getName())
          .append("')").append(System.lineSeparator())
          .append("            (sleep 0.2 && ").append(resolveActionPath(action))
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

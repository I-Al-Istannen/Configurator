package de.ialistannen.configurator.execution.inbuiltactions;

import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.context.RenderedAction;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

/**
 * An action to execute all reload action.
 */
public class ReloadAction extends InbuiltAction {

  /**
   * Creates a new reload action.
   *
   * @param pathResolver the action path resolver
   */
  public ReloadAction(Function<RenderedAction, Path> pathResolver) {
    super(pathResolver);
  }

  @Override
  public RenderedAction render(RenderContext context) {
    return new RenderedAction(
        "Reload all",
        "Reload_all",
        buildScript(context.getAllReloadActions()),
        false
    );
  }

  private String buildScript(List<RenderedAction> reloadActions) {
    return "#!/bin/sh\n" + buildCalls(reloadActions);
  }

  private String buildCalls(List<RenderedAction> reloadActions) {
    StringBuilder calls = new StringBuilder();

    for (RenderedAction action : reloadActions) {
      calls.append(resolveActionPath(action).toAbsolutePath())
          .append(" &") // execute in sub shell
          .append("\n");
    }

    return calls.toString();
  }
}

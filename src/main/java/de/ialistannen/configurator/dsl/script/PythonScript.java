package de.ialistannen.configurator.dsl.script;

import de.ialistannen.configurator.context.Action;
import de.ialistannen.configurator.context.PhaseContext;
import de.ialistannen.configurator.context.RenderContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import org.graalvm.polyglot.Context;

/**
 * A python script.
 */
@Data
public class PythonScript implements Script {

  private String script;

  /**
   * Creates a new python script.
   *
   * @param script the python script
   */
  public PythonScript(String script) {
    this.script = script;
  }

  @Override
  public RenderContext execute(RenderContext initial) {
    StatefulContextHolder contextHolder = new StatefulContextHolder(initial);
    Context context = Context.newBuilder("python")
        .allowAllAccess(true)
        .build();
    context.getBindings("python").putMember("context", contextHolder);

    context.eval("python", script);
    return contextHolder.underlying;
  }

  private static class StatefulContextHolder implements RenderContext {

    private RenderContext underlying;

    StatefulContextHolder(RenderContext underlying) {
      this.underlying = underlying;
    }

    @Override
    public <T> T getValue(String key) {
      return underlying.getValue(key);
    }

    @Override
    public <T> Optional<T> getValueOpt(String key) {
      return underlying.getValueOpt(key);
    }

    @Override
    public <T> RenderContext storeValue(String key, T val) {
      underlying = underlying.storeValue(key, val);
      return underlying;
    }

    @Override
    public Map<String, Object> getAllValues() {
      return underlying.getAllValues();
    }

    @Override
    public RenderContext storeAction(Action action) {
      underlying = underlying.storeAction(action);
      return underlying;
    }

    @Override
    public Action getAction(String name) {
      return underlying.getAction(name);
    }

    @Override
    public Optional<Action> getActionOpt(String name) {
      return underlying.getActionOpt(name);
    }

    @Override
    public List<Action> getAllActions() {
      return underlying.getAllActions();
    }

    @Override
    public RenderContext merge(RenderContext other) {
      return underlying.merge(other);
    }
  }

  public static void main(String[] args) {
    RenderContext context = new PhaseContext();
    String script = "context.storeValue(\"Hello\", 20);";
//    String script = "print(dir(context))";
    System.out.println(new PythonScript(script).execute(context));
  }

}

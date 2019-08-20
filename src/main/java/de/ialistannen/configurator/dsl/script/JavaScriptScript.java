package de.ialistannen.configurator.dsl.script;

import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.context.RenderedAction;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess.Export;

/**
 * A python script.
 */
@Data
public class JavaScriptScript implements Script {

  private String script;

  /**
   * Creates a new python script.
   *
   * @param script the python script
   */
  public JavaScriptScript(String script) {
    this.script = script;
  }

  @Override
  public RenderContext execute(RenderContext initial) {
    StatefulContextHolder contextHolder = new StatefulContextHolder(initial);
    Context context = Context.newBuilder("js")
        .allowAllAccess(true)
        .build();
    context.getBindings("js").putMember("context", contextHolder);

    context.eval("js", script);
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
      return this;
    }

    @Override
    public Map<String, Object> getAllValues() {
      return underlying.getAllValues();
    }

    @Export
    @Override
    public RenderContext storeAction(RenderedAction action) {
      underlying = underlying.storeAction(action);
      return this;
    }

    @Override
    public RenderedAction getAction(String name) {
      return underlying.getAction(name);
    }

    @Override
    public Optional<RenderedAction> getActionOpt(String name) {
      return underlying.getActionOpt(name);
    }

    @Override
    public List<RenderedAction> getAllActions() {
      return underlying.getAllActions();
    }

    @Override
    public RenderContext merge(RenderContext other) {
      return underlying.merge(other);
    }

    @Override
    public RenderContext storePostScript(String content) {
      underlying = underlying.storePostScript(content);
      return this;
    }

    @Override
    public List<String> getAllPostScripts() {
      return underlying.getAllPostScripts();
    }

    @Override
    public RenderContext storeReloadAction(RenderedAction action) {
      underlying = underlying.storeReloadAction(action);
      return this;
    }

    @Override
    public List<RenderedAction> getAllReloadActions() {
      return underlying.getAllReloadActions();
    }
  }
}

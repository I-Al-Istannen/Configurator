package de.ialistannen.configurator.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.pcollections.ConsPStack;
import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;

/**
 * The render context for phases.
 */
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PhaseContext implements RenderContext {

  private HashPMap<String, Object> values;
  private HashPMap<String, RenderedAction> actions;
  private ConsPStack<String> postActions;
  private ConsPStack<RenderedAction> reloadActions;

  public PhaseContext() {
    this(HashTreePMap.empty(), HashTreePMap.empty(), ConsPStack.empty(), ConsPStack.empty());
  }

  @Override
  public <T> T getValue(String key) {
    @SuppressWarnings("unchecked")
    T t = (T) values.get(key);
    return t;
  }

  @Override
  public <T> Optional<T> getValueOpt(String key) {
    return Optional.ofNullable(getValue(key));
  }

  @Override
  public <T> PhaseContext storeValue(String key, T val) {
    return new PhaseContext(values.plus(key, val), actions, postActions, reloadActions);
  }

  @Override
  public Map<String, Object> getAllValues() {
    return Collections.unmodifiableMap(values);
  }

  @Override
  public RenderContext storeAction(RenderedAction action) {
    return new PhaseContext(
        values,
        actions.plus(action.getName(), action),
        postActions,
        reloadActions
    );
  }

  @Override
  public RenderedAction getAction(String name) {
    return actions.get(name);
  }

  @Override
  public Optional<RenderedAction> getActionOpt(String name) {
    return Optional.ofNullable(getAction(name));
  }

  @Override
  public List<RenderedAction> getAllActions() {
    return new ArrayList<>(actions.values());
  }

  @Override
  public RenderContext merge(RenderContext other) {
    RenderContext result = this;
    for (RenderedAction action : other.getAllActions()) {
      if (!actions.containsKey(action.getName())) {
        result = result.storeAction(action);
      }
    }

    for (Entry<String, Object> entry : other.getAllValues().entrySet()) {
      if (!values.containsKey(entry.getKey())) {
        result = result.storeValue(entry.getKey(), entry.getValue());
      }
    }

    for (RenderedAction reloadAction : other.getAllReloadActions()) {
      if (!reloadActions.contains(reloadAction)) {
        result = result.storeReloadAction(reloadAction);
      }
    }

    for (String postScript : other.getAllPostScripts()) {
      if (!postActions.contains(postScript)) {
        result = result.storePostScript(postScript);
      }
    }

    return result;
  }

  @Override
  public RenderContext storePostScript(String content) {
    return new PhaseContext(values, actions, postActions.plus(content), reloadActions);
  }

  @Override
  public List<String> getAllPostScripts() {
    return postActions;
  }

  @Override
  public RenderContext storeReloadAction(RenderedAction action) {
    return new PhaseContext(
        values, actions, postActions, reloadActions.plus(action)
    );
  }

  @Override
  public List<RenderedAction> getAllReloadActions() {
    return Collections.unmodifiableList(reloadActions);
  }
}

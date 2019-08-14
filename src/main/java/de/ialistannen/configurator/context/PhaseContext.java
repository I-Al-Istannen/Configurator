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
  private HashPMap<String, Action> actions;

  public PhaseContext() {
    this(HashTreePMap.empty(), HashTreePMap.empty());
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
    return new PhaseContext(values.plus(key, val), actions);
  }

  @Override
  public Map<String, Object> getAllValues() {
    return Collections.unmodifiableMap(values);
  }

  @Override
  public RenderContext storeAction(Action action) {
    return new PhaseContext(values, actions.plus(action.getName(), action));
  }

  @Override
  public Action getAction(String name) {
    return actions.get(name);
  }

  @Override
  public Optional<Action> getActionOpt(String name) {
    return Optional.ofNullable(getAction(name));
  }

  @Override
  public List<Action> getAllActions() {
    return new ArrayList<>(actions.values());
  }

  @Override
  public RenderContext merge(RenderContext other) {
    RenderContext result = this;
    for (Action action : other.getAllActions()) {
      if (!actions.containsKey(action.getName())) {
        result = result.storeAction(action);
      }
    }

    for (Entry<String, Object> entry : other.getAllValues().entrySet()) {
      if (!values.containsKey(entry.getKey())) {
        result = result.storeValue(entry.getKey(), entry.getValue());
      }
    }

    return result;
  }
}

package de.ialistannen.configurator.context;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;

/**
 * The render context for phases.
 */
@ToString
@EqualsAndHashCode
public class PhaseContext implements RenderContext {

  private HashPMap<String, Object> values;

  public PhaseContext() {
    this(Collections.emptyMap());
  }

  public PhaseContext(Map<String, Object> entries) {
    this.values = HashTreePMap.from(entries);
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
    return new PhaseContext(values.plus(key, val));
  }

  @Override
  public Map<String, Object> getAllValues() {
    return Collections.unmodifiableMap(values);
  }
}

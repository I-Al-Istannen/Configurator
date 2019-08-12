package de.ialistannen.configurator.context;

import java.util.Map;
import java.util.Optional;

/**
 * The context for rendering a file. Immutable.
 */
public interface RenderContext {

  /**
   * Returns a value by its name.
   *
   * @param key the key to look it up under
   * @param <T> the type of the return value
   * @return the found value or null
   */
  <T> T getValue(String key);

  /**
   * Returns a value by its name.
   *
   * @param key the key to look it up under
   * @param <T> the type of the return value
   * @return the found value or an empty optional if none
   */
  <T> Optional<T> getValueOpt(String key);

  /**
   * Stores a given value.
   *
   * @param key the key to store it under
   * @param val the value to store
   * @param <T> the type of the value
   * @return a new render context
   */
  <T> RenderContext storeValue(String key, T val);

  /**
   * Returns all values.
   *
   * @return all values
   */
  Map<String, Object> getAllValues();
}

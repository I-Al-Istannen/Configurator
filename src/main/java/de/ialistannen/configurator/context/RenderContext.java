package de.ialistannen.configurator.context;

import java.util.List;
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

  /**
   * Stores a new action.
   *
   * @param action the action
   * @return the new render context
   */
  RenderContext storeAction(RenderedAction action);

  /**
   * Returns the action with the given name.
   *
   * @param name the name of the action
   * @return the action or null
   */
  RenderedAction getAction(String name);

  /**
   * Returns the action with the given name.
   *
   * @param name the name of the action
   * @return the action or null
   */
  Optional<RenderedAction> getActionOpt(String name);

  /**
   * Returns all actionS.
   *
   * @return the actions
   */
  List<RenderedAction> getAllActions();

  /**
   * Merges the two contexts. Keeps the keys of this context if there are conflicts.
   *
   * @param other the other context
   * @return the merged context
   */
  RenderContext merge(RenderContext other);

  /**
   * Adds a new script to be run at the end.
   *
   * @param content the content
   * @return the render context
   */
  RenderContext storePostScript(String content);

  /**
   * Returns all scripts that will be run at the end.
   *
   * @return all scripts to run at the end
   */
  List<String> getAllPostScripts();

  /**
   * Stores a new reload action.
   *
   * @param action the action
   * @return the render context
   */
  RenderContext storeReloadAction(RenderedAction action);

  /**
   * Returns all scripts that will reload a program.
   *
   * @return all scripts that reload a program
   */
  List<RenderedAction> getAllReloadActions();
}

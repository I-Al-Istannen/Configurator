package de.ialistannen.configurator.phases;

/**
 * A phase of the build process.
 */
public interface Phase {

  /**
   * Returns the identifier of this phase.
   *
   * @return the identifier of this phase
   */
  String identifier();

  /**
   * Returns the priority of this phase.
   *
   * @return the priority of the phase
   */
  int priority();
}

package de.ialistannen.configurator.exception;

/**
 * An exception indicating there was a problem distributing a file or action.
 */
public class DistributionException extends Exception {

  /**
   * Creates a new distribution exception.
   *
   * @param message the detail message
   * @param cause the cause
   */
  public DistributionException(String message, Throwable cause) {
    super(message, cause);
  }
}

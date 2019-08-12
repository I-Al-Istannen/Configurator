package de.ialistannen.configurator.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ProcessUtils {

  /**
   * Executes a command and captures the output.
   *
   * @param command the command to run
   * @return the stdout of the program
   */
  public static String runWithShellForOutput(String command) {
    ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", command);
    try {
      Process process = processBuilder.start();
      try (InputStream input = process.getInputStream();
          InputStreamReader inputStreamReader = new InputStreamReader(input);
          BufferedReader reader = new BufferedReader(inputStreamReader)) {

        List<String> result = new ArrayList<>();
        String tmp;
        while ((tmp = reader.readLine()) != null) {
          result.add(tmp);
        }
        String output = String.join(System.lineSeparator(), result);

        int exitCode = process.waitFor();
        if (exitCode != 0) {
          return "Process exited with exit code " + exitCode + "!";
        }

        return output;
      }
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}

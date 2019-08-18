package de.ialistannen.configurator.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
      try (InputStream input = process.getInputStream()) {
        String output = readOutput(input);

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

  private static String readOutput(InputStream inputStream) throws IOException {
    try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(inputStreamReader)) {
      List<String> result = new ArrayList<>();
      String tmp;
      while ((tmp = reader.readLine()) != null) {
        result.add(tmp);
      }
      return String.join(System.lineSeparator(), result);
    }
  }

  /**
   * Executes a command as a file.
   *
   * @param command the command to run
   * @return the standard out of the process
   */
  public static String runAsFileWithShell(String command) {
    try (ExecutableTempFile temp = new ExecutableTempFile("execute-file", ".sh")) {
      Files.write(temp.file, command.getBytes(StandardCharsets.UTF_8));
      ProcessBuilder builder = new ProcessBuilder(temp.file.toAbsolutePath().toString());
      Process process = builder.start();

      String output = readOutput(process.getInputStream());

      int exitCode = process.waitFor();

      if (exitCode != 0) {
        throw new RuntimeException("Process exited with exit code " + exitCode + "!");
      }
      return output;
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private static class ExecutableTempFile implements AutoCloseable {

    private Path file;

    ExecutableTempFile(String prefix, String suffix) throws IOException {
      file = Files.createTempFile(prefix, suffix);
      Set<PosixFilePermission> permissions = EnumSet.copyOf(Files.getPosixFilePermissions(file));
      permissions.add(PosixFilePermission.OWNER_EXECUTE);
      Files.setPosixFilePermissions(file, permissions);
    }

    @Override
    public void close() throws IOException {
      Files.deleteIfExists(file);
    }
  }
}

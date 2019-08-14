package de.ialistannen.configurator.config;

import de.ialistannen.configurator.phases.Phase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.ToString;
import org.yaml.snakeyaml.Yaml;

@Data
@ToString
public class Config {

  private final List<String> phases;

  /**
   * Returns the names of all phases.
   *
   * @return the names of all phases
   */
  public List<String> getPhases() {
    return Collections.unmodifiableList(phases);
  }

  /**
   * Returns all phases.
   *
   * @return the phases
   */
  public List<Phase> getPhasesAsObject() {
    List<Phase> phases = new ArrayList<>();

    List<String> phaseNames = getPhases();
    for (int i = 0; i < phaseNames.size(); i++) {
      String phaseName = phaseNames.get(i);
      phases.add(new SimplePhase(phaseName, i));
    }

    return phases;
  }

  /**
   * Loads the config.
   *
   * @param content the content
   * @return the config
   */
  public static Config loadConfig(String content) {
    Map<String, Object> map = new Yaml().load(content);
    @SuppressWarnings("unchecked")
    List<String> phases = (List<String>) map.get("phases");
    return new Config(phases);
  }

  @Data
  private static class SimplePhase implements Phase {

    private final String identifier;
    private final int priority;

    @Override
    public String identifier() {
      return identifier;
    }

    @Override
    public int priority() {
      return priority;
    }
  }
}

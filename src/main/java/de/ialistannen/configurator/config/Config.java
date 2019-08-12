package de.ialistannen.configurator.config;

import de.ialistannen.configurator.phases.Phase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

@Data
@ToString
public class Config {

  private List<String> phases;

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
    Constructor constructor = new Constructor(Config.class);
    TypeDescription typeDescription = new TypeDescription(Config.class);
    typeDescription.addPropertyParameters("phases", String.class);
    return new Yaml(constructor).load(content);
  }

  @ToString
  @EqualsAndHashCode
  @AllArgsConstructor
  private static class SimplePhase implements Phase {

    private String identifier;
    private int priority;

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

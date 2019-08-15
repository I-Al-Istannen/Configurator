package de.ialistannen.configurator.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.assertj.core.api.InstanceOfAssertFactories.map;

import de.ialistannen.configurator.context.Action;
import de.ialistannen.configurator.context.PhaseContext;
import de.ialistannen.configurator.context.RenderContext;
import de.ialistannen.configurator.util.ParseException;
import java.util.AbstractMap.SimpleEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StringRenderTargetTest {

  @Test
  public void parseSingleAssignment() throws ParseException {
    String input = getPrefix()
        + "# foo = 20";
    assertThat(getResult(new PhaseContext(), input))
        .isEqualTo("");

    assertThat(getContext(new PhaseContext(), input))
        .extracting(RenderContext::getAllValues)
        .asInstanceOf(map(String.class, Object.class))
        .containsExactly(new SimpleEntry<>("foo", "20"));
  }

  @Test
  public void parseMultipleAssignments() throws ParseException {
    String input = getPrefix()
        + "# foo = 20\n"
        + "# bar = foo";
    assertThat(getResult(new PhaseContext(), input))
        .isEqualTo("");

    assertThat(getContext(new PhaseContext(), input))
        .extracting(RenderContext::getAllValues)
        .asInstanceOf(map(String.class, Object.class))
        .containsEntry("foo", "20")
        .containsEntry("bar", "foo")
        .hasSize(2);
  }

  @Test
  public void parseMultipleAssignmentsWithVariables() throws ParseException {
    String input = getPrefix()
        + "# foo = 20\n"
        + "# bar = {{$foo}}";
    assertThat(getResult(new PhaseContext(), input))
        .isEqualTo("");

    assertThat(getContext(new PhaseContext(), input))
        .extracting(RenderContext::getAllValues)
        .asInstanceOf(map(String.class, Object.class))
        .containsEntry("foo", "20")
        .containsEntry("bar", "20")
        .hasSize(2);
  }

  @Test
  public void parseMultipleAssignmentsWithVariablesAndLiteral() throws ParseException {
    String input = getPrefix()
        + "# foo = 20\n"
        + "# bar = {{$foo}}!";
    assertThat(getResult(new PhaseContext(), input))
        .isEqualTo("");

    assertThat(getContext(new PhaseContext(), input))
        .extracting(RenderContext::getAllValues)
        .asInstanceOf(map(String.class, Object.class))
        .containsEntry("foo", "20")
        .containsEntry("bar", "20!")
        .hasSize(2);
  }

  @Test
  public void interpolateVariableOwnLine() throws ParseException {
    String input = getPrefix()
        + "# foo = 20\n"
        + "{{$foo}}!";
    assertThat(getResult(new PhaseContext(), input))
        .isEqualTo("20!");

    assertThat(getContext(new PhaseContext(), input))
        .extracting(RenderContext::getAllValues)
        .asInstanceOf(map(String.class, Object.class))
        .containsEntry("foo", "20")
        .hasSize(1);
  }

  @Test
  public void interpolateVariableInsideLiterals() throws ParseException {
    String input = getPrefix()
        + "# foo = 20\n"
        + "Hey, {{$foo}}! is my message";
    assertThat(getResult(new PhaseContext(), input))
        .isEqualTo("Hey, 20! is my message");

    assertThat(getContext(new PhaseContext(), input))
        .extracting(RenderContext::getAllValues)
        .asInstanceOf(map(String.class, Object.class))
        .containsEntry("foo", "20")
        .hasSize(1);
  }

  @Test
  public void interpolateShellInsideLiterals() throws ParseException {
    String input = getPrefix()
        + "Hey, {{!echo 20}}! is my message";
    assertThat(getResult(new PhaseContext(), input))
        .isEqualTo("Hey, 20! is my message");

    assertThat(getContext(new PhaseContext(), input))
        .extracting(RenderContext::getAllValues)
        .asInstanceOf(map(String.class, Object.class))
        .isEmpty();
  }

  @Test
  public void pipingShell() throws ParseException {
    String input = getPrefix()
        + "Hey, {{!echo -e \"Hello\\n world\" | sed \"s/Hello/whatever/\" | sed \"s/whatever/Hello/\" | tr '\\n' \" \" | sed \"s/ world /world\\n/\"}}! is my message";
    assertThat(getResult(new PhaseContext(), input))
        .isEqualTo("Hey, Hello world! is my message");

    assertThat(getContext(new PhaseContext(), input))
        .extracting(RenderContext::getAllValues)
        .asInstanceOf(map(String.class, Object.class))
        .isEmpty();
  }

  @Test
  public void actionAdded() throws ParseException {
    String input = getPrefix()
        + "# action Test me!\n"
        + "am content 'ä'\n"
        + "# end action";
    assertThat(getResult(new PhaseContext(), input))
        .isEqualTo("");

    assertThat(getContext(new PhaseContext(), input))
        .extracting(RenderContext::getAllValues)
        .asInstanceOf(map(String.class, Object.class))
        .isEmpty();
    assertThat(getContext(new PhaseContext(), input))
        .extracting(RenderContext::getAllActions)
        .asInstanceOf(list(Action.class))
        .containsExactly(new Action("Test me!", "am content 'ä'\n"));
  }

  @Test
  public void jsScriptRun() throws ParseException {
    String input = getPrefix()
        + "# script js\n"
        + "context.storeValue(\"Hey\", 20)\n"
        + "# end script";
    assertThat(getResult(new PhaseContext(), input))
        .isEqualTo("");

    assertThat(getContext(new PhaseContext(), input))
        .extracting(RenderContext::getAllValues)
        .asInstanceOf(map(String.class, Object.class))
        .containsEntry("Hey", 20)
        .hasSize(1);
    assertThat(getContext(new PhaseContext(), input))
        .extracting(RenderContext::getAllActions)
        .asInstanceOf(list(Action.class))
        .isEmpty();
  }

  @ParameterizedTest(name = "\"{0}\" should be {1}")
  @CsvSource({
      "(hey) == (hey), true",
      "(hey) == (Hey), true",
      "(hey) == (heys), false",
      "(hey) === (hey), true",
      "(hey) === (Hey), false",
      "(hey) === (heys), false",
      "(hey) != (hey), false",
      "(hey) != (Hey), true",
      "(hey) != (heys), true",
      "(5) > (5), false",
      "(5) > (6), false",
      "(5) > (4), true",
      "(5) < (5), false",
      "(5) < (6), true",
      "(5) < (4), false",
      "(true)  || (true), true",
      "(true)  || (false), true",
      "(false) || (true), true",
      "(false) || (false), false",
      "(true)  && (true), true",
      "(true)  && (false), false",
      "(false) && (true), false",
      "(false) && (false), false",
  })
  public void simpleStringComparison(String condition, boolean expected) throws ParseException {
    assertIfIsTrueOrFalse(condition, expected);
  }

  private void assertIfIsTrueOrFalse(String ifCondition, boolean expectedTrue)
      throws ParseException {
    String input = getPrefix()
        + "# if " + ifCondition + "\n"
        + "true\n"
        + "# end if";
    assertThat(getResult(new PhaseContext(), input))
        .isEqualTo(expectedTrue ? "true" : "");

    assertThat(getContext(new PhaseContext(), input))
        .extracting(RenderContext::getAllValues)
        .asInstanceOf(map(String.class, Object.class))
        .hasSize(0);
    assertThat(getContext(new PhaseContext(), input))
        .extracting(RenderContext::getAllActions)
        .asInstanceOf(list(Action.class))
        .isEmpty();
  }

  @Test
  public void multiStatementIf() throws ParseException {
    String input = getPrefix()
        + "# if (true) == (true)\n"
        + "true\n"
        + "# a = b"
        + "# end if";
    assertThat(getResult(new PhaseContext(), input))
        .isEqualTo("true");

    assertThat(getContext(new PhaseContext(), input))
        .extracting(RenderContext::getAllValues)
        .asInstanceOf(map(String.class, Object.class))
        .containsExactly(new SimpleEntry<>("a", "b"));
    assertThat(getContext(new PhaseContext(), input))
        .extracting(RenderContext::getAllActions)
        .asInstanceOf(list(Action.class))
        .isEmpty();
  }

  private String getResult(RenderContext context, String input) throws ParseException {
    return new StringRenderTarget(input).render(context).getFirst().asString();
  }

  private RenderContext getContext(RenderContext context, String input) throws ParseException {
    return new StringRenderTarget(input).render(context).getSecond();
  }

  private String getPrefix() {
    return "Command prefix: #\n";
  }
}
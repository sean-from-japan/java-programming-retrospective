package io.github.seanfromjapan.javaretro.weights;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CatalogueParserTest {

  private final CatalogueParser parser = new CatalogueParser();

  private CatalogueParser.Result parse(String text) throws IOException {
    return parser.parse(new StringReader(text));
  }

  @Test
  @DisplayName("name and factor on one line")
  void readsAPairOnOneLine() throws IOException {
    CatalogueParser.Result result = parse("Titan 0.14\n");
    assertTrue(result.isClean());
    assertEquals(List.of(new Body("Titan", 0.14)), result.bodies());
  }

  @Test
  @DisplayName("name on one line, factor on the next")
  void readsAPairSplitOverTwoLines() throws IOException {
    CatalogueParser.Result result = parse("Europa\n0.134\n");
    assertTrue(result.isClean());
    assertEquals("Europa", result.bodies().get(0).name());
    assertEquals(0.134, result.bodies().get(0).gravityFactor(), 1e-9);
  }

  @Test
  @DisplayName("a factor written into a sentence")
  void readsASentence() throws IOException {
    CatalogueParser.Result result = parse("The gravity of Ceres is 0.029 relative to Earth\n");
    assertTrue(result.isClean(), result.problems().toString());
    assertEquals("Ceres", result.bodies().get(0).name());
    assertEquals(0.029, result.bodies().get(0).gravityFactor(), 1e-9);
  }

  @Test
  @DisplayName("a body name of two words survives")
  void readsAMultiWordName() throws IOException {
    CatalogueParser.Result result = parse("The gravity of Alpha Centauri Bb is 1.4\n");
    assertEquals("Alpha Centauri Bb", result.bodies().get(0).name());
  }

  @Test
  void ignoresBlankLinesAndComments() throws IOException {
    CatalogueParser.Result result = parse("\n# a note\n\nTitan 0.14\n\n");
    assertTrue(result.isClean());
    assertEquals(1, result.bodies().size());
  }

  @Test
  @DisplayName("a name with no factor is reported, not dropped")
  void reportsANameWithoutAFactor() throws IOException {
    CatalogueParser.Result result = parse("Mimas approximately\n");
    assertEquals(0, result.bodies().size());
    assertEquals(1, result.problems().size());
  }

  @Test
  @DisplayName("a trailing name at end of file is reported")
  void reportsATrailingName() throws IOException {
    CatalogueParser.Result result = parse("Titan 0.14\nEuropa\n");
    assertEquals(1, result.bodies().size());
    assertEquals(1, result.problems().size());
    assertTrue(result.problems().get(0).reason().contains("no gravity factor"));
  }

  @Test
  @DisplayName("a pending name followed by another name reports the first")
  void reportsANameFollowedByAnotherName() throws IOException {
    CatalogueParser.Result result = parse("Europa\nTitan 0.14\n");
    assertEquals(List.of(new Body("Titan", 0.14)), result.bodies());
    assertEquals(1, result.problems().size());
    assertEquals(1, result.problems().get(0).lineNumber());
  }

  @Test
  @DisplayName("a zero or negative gravity factor is rejected")
  void rejectsANonPositiveFactor() throws IOException {
    CatalogueParser.Result result = parse("Nowhere 0\nAntiworld -1.2\n");
    assertEquals(0, result.bodies().size());
    assertEquals(2, result.problems().size());
  }

  @Test
  @DisplayName("problems carry the line number they came from")
  void problemsKeepTheirLineNumber() throws IOException {
    CatalogueParser.Result result = parse("Titan 0.14\n???\n");
    assertEquals(2, result.problems().get(0).lineNumber());
  }

  @Test
  @DisplayName("one bad line does not stop the rest of the file")
  void keepsGoingAfterABadLine() throws IOException {
    CatalogueParser.Result result = parse("???\nTitan 0.14\nGanymede 0.15\n");
    assertEquals(2, result.bodies().size());
    assertEquals(1, result.problems().size());
  }

  @Test
  void normalisesTheCapitalisationOfNames() throws IOException {
    assertEquals("Titan", parse("titan 0.14\n").bodies().get(0).name());
  }
}

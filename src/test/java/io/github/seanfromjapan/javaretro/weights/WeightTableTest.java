package io.github.seanfromjapan.javaretro.weights;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class WeightTableTest {

  @Test
  void earthIsAlwaysTheFirstRow() {
    String table = new WeightTable(3).render(100, List.of(new Body("Titan", 0.14)));
    String[] lines = table.split("\n");
    assertTrue(lines[2].startsWith("Earth"), table);
  }

  @Test
  void earthIsNotRepeatedWhenTheCatalogueAlsoListsIt() {
    String table = new WeightTable(3).render(100, List.of(new Body("earth", 1.0)));
    assertEquals(1, table.lines().filter(line -> line.toLowerCase().startsWith("earth")).count());
  }

  @Test
  void columnsAreWideEnoughForTheLongestName() {
    String table = new WeightTable(3).render(100, List.of(new Body("Alpha Centauri Bb", 1.4)));
    for (String line : table.split("\n")) {
      assertTrue(line.contains("  "), line);
    }
    assertTrue(table.contains("Alpha Centauri Bb"));
  }

  @Test
  void theRequestedNumberOfDecimalsIsUsed() {
    assertTrue(new WeightTable(1).render(100, List.of()).contains("45.4"));
    assertTrue(new WeightTable(5).render(100, List.of()).contains("45.35924"));
  }

  @Test
  void anImpossibleDecimalCountIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new WeightTable(-1));
  }

  @Test
  void aDecimalPointIsUsedRegardlessOfTheSystemLocale() {
    // The default locale of a machine in Europe would otherwise print a comma.
    assertTrue(new WeightTable(3).render(100, List.of()).contains("45.359"));
  }
}

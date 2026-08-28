package io.github.seanfromjapan.javaretro.pixelart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MaterialRulesTest {

  private static final String STANDARD = "rules = 13:FLAG, 5:SAIL, 3:HULL, odd:MAST\ndefault = WATER\n";

  private static MaterialRules load(String text) throws IOException {
    return MaterialRules.load(new StringReader(text));
  }

  @Test
  @DisplayName("the first matching rule wins")
  void firstMatchWins() throws IOException {
    MaterialRules rules = load(STANDARD);
    // 'A' is 65: divisible by both 13 and 5, and the flag rule comes first.
    assertEquals(Material.FLAG, rules.materialOf('A'));
  }

  @Test
  @DisplayName("reordering the rules changes the picture")
  void ruleOrderIsSignificant() throws IOException {
    MaterialRules flagFirst = load(STANDARD);
    MaterialRules sailFirst = load("rules = 5:SAIL, 13:FLAG, 3:HULL, odd:MAST\ndefault = WATER\n");
    assertNotEquals(flagFirst.materialOf('A'), sailFirst.materialOf('A'));
    assertEquals(Material.SAIL, sailFirst.materialOf('A'));
  }

  @Test
  void divisibilityRulesSelectTheirMaterial() throws IOException {
    MaterialRules rules = load(STANDARD);
    assertEquals(Material.SAIL, rules.materialOf('F')); // 70
    assertEquals(Material.HULL, rules.materialOf('H')); // 72
  }

  @Test
  void anOddCharacterThatMatchesNoDivisorIsAMast() throws IOException {
    assertEquals(Material.MAST, load(STANDARD).materialOf('w')); // 119
  }

  @Test
  void anythingElseFallsBackToTheDefault() throws IOException {
    assertEquals(Material.WATER, load(STANDARD).materialOf('z')); // 122
  }

  @Test
  @DisplayName("every material is reachable, so any picture can be encoded")
  void everyMaterialHasAnAlphabet() throws IOException {
    load(STANDARD).alphabetSizes().forEach((material, size) -> assertTrue(size > 0, material.name()));
  }

  @Test
  @DisplayName("rules that can never produce a material are rejected at load time")
  void unreachableMaterialIsRejected() {
    assertThrows(IllegalArgumentException.class,
        () -> load("rules = 1:WATER\ndefault = WATER\n"));
  }

  @Test
  void aRuleFileMissingItsDefaultIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> load("rules = 13:FLAG\n"));
  }

  @Test
  void aMalformedRuleIsRejected() {
    assertThrows(IllegalArgumentException.class,
        () -> load("rules = 13-FLAG\ndefault = WATER\n"));
  }

  @Test
  void anUnknownMaterialNameIsRejected() {
    assertThrows(IllegalArgumentException.class,
        () -> load("rules = 13:CLOUD\ndefault = WATER\n"));
  }
}

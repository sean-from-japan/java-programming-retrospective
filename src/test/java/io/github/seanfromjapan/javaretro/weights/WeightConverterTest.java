package io.github.seanfromjapan.javaretro.weights;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class WeightConverterTest {

  @Test
  void oneHundredPoundsIsTheDefinedNumberOfKilograms() {
    assertEquals(45.359237, WeightConverter.poundsToKilograms(100), 1e-9);
  }

  @Test
  void zeroConvertsToZero() {
    assertEquals(0.0, WeightConverter.poundsToKilograms(0), 0.0);
  }

  @Test
  void weightOnEarthEqualsTheMassInKilograms() {
    assertEquals(45.359237, WeightConverter.weightOn(100, new Body("Earth", 1.0)), 1e-9);
  }

  @Test
  void weightScalesWithTheGravityFactor() {
    assertEquals(45.359237 * 0.14, WeightConverter.weightOn(100, new Body("Titan", 0.14)), 1e-9);
  }

  @Test
  void aNegativeMassIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> WeightConverter.poundsToKilograms(-1));
  }

  @Test
  void aBodyWithoutANameIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Body("   ", 1.0));
  }

  @Test
  void aNonFiniteGravityFactorIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Body("Nan", Double.NaN));
  }
}

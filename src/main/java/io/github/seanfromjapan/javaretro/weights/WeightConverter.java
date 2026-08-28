package io.github.seanfromjapan.javaretro.weights;

/** Converts a mass in pounds to kilograms, and to the weight felt on another body. */
public final class WeightConverter {

  /** The international avoirdupois pound, exactly, as defined in 1959. */
  public static final double POUND_IN_KILOGRAMS = 0.45359237;

  private WeightConverter() {}

  public static double poundsToKilograms(double pounds) {
    if (pounds < 0) {
      throw new IllegalArgumentException("mass must not be negative: " + pounds);
    }
    return pounds * POUND_IN_KILOGRAMS;
  }

  /**
   * The weight a mass of {@code pounds} would register on a scale on {@code body}, expressed in
   * kilograms-force. Mass does not change with gravity; the reading on a spring scale does, and
   * that is what the original exercise was asking for.
   */
  public static double weightOn(double pounds, Body body) {
    return poundsToKilograms(pounds) * body.gravityFactor();
  }
}

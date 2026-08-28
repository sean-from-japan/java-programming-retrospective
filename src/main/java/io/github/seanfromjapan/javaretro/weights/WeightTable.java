package io.github.seanfromjapan.javaretro.weights;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Formats the conversion table.
 *
 * <p>Formatting is separated from computation and from input so that the table can be asserted on
 * in a test. The coursework version interleaved all three, which is why it had no tests.
 */
public final class WeightTable {

  private static final String EARTH = "Earth";
  private final int decimals;

  public WeightTable(int decimals) {
    if (decimals < 0 || decimals > 10) {
      throw new IllegalArgumentException("decimals out of range: " + decimals);
    }
    this.decimals = decimals;
  }

  public String render(double pounds, List<Body> bodies) {
    List<Body> rows = new ArrayList<>();
    rows.add(new Body(EARTH, 1.0));
    for (Body body : bodies) {
      if (!body.name().equalsIgnoreCase(EARTH)) {
        rows.add(body);
      }
    }

    int nameWidth = "Body".length();
    for (Body body : rows) {
      nameWidth = Math.max(nameWidth, body.name().length());
    }

    String numberFormat = "%." + decimals + "f";
    StringBuilder out = new StringBuilder();
    out.append(String.format(Locale.ROOT, "%-" + nameWidth + "s  %8s  %12s%n", "Body", "Gravity",
        "Weight (kg)"));
    out.append(String.format(Locale.ROOT, "%s  %s  %s%n", "-".repeat(nameWidth), "-".repeat(8),
        "-".repeat(12)));
    for (Body body : rows) {
      out.append(String.format(Locale.ROOT, "%-" + nameWidth + "s  %8.3f  %12s%n", body.name(),
          body.gravityFactor(),
          String.format(Locale.ROOT, numberFormat, WeightConverter.weightOn(pounds, body))));
    }
    return out.toString();
  }
}

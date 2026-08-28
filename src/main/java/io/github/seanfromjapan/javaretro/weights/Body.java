package io.github.seanfromjapan.javaretro.weights;

import java.util.Objects;

/** A body with a surface gravity expressed as a multiple of Earth's. */
public final class Body {

  private final String name;
  private final double gravityFactor;

  public Body(String name, double gravityFactor) {
    this.name = Objects.requireNonNull(name, "name").trim();
    if (this.name.isEmpty()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    if (!Double.isFinite(gravityFactor) || gravityFactor <= 0) {
      throw new IllegalArgumentException(
          "gravity factor must be a positive finite number, got " + gravityFactor);
    }
    this.gravityFactor = gravityFactor;
  }

  public String name() {
    return name;
  }

  public double gravityFactor() {
    return gravityFactor;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Body)) {
      return false;
    }
    Body body = (Body) other;
    return Double.compare(body.gravityFactor, gravityFactor) == 0 && name.equals(body.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, gravityFactor);
  }

  @Override
  public String toString() {
    return name + " (x" + gravityFactor + ")";
  }
}

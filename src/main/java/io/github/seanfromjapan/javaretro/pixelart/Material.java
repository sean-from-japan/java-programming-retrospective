package io.github.seanfromjapan.javaretro.pixelart;

/** What a cell of the grid represents, and the colour it is drawn in. */
public enum Material {
  WATER('.', 0x00000000, true),
  HULL('H', 0xFF8B4A2B, false),
  SAIL('S', 0xFFF2EFE4, false),
  MAST('M', 0xFF5A3A1E, false),
  FLAG('F', 0xFFD1495B, false);

  private final char symbol;
  private final int argb;
  private final boolean transparent;

  Material(char symbol, int argb, boolean transparent) {
    this.symbol = symbol;
    this.argb = argb;
    this.transparent = transparent;
  }

  public char symbol() {
    return symbol;
  }

  public int argb() {
    return argb;
  }

  /** Transparent materials are skipped when drawing, so the scene shows through. */
  public boolean isTransparent() {
    return transparent;
  }

  public static Material fromSymbol(char symbol) {
    for (Material material : values()) {
      if (material.symbol == symbol) {
        return material;
      }
    }
    throw new IllegalArgumentException("no material for symbol '" + symbol + "'");
  }
}

package io.github.seanfromjapan.javaretro.pixelart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A rectangular grid of materials.
 *
 * <p>The grid is validated on construction. The coursework version read a fixed 130 by 130 block of
 * characters with a nested loop and no checks at all, so a data file one character short produced a
 * silently skewed image rather than an error.
 */
public final class Sprite {

  private final int width;
  private final int height;
  private final Material[][] cells;

  public Sprite(Material[][] cells) {
    if (cells.length == 0 || cells[0].length == 0) {
      throw new IllegalArgumentException("sprite must not be empty");
    }
    this.height = cells.length;
    this.width = cells[0].length;
    this.cells = new Material[height][];
    for (int row = 0; row < height; row++) {
      if (cells[row].length != width) {
        throw new IllegalArgumentException(
            "row " + (row + 1) + " has " + cells[row].length + " cells, expected " + width);
      }
      this.cells[row] = cells[row].clone();
    }
  }

  public int width() {
    return width;
  }

  public int height() {
    return height;
  }

  /** Row 0 is the top row, as it appears in the data file. */
  public Material at(int row, int column) {
    return cells[row][column];
  }

  /** A left-right mirrored copy, for a second sprite facing the other way. */
  public Sprite mirrored() {
    Material[][] flipped = new Material[height][width];
    for (int row = 0; row < height; row++) {
      for (int column = 0; column < width; column++) {
        flipped[row][column] = cells[row][width - 1 - column];
      }
    }
    return new Sprite(flipped);
  }

  /** Renders back to the plain symbol form, which makes round-trip testing possible. */
  public String toSymbolMap() {
    StringBuilder out = new StringBuilder();
    for (Material[] row : cells) {
      for (Material material : row) {
        out.append(material.symbol());
      }
      out.append('\n');
    }
    return out.toString();
  }

  /** Reads the plain form: one symbol per cell. */
  public static Sprite fromSymbolMap(Reader source) throws IOException {
    List<Material[]> rows = new ArrayList<>();
    int lineNumber = 0;
    try (BufferedReader reader = new BufferedReader(source)) {
      String line;
      while ((line = reader.readLine()) != null) {
        lineNumber++;
        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }
        Material[] row = new Material[line.length()];
        for (int column = 0; column < line.length(); column++) {
          try {
            row[column] = Material.fromSymbol(line.charAt(column));
          } catch (IllegalArgumentException unknown) {
            throw new IllegalArgumentException(
                "line " + lineNumber + ", column " + (column + 1) + ": " + unknown.getMessage());
          }
        }
        rows.add(row);
      }
    }
    return new Sprite(rows.toArray(new Material[0][]));
  }

  /**
   * Reads the encoded form, decoding each character through the rules.
   *
   * <p>Unlike the symbol map, an encoded file has no comment syntax: every printable character is
   * data, so a leading {@code #} is a material and not a remark.
   */
  public static Sprite decode(Reader source, MaterialRules rules) throws IOException {
    List<Material[]> rows = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(source)) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isEmpty()) {
          continue;
        }
        Material[] row = new Material[line.length()];
        for (int column = 0; column < line.length(); column++) {
          row[column] = rules.materialOf(line.charAt(column));
        }
        rows.add(row);
      }
    }
    return new Sprite(rows.toArray(new Material[0][]));
  }

  public static Sprite decode(Path path, MaterialRules rules) throws IOException {
    try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      return decode(reader, rules);
    }
  }
}

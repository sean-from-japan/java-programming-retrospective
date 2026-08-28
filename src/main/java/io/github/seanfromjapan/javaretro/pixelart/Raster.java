package io.github.seanfromjapan.javaretro.pixelart;

import java.util.Arrays;

/**
 * A plain ARGB pixel buffer.
 *
 * <p>Drawing is separated from any display or file format so the scene can be asserted on
 * pixel by pixel in a test. The coursework version drew straight into a windowing library's canvas,
 * which meant nothing about the picture could be checked without a human looking at a window.
 */
public final class Raster {

  private final int width;
  private final int height;
  private final int[] pixels;

  public Raster(int width, int height) {
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("raster must be at least 1x1, got " + width + "x" + height);
    }
    this.width = width;
    this.height = height;
    this.pixels = new int[width * height];
  }

  public int width() {
    return width;
  }

  public int height() {
    return height;
  }

  public int[] pixels() {
    return pixels.clone();
  }

  public int get(int x, int y) {
    requireInside(x, y);
    return pixels[y * width + x];
  }

  public void set(int x, int y, int argb) {
    if (x >= 0 && y >= 0 && x < width && y < height) {
      pixels[y * width + x] = argb;
    }
  }

  public void fill(int argb) {
    Arrays.fill(pixels, argb);
  }

  /** Fills a rectangle, clipped to the raster. Coordinates count down from the top. */
  public void fillRect(int x, int y, int rectWidth, int rectHeight, int argb) {
    for (int row = Math.max(0, y); row < Math.min(height, y + rectHeight); row++) {
      for (int column = Math.max(0, x); column < Math.min(width, x + rectWidth); column++) {
        pixels[row * width + column] = argb;
      }
    }
  }

  private void requireInside(int x, int y) {
    if (x < 0 || y < 0 || x >= width || y >= height) {
      throw new IndexOutOfBoundsException("(" + x + ", " + y + ") outside " + width + "x" + height);
    }
  }
}

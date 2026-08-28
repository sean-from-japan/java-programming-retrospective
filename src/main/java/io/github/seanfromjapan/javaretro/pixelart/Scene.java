package io.github.seanfromjapan.javaretro.pixelart;

/** Where the horizon sits and what colour each band is. */
public final class Scene {

  private final int width;
  private final int height;
  private final int skyHeight;
  private final int shoreHeight;
  private final int skyColour;
  private final int shoreColour;
  private final int waterColour;

  public Scene(int width, int height, int skyHeight, int shoreHeight, int skyColour,
      int shoreColour, int waterColour) {
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("scene must be at least 1x1");
    }
    if (skyHeight < 0 || shoreHeight < 0 || skyHeight + shoreHeight > height) {
      throw new IllegalArgumentException(
          "sky (" + skyHeight + ") and shore (" + shoreHeight + ") do not fit in " + height);
    }
    this.width = width;
    this.height = height;
    this.skyHeight = skyHeight;
    this.shoreHeight = shoreHeight;
    this.skyColour = skyColour;
    this.shoreColour = shoreColour;
    this.waterColour = waterColour;
  }

  /** A scene split into a quarter of sky, a quarter of shore and a half of water. */
  public static Scene standard(int width, int height) {
    return new Scene(width, height, height / 4, height / 4, 0xFF9CC3D5, 0xFF6E8B4C, 0xFF1F5673);
  }

  public int width() {
    return width;
  }

  public int height() {
    return height;
  }

  /** The y coordinate of the waterline, counting down from the top. */
  public int waterlineY() {
    return skyHeight + shoreHeight;
  }

  public Raster paintBackground() {
    Raster raster = new Raster(width, height);
    raster.fillRect(0, 0, width, skyHeight, skyColour);
    raster.fillRect(0, skyHeight, width, shoreHeight, shoreColour);
    raster.fillRect(0, waterlineY(), width, height - waterlineY(), waterColour);
    return raster;
  }
}

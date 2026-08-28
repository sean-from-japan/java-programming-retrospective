package io.github.seanfromjapan.javaretro.pixelart;

/** Draws sprites onto a scene at an integer scale, optionally submerged. */
public final class SceneRenderer {

  private final Scene scene;

  public SceneRenderer(Scene scene) {
    this.scene = scene;
  }

  public Scene scene() {
    return scene;
  }

  /**
   * Draws {@code sprite} with its top-left corner at ({@code x}, {@code y}).
   *
   * @param scale how many pixels wide each grid cell becomes
   * @param submerge if true, the part of the hull below the waterline is not drawn, so the boat
   *     sits in the water rather than on top of it
   */
  public void draw(Raster raster, Sprite sprite, int x, int y, int scale, boolean submerge) {
    if (scale < 1) {
      throw new IllegalArgumentException("scale must be at least 1, got " + scale);
    }
    int waterline = scene.waterlineY();
    for (int row = 0; row < sprite.height(); row++) {
      for (int column = 0; column < sprite.width(); column++) {
        Material material = sprite.at(row, column);
        if (material.isTransparent()) {
          continue;
        }
        int top = y + row * scale;
        if (submerge && material == Material.HULL && top >= waterline) {
          continue;
        }
        raster.fillRect(x + column * scale, top, scale, scale, material.argb());
      }
    }
  }
}

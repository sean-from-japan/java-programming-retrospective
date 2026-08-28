package io.github.seanfromjapan.javaretro.pixelart;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

/**
 * Writes a raster out as a PNG.
 *
 * <p>This is the only class that touches an image library, and it holds no drawing logic, so every
 * other class in the package can be tested without a display or a temporary file.
 */
public final class PngWriter {

  private PngWriter() {}

  public static void write(Raster raster, OutputStream out) throws IOException {
    BufferedImage image =
        new BufferedImage(raster.width(), raster.height(), BufferedImage.TYPE_INT_ARGB);
    image.setRGB(0, 0, raster.width(), raster.height(), raster.pixels(), 0, raster.width());
    if (!ImageIO.write(image, "png", out)) {
      throw new IOException("no PNG writer is available in this Java runtime");
    }
  }

  public static void write(Raster raster, Path path) throws IOException {
    Path parent = path.toAbsolutePath().getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    try (OutputStream out = Files.newOutputStream(path)) {
      write(raster, out);
    }
  }
}

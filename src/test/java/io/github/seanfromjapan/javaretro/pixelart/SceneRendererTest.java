package io.github.seanfromjapan.javaretro.pixelart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SceneRendererTest {

  private static Sprite sprite(String map) throws IOException {
    return Sprite.fromSymbolMap(new StringReader(map));
  }

  @Test
  void theBackgroundIsThreeBands() {
    Scene scene = Scene.standard(40, 40);
    Raster raster = scene.paintBackground();
    assertEquals(20, scene.waterlineY());
    assertNotEquals(raster.get(0, 0), raster.get(0, 15));
    assertNotEquals(raster.get(0, 15), raster.get(0, 30));
  }

  @Test
  @DisplayName("transparent cells let the background show through")
  void waterCellsAreNotDrawn() throws IOException {
    Scene scene = Scene.standard(40, 40);
    Raster raster = scene.paintBackground();
    int skyBefore = raster.get(1, 1);
    new SceneRenderer(scene).draw(raster, sprite("..\n..\n"), 0, 0, 1, false);
    assertEquals(skyBefore, raster.get(1, 1));
  }

  @Test
  void aCellIsDrawnAsAScaledSquare() throws IOException {
    Scene scene = Scene.standard(40, 40);
    Raster raster = scene.paintBackground();
    new SceneRenderer(scene).draw(raster, sprite("S\n"), 4, 4, 3, false);
    assertEquals(Material.SAIL.argb(), raster.get(4, 4));
    assertEquals(Material.SAIL.argb(), raster.get(6, 6));
    assertNotEquals(Material.SAIL.argb(), raster.get(7, 7));
  }

  @Test
  @DisplayName("the hull below the waterline is hidden when submerged")
  void submergedHullIsNotDrawn() throws IOException {
    Scene scene = Scene.standard(40, 40); // waterline at y = 20
    Raster raster = scene.paintBackground();
    int waterColour = raster.get(0, 25);
    new SceneRenderer(scene).draw(raster, sprite("H\nH\n"), 0, 19, 1, true);
    assertEquals(Material.HULL.argb(), raster.get(0, 19), "above the waterline");
    assertEquals(waterColour, raster.get(0, 20), "below the waterline");
  }

  @Test
  void thatSameHullIsDrawnWhenNotSubmerged() throws IOException {
    Scene scene = Scene.standard(40, 40);
    Raster raster = scene.paintBackground();
    new SceneRenderer(scene).draw(raster, sprite("H\nH\n"), 0, 19, 1, false);
    assertEquals(Material.HULL.argb(), raster.get(0, 20));
  }

  @Test
  @DisplayName("only the hull is hidden; the mast still shows above it")
  void onlyTheHullIsSubmerged() throws IOException {
    Scene scene = Scene.standard(40, 40);
    Raster raster = scene.paintBackground();
    new SceneRenderer(scene).draw(raster, sprite("M\n"), 0, 25, 1, true);
    assertEquals(Material.MAST.argb(), raster.get(0, 25));
  }

  @Test
  void drawingPartlyOffTheEdgeIsClippedNotAnError() throws IOException {
    Scene scene = Scene.standard(10, 10);
    Raster raster = scene.paintBackground();
    new SceneRenderer(scene).draw(raster, sprite("HH\nHH\n"), -1, -1, 4, false);
    assertEquals(Material.HULL.argb(), raster.get(0, 0));
  }

  @Test
  void aScaleBelowOneIsRejected() throws IOException {
    Scene scene = Scene.standard(10, 10);
    Sprite boat = sprite("H\n");
    Raster raster = scene.paintBackground();
    SceneRenderer renderer = new SceneRenderer(scene);
    assertThrows(IllegalArgumentException.class, () -> renderer.draw(raster, boat, 0, 0, 0, false));
  }

  @Test
  void aSceneWhoseBandsDoNotFitIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Scene(10, 10, 8, 8, 0, 0, 0));
  }

  @Test
  void readingOutsideTheRasterIsAnError() {
    Raster raster = new Raster(4, 4);
    assertThrows(IndexOutOfBoundsException.class, () -> raster.get(4, 0));
  }

  @Test
  void writingOutsideTheRasterIsSilentlyClipped() {
    Raster raster = new Raster(4, 4);
    raster.set(9, 9, 0xFFFFFFFF);
    assertEquals(0, raster.get(3, 3));
  }
}

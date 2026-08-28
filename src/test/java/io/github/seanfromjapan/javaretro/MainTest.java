package io.github.seanfromjapan.javaretro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** End-to-end runs of the command line, including the committed sample data. */
class MainTest {

  private final ByteArrayOutputStream out = new ByteArrayOutputStream();
  private final ByteArrayOutputStream err = new ByteArrayOutputStream();

  private int run(String... args) {
    Main main = new Main(new PrintStream(out, true, StandardCharsets.UTF_8),
        new PrintStream(err, true, StandardCharsets.UTF_8));
    return main.run(args);
  }

  private String stdout() {
    return out.toString(StandardCharsets.UTF_8);
  }

  private String stderr() {
    return err.toString(StandardCharsets.UTF_8);
  }

  @Test
  void noArgumentsPrintsUsage() {
    assertEquals(2, run());
    assertTrue(stderr().contains("usage:"));
  }

  @Test
  void anUnknownCommandIsRejected() {
    assertEquals(2, run("fly"));
    assertTrue(stderr().contains("unknown command"));
  }

  @Test
  @DisplayName("the sample catalogue prints a table and reports its one bad line")
  void weightsOnTheSampleCatalogue() {
    int code = run("weights", "data/gravity.txt", "150");
    assertTrue(stdout().contains("Earth"));
    assertTrue(stdout().contains("Titan"));
    assertTrue(stdout().contains("Ceres"));
    assertTrue(stderr().contains("could not be read"), stderr());
    assertEquals(1, code, "a file with an unreadable line does not exit clean");
  }

  @Test
  void aCleanCatalogueExitsZero(@TempDir Path dir) throws IOException {
    Path catalogue = dir.resolve("clean.txt");
    Files.writeString(catalogue, "Titan 0.14\n", StandardCharsets.UTF_8);
    assertEquals(0, run("weights", catalogue.toString(), "100"));
  }

  @Test
  void aNonNumericMassIsRejected() {
    assertEquals(2, run("weights", "data/gravity.txt", "heavy"));
    assertTrue(stderr().contains("not a number"));
  }

  @Test
  void aMissingCatalogueIsReportedWithoutAStackTrace() {
    assertEquals(2, run("weights", "data/nowhere.txt", "100"));
    assertTrue(stderr().startsWith("error:"));
  }

  @Test
  @DisplayName("the sample sprite renders to a real PNG")
  void sceneWritesAPng(@TempDir Path dir) throws IOException {
    Path png = dir.resolve("scene.png");
    assertEquals(0, run("scene", "data/sailboat.txt", png.toString()));
    byte[] bytes = Files.readAllBytes(png);
    assertTrue(bytes.length > 100);
    assertEquals((byte) 0x89, bytes[0]);
    assertEquals('P', bytes[1]);
    assertEquals('N', bytes[2]);
    assertEquals('G', bytes[3]);
  }

  @Test
  void encodeReproducesTheCommittedEncodedSprite(@TempDir Path dir) throws IOException {
    Path encoded = dir.resolve("sailboat.txt");
    assertEquals(0, run("encode", "data/sailboat.map", encoded.toString()));
    assertEquals(Files.readString(Path.of("data/sailboat.txt"), StandardCharsets.UTF_8),
        Files.readString(encoded, StandardCharsets.UTF_8));
  }
}

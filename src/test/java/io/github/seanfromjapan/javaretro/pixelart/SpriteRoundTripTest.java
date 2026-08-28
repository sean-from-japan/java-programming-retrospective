package io.github.seanfromjapan.javaretro.pixelart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The encoder exists so that the decoder can be checked against a known picture. These tests are
 * the reason the sample data in {@code data/} is reproducible rather than given.
 */
class SpriteRoundTripTest {

  private static MaterialRules rules() throws IOException {
    try (InputStream stream = MaterialRules.class.getResourceAsStream("/material-rules.properties")) {
      return MaterialRules.load(stream);
    }
  }

  @Test
  @DisplayName("encoding then decoding reproduces the picture exactly")
  void roundTripsAnInventedPicture() throws IOException {
    String map = "...F...\n...M...\n..SM...\n.SSM...\nHHHHHHH\n";
    Sprite original = Sprite.fromSymbolMap(new StringReader(map));
    String encoded = new SpriteEncoder(rules()).encode(original);
    Sprite decoded = Sprite.decode(new StringReader(encoded), rules());
    assertEquals(map, decoded.toSymbolMap());
  }

  @Test
  @DisplayName("the encoded form does not look like the picture")
  void encodingIsNotJustTheSymbols() throws IOException {
    String map = "HHHH\nHHHH\n";
    String encoded = new SpriteEncoder(rules()).encode(Sprite.fromSymbolMap(new StringReader(map)));
    assertNotEquals(map, encoded);
    // Consecutive identical cells should not all encode to the same character.
    assertTrue(encoded.chars().distinct().count() > 2, encoded);
  }

  @Test
  @DisplayName("the committed sample data decodes back to the committed map")
  void committedSampleDataIsConsistent() throws IOException {
    Path root = Path.of("data");
    Sprite fromMap;
    try (var reader = Files.newBufferedReader(root.resolve("sailboat.map"), StandardCharsets.UTF_8)) {
      fromMap = Sprite.fromSymbolMap(reader);
    }
    Sprite fromEncoded = Sprite.decode(root.resolve("sailboat.txt"), rules());
    assertEquals(fromMap.toSymbolMap(), fromEncoded.toSymbolMap());
    assertEquals(20, fromMap.width());
    assertEquals(16, fromMap.height());
  }
}

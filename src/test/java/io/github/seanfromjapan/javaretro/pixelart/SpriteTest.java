package io.github.seanfromjapan.javaretro.pixelart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SpriteTest {

  private static Sprite fromMap(String text) throws IOException {
    return Sprite.fromSymbolMap(new StringReader(text));
  }

  @Test
  void readsASymbolMap() throws IOException {
    Sprite sprite = fromMap(".S.\nHHH\n");
    assertEquals(3, sprite.width());
    assertEquals(2, sprite.height());
    assertEquals(Material.SAIL, sprite.at(0, 1));
    assertEquals(Material.HULL, sprite.at(1, 0));
  }

  @Test
  @DisplayName("a ragged grid is rejected with the offending row")
  void raggedRowsAreRejected() {
    IllegalArgumentException failure =
        assertThrows(IllegalArgumentException.class, () -> fromMap(".S.\nHH\n"));
    assertEquals("row 2 has 2 cells, expected 3", failure.getMessage());
  }

  @Test
  void anUnknownSymbolIsRejectedWithItsPosition() {
    IllegalArgumentException failure =
        assertThrows(IllegalArgumentException.class, () -> fromMap(".S.\nHXH\n"));
    assertEquals("line 2, column 2: no material for symbol 'X'", failure.getMessage());
  }

  @Test
  void anEmptySpriteIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> fromMap("# only a comment\n"));
  }

  @Test
  void commentsAndBlankLinesAreIgnoredInASymbolMap() throws IOException {
    assertEquals(1, fromMap("# a note\n\nHHH\n").height());
  }

  @Test
  void mirroringFlipsColumnsAndIsItsOwnInverse() throws IOException {
    Sprite sprite = fromMap("SM.\nHHH\n");
    assertEquals(".MS\nHHH\n", sprite.mirrored().toSymbolMap());
    assertEquals(sprite.toSymbolMap(), sprite.mirrored().mirrored().toSymbolMap());
  }

  @Test
  void aSpriteIsIndependentOfTheArrayItWasBuiltFrom() {
    Material[][] cells = {{Material.HULL, Material.HULL}};
    Sprite sprite = new Sprite(cells);
    cells[0][0] = Material.FLAG;
    assertEquals(Material.HULL, sprite.at(0, 0));
  }
}

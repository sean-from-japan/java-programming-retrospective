package io.github.seanfromjapan.javaretro.pixelart;

import java.util.List;

/**
 * Turns a readable material map back into the encoded form.
 *
 * <p>This direction did not exist in the coursework — the encoded file was handed out and simply
 * consumed. Writing the encoder is what made the decoder testable: encode a known picture, decode
 * it again, and assert the picture survived. It also means the sample data in this repository is
 * mine and is reproducible, rather than a data file I was given.
 *
 * <p>Characters are cycled through every legal option for the material so the output looks like the
 * noise it is meant to look like, while staying deterministic.
 */
public final class SpriteEncoder {

  private final MaterialRules rules;

  public SpriteEncoder(MaterialRules rules) {
    this.rules = rules;
  }

  public String encode(Sprite sprite) {
    StringBuilder out = new StringBuilder();
    int counter = 0;
    for (int row = 0; row < sprite.height(); row++) {
      for (int column = 0; column < sprite.width(); column++) {
        List<Character> alphabet = rules.charactersFor(sprite.at(row, column));
        out.append(alphabet.get(counter % alphabet.size()));
        counter += 7; // a stride coprime with the alphabet sizes, so runs vary
      }
      out.append('\n');
    }
    return out.toString();
  }
}

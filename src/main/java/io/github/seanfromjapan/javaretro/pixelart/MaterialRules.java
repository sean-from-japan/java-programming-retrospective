package io.github.seanfromjapan.javaretro.pixelart;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Decides which material a character in the encoded grid represents.
 *
 * <p>The rules are ordered and the first match wins, which is the whole subtlety: a character
 * divisible by both 13 and 5 is a flag, not a sail, and swapping the two lines silently changes the
 * picture. In the coursework version this order lived in an if/else chain with the divisors written
 * as bare numbers and no explanation, and getting it wrong was the single hardest bug to see.
 * Here the order is data, it is named, and a test asserts that it matters.
 */
public final class MaterialRules {

  /** One rule: a test on the character's code point, and the material it selects. */
  public static final class Rule {
    private final String predicate;
    private final Material material;

    Rule(String predicate, Material material) {
      this.predicate = predicate;
      this.material = material;
    }

    boolean matches(int codePoint) {
      if ("odd".equals(predicate)) {
        return Math.floorMod(codePoint, 2) == 1;
      }
      if ("even".equals(predicate)) {
        return Math.floorMod(codePoint, 2) == 0;
      }
      return Math.floorMod(codePoint, Integer.parseInt(predicate)) == 0;
    }

    public String predicate() {
      return predicate;
    }

    public Material material() {
      return material;
    }

    @Override
    public String toString() {
      return predicate + " -> " + material;
    }
  }

  private final List<Rule> rules;
  private final Material fallback;

  public MaterialRules(List<Rule> rules, Material fallback) {
    if (rules.isEmpty()) {
      throw new IllegalArgumentException("at least one rule is required");
    }
    this.rules = List.copyOf(rules);
    this.fallback = fallback;
  }

  public List<Rule> rules() {
    return rules;
  }

  public Material fallback() {
    return fallback;
  }

  public Material materialOf(char character) {
    for (Rule rule : rules) {
      if (rule.matches(character)) {
        return rule.material;
      }
    }
    return fallback;
  }

  /**
   * Every printable ASCII character that this rule set decodes to {@code material}, in code order.
   * Used to encode a plain material map back into the puzzle form.
   */
  public List<Character> charactersFor(Material material) {
    List<Character> found = new ArrayList<>();
    for (char candidate = '!'; candidate <= '~'; candidate++) {
      if (materialOf(candidate) == material) {
        found.add(candidate);
      }
    }
    return found;
  }

  /** Reports any material the rules can never produce, which would make encoding impossible. */
  public Map<Material, Integer> alphabetSizes() {
    Map<Material, Integer> sizes = new LinkedHashMap<>();
    for (Material material : Material.values()) {
      sizes.put(material, charactersFor(material).size());
    }
    return sizes;
  }

  public static MaterialRules load(Reader source) throws IOException {
    Properties properties = new Properties();
    properties.load(source);
    return from(properties);
  }

  public static MaterialRules load(InputStream source) throws IOException {
    Properties properties = new Properties();
    properties.load(source);
    return from(properties);
  }

  private static MaterialRules from(Properties properties) {
    String ordered = properties.getProperty("rules");
    String fallbackName = properties.getProperty("default");
    if (ordered == null || fallbackName == null) {
      throw new IllegalArgumentException("rule file needs both 'rules' and 'default'");
    }

    List<Rule> parsed = new ArrayList<>();
    for (String entry : ordered.split(",")) {
      String trimmed = entry.trim();
      if (trimmed.isEmpty()) {
        continue;
      }
      String[] halves = trimmed.split(":", 2);
      if (halves.length != 2) {
        throw new IllegalArgumentException("rule must be 'predicate:MATERIAL', got '" + trimmed + "'");
      }
      parsed.add(new Rule(halves[0].trim(), material(halves[1])));
    }
    MaterialRules rules = new MaterialRules(parsed, material(fallbackName));
    // Fail at load time rather than producing a picture with a missing colour.
    rules.alphabetSizes().forEach((material, size) -> {
      if (size == 0) {
        throw new IllegalArgumentException(
            "no printable character decodes to " + material + " under these rules");
      }
    });
    return rules;
  }

  private static Material material(String name) {
    return Material.valueOf(name.trim().toUpperCase(Locale.ROOT));
  }
}

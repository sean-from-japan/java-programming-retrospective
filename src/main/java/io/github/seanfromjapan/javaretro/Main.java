package io.github.seanfromjapan.javaretro;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.github.seanfromjapan.javaretro.pixelart.MaterialRules;
import io.github.seanfromjapan.javaretro.pixelart.PngWriter;
import io.github.seanfromjapan.javaretro.pixelart.Raster;
import io.github.seanfromjapan.javaretro.pixelart.Scene;
import io.github.seanfromjapan.javaretro.pixelart.SceneRenderer;
import io.github.seanfromjapan.javaretro.pixelart.Sprite;
import io.github.seanfromjapan.javaretro.pixelart.SpriteEncoder;
import io.github.seanfromjapan.javaretro.weights.Body;
import io.github.seanfromjapan.javaretro.weights.CatalogueParser;
import io.github.seanfromjapan.javaretro.weights.ParseProblem;
import io.github.seanfromjapan.javaretro.weights.WeightTable;

/** Command line front end. Every subcommand is a thin wrapper over a tested class. */
public final class Main {

  private static final String RULES_RESOURCE = "/material-rules.properties";

  private final PrintStream out;
  private final PrintStream err;

  public Main(PrintStream out, PrintStream err) {
    this.out = out;
    this.err = err;
  }

  public static void main(String[] args) {
    System.exit(new Main(System.out, System.err).run(args));
  }

  /** Returns the process exit code: 0 success, 1 bad data, 2 wrong usage. */
  public int run(String[] args) {
    if (args.length == 0) {
      usage();
      return 2;
    }
    try {
      switch (args[0]) {
        case "weights":
          return weights(args);
        case "scene":
          return scene(args);
        case "encode":
          return encode(args);
        default:
          err.println("unknown command: " + args[0]);
          usage();
          return 2;
      }
    } catch (IllegalArgumentException bad) {
      err.println("error: " + bad.getMessage());
      return 2;
    } catch (IOException failure) {
      err.println("error: " + failure.getMessage());
      return 2;
    }
  }

  private void usage() {
    err.println("usage:");
    err.println("  weights <catalogue.txt> <pounds>       print the conversion table");
    err.println("  scene   <sprite.txt> <output.png>      draw the sprite into a scene");
    err.println("  encode  <sprite.map> <output.txt>      encode a symbol map");
  }

  private int weights(String[] args) throws IOException {
    if (args.length != 3) {
      usage();
      return 2;
    }
    double pounds;
    try {
      pounds = Double.parseDouble(args[2]);
    } catch (NumberFormatException notANumber) {
      err.println("error: '" + args[2] + "' is not a number of pounds");
      return 2;
    }

    CatalogueParser.Result result = new CatalogueParser().parse(Path.of(args[1]));
    List<Body> bodies = result.bodies();
    out.printf("%.0f lb = %.3f kg of mass%n%n", pounds,
        io.github.seanfromjapan.javaretro.weights.WeightConverter.poundsToKilograms(pounds));
    out.print(new WeightTable(3).render(pounds, bodies));

    if (!result.isClean()) {
      err.println();
      err.println(result.problems().size() + " line(s) could not be read:");
      for (ParseProblem problem : result.problems()) {
        err.println("  " + problem);
      }
      return 1;
    }
    return bodies.isEmpty() ? 1 : 0;
  }

  private int scene(String[] args) throws IOException {
    if (args.length != 3) {
      usage();
      return 2;
    }
    MaterialRules rules = loadRules();
    Sprite sprite = Sprite.decode(Path.of(args[1]), rules);

    Scene scene = Scene.standard(512, 512);
    Raster raster = scene.paintBackground();
    SceneRenderer renderer = new SceneRenderer(scene);

    // A large boat on the right and a smaller mirrored one further off, each
    // placed so the waterline cuts through the hull rather than above it.
    renderer.draw(raster, sprite, 224, 160, 8, true);
    renderer.draw(raster, sprite.mirrored(), 48, 210, 4, true);

    Path output = Path.of(args[2]);
    PngWriter.write(raster, output);
    out.println("wrote " + output + " (" + raster.width() + "x" + raster.height() + ")");
    out.println("sprite " + sprite.width() + "x" + sprite.height()
        + ", waterline at y=" + scene.waterlineY());
    return 0;
  }

  private int encode(String[] args) throws IOException {
    if (args.length != 3) {
      usage();
      return 2;
    }
    MaterialRules rules = loadRules();
    Sprite sprite;
    try (Reader reader = Files.newBufferedReader(Path.of(args[1]), StandardCharsets.UTF_8)) {
      sprite = Sprite.fromSymbolMap(reader);
    }
    Files.writeString(Path.of(args[2]), new SpriteEncoder(rules).encode(sprite),
        StandardCharsets.UTF_8);
    out.println("wrote " + args[2] + " (" + sprite.width() + "x" + sprite.height() + ")");
    return 0;
  }

  private MaterialRules loadRules() throws IOException {
    try (InputStream stream = Main.class.getResourceAsStream(RULES_RESOURCE)) {
      if (stream == null) {
        throw new IOException("missing resource " + RULES_RESOURCE);
      }
      return MaterialRules.load(stream);
    }
  }
}

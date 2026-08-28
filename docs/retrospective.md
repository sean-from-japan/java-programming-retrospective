# Retrospective

A longer account of what changed between the graded submissions and this
repository, and why. Written from memory of my own code and from re-reading
it; the original files are not published here.

## 1. The teaching library

Both submissions began with an import of a small package supplied by the
department. It provided a reader that returned a parsed `int` or `double`
straight from a prompt, a writer with column-width and decimal-place
arguments, and a graphics canvas with a fill-rectangle call and a colour
setter.

It is a good teaching device. In week one it removes `Scanner`, checked
exceptions, `printf` format strings and the whole of Swing from the problem,
which is exactly what a beginner needs removed.

It has one cost, and I only saw it later: **every program written against it
is unbuildable outside the course.** The submissions cannot be compiled by
anyone who does not have the department's jar, which means they cannot be
shown, cannot be run in CI, and cannot be revisited. That is why replacing it
was the first thing I did here, and why nothing in this repository depends on
anything outside the JDK except JUnit.

The replacements were smaller than I expected:

| Helper | Replacement |
|---|---|
| prompted `readInt` / `readDouble` | `BufferedReader` plus explicit parsing that reports what it could not read |
| file reader with `eof()` | `Files.newBufferedReader` and `readLine() != null` |
| writer with width and decimals | `String.format` with an explicit `Locale.ROOT` |
| graphics canvas | an `int[]` of ARGB pixels, written out by `ImageIO` in one class |

The `Locale.ROOT` detail is the kind of thing the helper hid: the original
would have printed `45,359` rather than `45.359` on a machine configured for
much of Europe, and neither I nor the marker would have seen it.

## 2. Reading a file that a person wrote

The first assignment read a small data file of gravity values. The file had
three different shapes in it — a name and a number on one line, a name and its
number on consecutive lines, and one value written into an English sentence.

My submission handled this by reading tokens, then reconstructing a line
character by character with a loop over `readChar()`, then searching that
string for `" of "` and `" is "` and slicing between them. It worked on the
file it was given.

Two things were wrong with it:

**It could not say what it had failed on.** The parse was wrapped in a `try`
with an empty `catch`. A malformed line produced a table with one fewer row
and no message at all. I remember thinking of that as being tidy.

**It was one method.** Reading, deciding the shape of a line, parsing a
number, and printing were interleaved, so there was no seam to test at.

The rebuild splits those apart. `CatalogueParser` returns a `Result` holding
both the bodies it understood and a list of `ParseProblem`, each with a line
number and a reason. The command line prints the table from the first list and
the failures from the second, and exits non-zero when the second is non-empty.

The version that reports its failures is barely longer than the one that hid
them. What it needed was not more code but somewhere for the failure to *go*.

## 3. Deciding what a character means

The second assignment read a grid of characters, and each character's meaning
came from arithmetic on its code point: divisible by one number meant one
thing, even meant another, and so on, tested in a specific order.

My submission had that as an if/else chain with the divisors written in as
bare numbers, plus a one-line comment noting that one test had to come before
the others. The comment was right, and it was the only thing standing between
a working picture and a subtly wrong one.

Here the rules live in `material-rules.properties`:

```properties
rules = 13:FLAG, 5:SAIL, 3:HULL, odd:MAST
default = WATER
```

Three things changed as a result:

- The order is visible, and the file says in words that the first match wins.
- A test asserts that reordering the rules changes the decoded picture, so the
  significance of the order is checked rather than remembered.
- The rule set is validated when it loads: if no printable character can
  decode to some material, the file is rejected immediately, rather than
  producing a picture missing a colour.

## 4. Making a drawing testable

The original drew directly onto the helper's canvas. To know whether it was
right, a person had to look at a window for twenty seconds before it closed.

The rebuild splits this into `Raster` (a pixel buffer with `get` and
`fillRect`), `Scene` (where the horizon is and what colour each band is),
`SceneRenderer` (how a sprite lands on a raster, at a scale, mirrored,
submerged) and `PngWriter` (the only class that has ever heard of an image
format).

Now the interesting behaviour is directly assertable. The one that matters:

```java
new SceneRenderer(scene).draw(raster, sprite("H\nH\n"), 0, 19, 1, true);
assertEquals(Material.HULL.argb(), raster.get(0, 19)); // above the waterline
assertEquals(waterColour,          raster.get(0, 20)); // below it, hidden
```

That is the occlusion rule from the original assignment, stated as two
assertions instead of a paragraph of hoping.

## 5. Owning the sample data

The pixel data came with the exercise, so it is not mine to publish. Rather
than find a substitute, I wrote the missing half of the program: an encoder
that turns a readable material map into the noise form.

That solved three problems at once.

- The sample data in `data/` is mine and is reproducible from
  `data/sailboat.map` with one command.
- The decoder became provable: encode a known picture, decode it, compare.
  Before this, "the picture looks right" was the only test available.
- A property of the rule set became checkable — every material must have at
  least one character that decodes to it, or the picture cannot be encoded at
  all. That check now runs when the rules file loads.

Writing the inverse of a function you were given is the cheapest way I know to
find out whether you actually understood it.

## 6. What I would still change

- `Material` couples meaning to colour. A palette separate from the material
  would be better for anything beyond one picture, but it would also be
  ceremony at this size.
- The catalogue parser recognises three hand-written shapes with regular
  expressions. Past four or five, that becomes a grammar, and a hand-written
  recursive descent parser would be clearer than a fourth pattern.
- There is no property-based testing. The encode/decode round trip is exactly
  the shape that generated inputs would test well, and it is checked on two
  fixed pictures instead.

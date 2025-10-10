<!--
Guidance for AI coding agents working on the pptbuilder repository.
Keep this short, specific, and actionable. Reference files and examples that
clarify project structure, conventions, and developer workflows.
-->

# pptbuilder — AI assistant instructions

- Purpose: generate presentations from an XML+Markdown hybrid format ("xmlmd").
- Language: Kotlin on the JVM. Build with Gradle (wrapper included).

Key concepts (big picture)
- Input: an XML document with a `<presentation>` root. Head metadata lives in `/presentation/head` and slides are `<section>` and `<slide>` nodes. See `Parser.kt`.
- Parse: `Parser.parse(...)` turns XML into an AST defined in `src/main/kotlin/com/newardassociates/pptbuilder/AST.kt` (types: `Presentation`, `Section`, `Slide`). Markdown bodies are parsed with Flexmark into `markdownBody`.
- Transform: `Processor` (abstract) walks the AST and delegates to format-specific processors. Concrete processors live under `pptx/`, `reveal/`, `slidy/`, `text/`, `nop/`.
- Output: processors emit their target format and call `write(...)`. PPTX uses Apache POI via `PPTXProcessor` + `Deck` + `Slide` helpers.

Important files to read when making changes
- `App.kt` — CLI entry point; shows supported formats, CLI flags, and how `Processor.Options` are constructed.
- `Parser.kt` — XML parsing, XPath expressions for head metadata, XInclude-aware DocumentBuilder, Flexmark setup.
- `Processor.kt` — common processing flow, code-import helper `importCode(...)`, and where footnotes are collected.
- `pptx/PPTXProcessor.kt`, `pptx/Deck.kt`, `pptx/Slide.kt` — PPTX slide creation, layouts, and property wiring.
- `reveal/RevealProcessor.kt` and `slidy/SlidyProcessor.kt` — examples of HTML output generation using Flexmark NodeVisitor handlers.
- Tests under `src/test/kotlin/.../PPTXProcessorTests.kt` — these provide many compact XMLMD examples you can reuse.

Project-specific conventions and patterns
- XMLMD: slide content is raw Markdown inside `<slide>...</slide>`; legacy code slides use nested `<text>` and `<code>` nodes. See `Parser.parseNodes` and `Processor.processSlide`.
- Title separators: presentation titles may contain `|` to represent a title line break; processors often call `replace("|", "\n")`.
- Bullet semantics: Flexmark `BulletList.openingMarker == '-'` is treated as "no bullet" (space character) in PPTX; check `PPTXProcessor`.
- Code import: `<code src="..." marker="..."></code>` supports HTTP(S) and local files; markers capture between special BEGIN/END markers in source files. See `Processor.importCode`.
- Templates: PPTX template path passed via `--template` CLI flag and wired into `Processor.Options.templateFile`.

Build, run and test (developer workflows)
- Build jar (fat/shadow): use the included Gradle wrapper

```powershell
./gradlew.bat shadowJar
```

- Run CLI (example): produce PPTX from an xmlmd file

```powershell
./gradlew.bat run --args="input.xmlmd outputname --format pptx --template mytemplate.pptx --verbosity info"
```

- Run tests:

```powershell
./gradlew.bat test
```

Patterns for safe edits
- When changing AST shapes, update `Parser.kt` and all `Processor` implementations that deconstruct slides (search for `markdownBody`, `slide.node`, `Slide(title, node, ...)`).
- Prefer adding small unit tests in `src/test/kotlin/...` using existing test helpers and XMLMD samples. Tests are used as living examples of expected output.
- For PPTX changes, inspect `Deck` layout selection logic; templates may lack expected layouts and code should fall back gracefully.

Integration points & external libs
- Flexmark (Markdown parsing) — configuration in `Parser.kt` and visited in processors using NodeVisitor handlers.
- Apache POI (PPTX) — used heavily in `pptx/` package. Watch out for text box sizing and anchor math (some TODOs reference sizing bugs).
- XInclude / XML parsing — `DocumentBuilderFactory` is XInclude-aware; relative includes affect `node.baseURI` and `importCode` path resolution.

Quick examples to reference in edits
- Sample XMLMD slides are embedded in tests (see `PPTXProcessorTests.kt`, especially `pptxLegacyCode` and `pptxTitleMarkedupTextContent`).
- Legacy XInclude example: `src/test/resources/legacyXIncludedCode.xmlmd`.

If you need more context
- Run tests locally to see expected file outputs under `build/test-results/test/pptx/`.
- Ask for specific example input->output pairs to clarify rendering behavior for Markdown constructs (lists, emphasis, code blocks).

If you change public APIs or behavior, update `README.md` and add/extend tests. Ask for a review of visual output when PPTX or HTML rendering changes are non-trivial.

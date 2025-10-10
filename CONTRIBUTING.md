# Contributing to pptbuilder

Thank you for helping improve pptbuilder. This file is a short developer quickstart — see `README.md` for project background.

Build / run / test
- Build a fat JAR (Windows PowerShell):

```powershell
./gradlew.bat shadowJar
```

- Run the CLI locally (example):

```powershell
./gradlew.bat run --args="input.xmlmd outputname --format pptx --template mytemplate.pptx --verbosity info"
```

- Run unit tests:

```powershell
./gradlew.bat test
```

Where to make edits
- Parser & AST: `src/main/kotlin/com/newardassociates/pptbuilder/Parser.kt` and `AST.kt`. If you change the AST shape, update all processors that deconstruct `Slide` / `Section` and add/update tests.
- Processors: format-specific processors are under `src/main/kotlin/com/newardassociates/pptbuilder/` (subfolders `pptx`, `reveal`, `slidy`, `text`, `nop`). Keep existing NodeVisitor patterns when altering markdown traversal.
- PPTX specifics: `pptx/Deck.kt` selects slide layouts from template masters. If you change layout handling, ensure templates without matching named layouts still work.

Tests and examples
- Use `src/test/kotlin/.../PPTXProcessorTests.kt` for compact XMLMD examples that exercise processors. Add tests that include XMLMD snippets in test functions or new resources under `src/test/resources`.

PR checklist
- Include or update unit tests for new behavior.
- Run `./gradlew.bat test` and fix any regressions.
- Update `README.md` or `.github/copilot-instructions.md` if public behavior or CLI options change.

If in doubt, open an issue describing the change and link to the tests you plan to add.

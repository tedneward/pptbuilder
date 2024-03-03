# pptbuilder
A tool to create presentations out of XML/markdown combination

## Legacy format
The legacy format is the format I used for presentations prior to the construction of this tool. The legacy format is XML-based, but the contents of the XML slides are a pidgin Markdown-like format in that it uses an incrementing-number of `*` to indicate a bulleted list, and `-` to indicate an unbulleted (no glyph) list. One `*` was top-level (no indent), as was no `*`.

The format suffered from a serious lack of flexibility, in that it offered no hyperlinks, no images, nothing beyond text, really. The tool suffered similarly, in that it could produce PPTs and nothing else. With this generation of tool, I want to correct for that.

Legacy examples/samples appear in `slidesamples/legacy`.

## New format: XML/MD
The new format wants to be pure Markdown inside of XML. The XML is still useful because of XInclude, to modularize, and XML allows for in-place metadata on various slide elements. Markdown then describes each slide. I would like to support the full flavor of Markdown, so as to allow for maximum flexibility in slide content, though some features of Markdown will be tricky to translate.

New format examples/samples appear in "src/tests/kotlin" and "src/tests/resources", with the "xmlmd" suffix. (I deliberately chose to use a different suffix than just "xml" because I want to differentiate these files against other XML files that might be used. It's easy enough to configure editors to recognize "xmlmd" as an "xml" file type, after all.)

I would like the tool to parse XMLMD into an AST, then transform that AST into a variety of different output formats: PPTX (it's been long enough, let's just move away from PPT at this point), PDF, and HTML ([Slidy?](https://www.w3.org/2005/03/slideshow.html#(1)) [RevealJS?](https://revealjs.com/)). DocBook Slides, just for fun?

I also want a tool (not necessarily the same one) that knows how to parse the legacy format and spit out XMLMD equivalents, for easy porting.

## Tech stack
I needed a Markdown library that doesn't go from Markdown straight to HTML; I needed it to parse into an intermediate format that allows me to do the actual output. Beyond that, this could be done in just about any language/platform stack that allows me to do this headless (for CI/CD purposes).

Something JVM-based seemed to be the best solution; it has an XIncluding XML parser, PPTX support (in the Apache POI libraries) and Markdown libraries (Flexmark-Java) that I need. I could write it one of many different languages, a la Java, Groovy, Kotlin, Scala, even Clojure if I really feel like punishing myself. ;-) Chose Kotlin--seems to be the best "Java++" I can work with.

## Feature backlog

### XMLMD syntax/semantics
* Allow <code>/```-fenced blocks to use URLs (for reference to Demo GitHub projects, rather than embedding all the code in this repo)

* Provide "property" support, to customize slide decks with values (mostly to choose whether to include certain slides or sections or not)?

* Image imports/references
    * As a tag? Or as Markdown?
    * Does <slide> want/need to incorporate images as part of its layout somehow? A la specific slide tags for each layout? ("pzenslide", "twocolslide", ...?)

* Chart/graph imports/references
    * Use MermaidJS to generate 

### Infrastructure
* Create a diagnostic log of items happening (with verbosity levels)

* Work off of template files as starting points for processed output; use Freemarker for slidy/reveal templates?

* Use GraalVM to run? (Allows use of JavaScript, Python, ....)

* Host the whole generation process inside a Docker image (for easier config mgmt)?

### Markdown
* Fenced code blocks should be in separate text boxes (?)

* Support "*" vs "-" bullet list differences (bullet vs no-bullet)

* Footnotes (`[^1]` or `[^devguide]`) should be collected into a slide section at the end of the deck; references should be normalized by footnote tag; any footnoted text *not* met by a footnote definition should yield a warning? error?

    * Only generate footnotes if the pptbuilder is invoked with a `-r` option to reference a bibliography; if it is, any unrecongized footnotes should generate a warning

* Title slide contact info should have icons/emojis/whatever for email/Twitter/LinkedIn/etc

* Allow <slide>/<notes> nodes to use Markdown styling

* Create a <slide>/<prose> section for longer-form consumption? (Or are we getting too much into Terra territory here?)

* URLs
    * LinkedIn URLs look like: https://www.linkedin.com/in/tedneward/
    * Twitter URLs look like: https://twitter.com/tedneward
    * Github URLs look like: https://github.com/tedneward

### PPTX improvements/fixes
* Fix bug around code-block incorrect bounding box size calculations

* Fix lazyinit titleonly problem with PPT

### Slidy improvements/fixes
* Allow -t flag to specify stylesheet to use for customization

### DXSlides

### Pandoc-PPTX
* As a potential workaround for the code-block incorrect bounding box size calculations

## Sample "~/.pptbuilder.properties" file

```
#PPTBuilder
#Fri Jun 12 02:55:15 PDT 2020
author=Ted Neward
affiliation=Neward & Associates
jobTitle=Principal

contact.website=http\://www.newardassociates.com
contact.blog=http\://blogs.newardassociates.com
contact.email=ted@tedneward.com
contact.github=tedneward
contact.linkedin=tedneward
contact.twitter=@tedneward
contact.mastodon=@tedneward@hachyderm.io

#template.pptx=/Users/tedneward/Projects/Presentations/Templates/__Template.pptx
```

* Add website contact links/info

* "template" should be segmented into different template types: .pptx, .slidy, .reveal, etc, one for each --format type


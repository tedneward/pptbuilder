# pptbuilder
A tool to create presentations out of XML/markdown combination

## Legacy format
The legacy format is the format I used for presentations prior to the construction of this tool. The legacy format is XML-based, but the contents of the XML slides are a pidgin Markdown-like format in that it uses an incrementing-number of "*" to indicate a bulleted list, and "-" to indicate an unbulleted (no glyph) list. One "*" was top-level (no indent), as was no "*".

The format suffered from a serious lack of flexibility, in that it offered no hyperlinks, no images, nothing beyond text, really. The tool suffered similarly, in that it could produce PPTs and nothing else. With this generation of tool, I want to correct for that.

Legacy examples/samples appear in "slidesamples/legacy".

## New format: XML/MD
The new format wants to be pure Markdown inside of XML. The XML is still useful because of XInclude, to modularize, and XML allows for in-place metadata on various slide elements. Markdown then describes each slide. I would like to support the full flavor of Markdown, so as to allow for maximum flexibility in slide content, though some features of Markdown will be tricky to translate.

New format examples/samples appear in "slidesamples/xmlmd".

I would like the tool to parse XMLMD into an AST, then transform that AST into a variety of different output formats: PPTX (it's been long enough, let's just move away from PPT at this point), PDF, and HTML ([Slidy?](https://www.w3.org/2005/03/slideshow.html#(1)) [RevealJS?](https://revealjs.com/)).

I also want a tool (not necessarily the same one) that knows how to parse the legacy format and spit out XMLMD equivalents, for easy porting.

## Tech stack
I need a Markdown library that doesn't go from Markdown straight to HTML; I need it to parse into an intermediate format that allows me to do the actual output. Beyond that, this could be done in just about any language/platform stack that allows me to do this headless (for CI/CD purposes).

Java seems to be the best solution; it has an XIncluding XML parser, PPTX support (in the Apache POI libraries) and Markdown libraries (Flexmark-Java) that I need. I can write it one of many different Java languages, a la Java, Groovy, Kotlin, Scala, even Clojure if I really feel like punishing myself. ;-) Thinking Kotlin for now--seems to be the best "Java++" I can work with.

## Feature backlog

* Create a diagnostic log of items happening (with verbosity levels)

* Fenced code blocks should be in separate text boxes

* Provide "property" support, to customize slide decks with values (mostly to choose whether to include certain slides or sections or not)?

* Title slide contact info--all should be relevant hyperlinks (mailto's for email, URL for twitter, etc)

* Title slide contact info should have icons/emojis/whatever for email/Twitter/LinkedIn/etc

* Allow <slide>/<notes> nodes to use Markdown styling

* Work off of template files as starting points for processed output

LinkedIn URLs look like: https://www.linkedin.com/in/tedneward/
Twitter URLs look like: https://twitter.com/tedneward
Github URLs look like: https://github.com/tedneward

## Sample "~/.pptbuilder.properties" file

```
#PPTBuilder
#Fri Jun 12 02:55:15 PDT 2020
author=Ted Neward
affiliation=Neward & Associates
jobTitle=Principal

contact.blog=http\://blogs.tedneward.com
contact.email=ted@tedneward.com
contact.github=tedneward
contact.linkedin=tedneward
contact.twitter=@tedneward

#template=/Users/tedneward/Projects/Presentations/Templates/__Template.ppt
```

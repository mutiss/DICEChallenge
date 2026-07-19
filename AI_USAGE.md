# AI Usage

This document discloses which AI assistants were used while building this project, and for what purpose. Transparency about AI usage matters, so this is kept up to date and honest about where AI helped and where it didn't.

## Claude
- Writing unit tests and UI tests entirely: with this help, the majority of edge cases and scenarios were covered easily
- Generation of testTags for UI tests and accesibility
- Generation of `AI_USAGE.md` file and the `README.md` file
- Auditing the codebase and suggesting and applying improvements
- Generating some composables, such as `StatusViews`, `BottomBar`, `ArtistRow` and `ReleaseGroupRow` and previews

## ChatGPT
- Architecture discussions
- Generating an SVG asset for the DICE triangle icon
- Asking about and checking HTTP caching for network requests

## Gemini
- AI-assisted fixes during development
- Suggesting improvements and catching things that may have been missed

## Review

Everything produced with the help of these tools was reviewed before being included in the project. Nothing was accepted blindly — every suggestion, test, or fix was read and understood before committing it.

## Closing note

AI is a powerful tool. Used well, it speeds up the parts of development that benefit from a second pair of hands — tests, boilerplate, brainstorming — while leaving the judgment calls where they belong: with the developer.

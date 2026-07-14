# Contributing

## Versioning

This project's version is **generated automatically** — do not edit the `<version>`
in `pom.xml` by hand, and don't create tags.

Just follow the commit convention below; the rest happens on its own:

- `fix: description` → bug fix (bumps patch version)
- `feat: description` → new feature (bumps minor version)
- `feat!: description` (or a `BREAKING CHANGE:` footer) → incompatible change (bumps major
  version — except while the project is pre-`1.0.0`, where `bump-minor-pre-major` in
  `release-please-config.json` makes it bump minor instead, same as a plain `feat:`)

On push/merge to `main`, a bot (Release Please) opens its own PR proposing the new
version. You don't need to touch any of that.

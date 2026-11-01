# Contributing to cLeanBot

Thanks for helping! cLeanBot is in active development, so the code and its structure are moving quickly.

## Ground Rules

- **Read the [safety rules](README.md#%EF%B8%8F-safety--reliability-principles) first.** A pull request that uninstalls apps that came with the TV, bypasses the protected list, or skips recording what was originally active won't be merged.
- **Open pull requests against `dev`.** `main` is the release branch.
- **One change per pull request.** Smaller is easier to review.
- **`./gradlew compileDebugKotlin` and tests must pass** before opening a PR.
- **New files start with an SPDX header:** `// SPDX-License-Identifier: GPL-3.0-only`.
- **Licensing.** By contributing you agree that your work is licensed under GPL-3.0. If you adapt code or lists from another project, check that its license is compatible and add it to [NOTICE](NOTICE) in the same PR.

## Debloat Lists

The per-TV lists behind **Debloat** and the Overview's list card live in the upstream [tellyscope-devices](https://github.com/man15h/tellyscope-devices) repository; add or fix a list there. When you add a package to a list:

- State which TV models you tested on, and what changed after disabling it.
- If disabling it broke anything (launcher, updates, remote, input, HDMI-CEC), put it in the list's keep entries, or propose it for the built-in protected list (`adb/src/main/kotlin/com/gerstudio/cleanbot/adb/Protected.kt`).

### Using Your Own Lists

You don't have to trust external lists. You can fork `tellyscope-devices`, edit the lists in your fork, and keep `index.json` on schema 1 with each file's sha256 updated. Then in the app:
1. Open **Settings** (in the ⋮ menu at the top right).
2. Open **Advanced** at the bottom.
3. Tap **Use your own debloat lists**, and paste the raw HTTPS address of the folder that holds `index.json`.

The built-in protected list still wins over any external list, and **Reset to default** returns to the standard lists.

## Launchers, App Names and Streaming Services

These lists live in the device repository too, and the app downloads them dynamically:
- `lists/launchers.json`: Third-party launchers cLeanBot recognizes.
- `lists/labels.json`: Display names shown for common TV apps when the TV cannot report them.
- `lists/streaming.json` and `logos/`: Streaming services the Remote can display a shortcut button for.

Which launchers may turn on an accessibility service is hardcoded in `ACCESSIBILITY_LAUNCHERS` in [`Launchers.kt`](app/src/main/java/com/gerstudio/cleanbot/data/Launchers.kt), because an accessibility service has broad screen reading access.

## Code Style

- Follow standard Kotlin official code style, as defined in `.editorconfig`.
- Architecture separation: Logic that does not depend on the Android framework belongs in the `:adb` module with isolated unit tests.
- UI uses Material 3 Expressive components and the app theme. Do not hard-code colors or raw pixel dimensions.
- New UI text belongs in `app/src/main/res/values/strings.xml`, never as hardcoded literals in Kotlin. Compose code consumes strings with `stringResource`.

## Reporting a TV

To report an unsupported or new TV model, open a device report issue. Please ensure you do not include serial numbers, private IP addresses, or personal account names.

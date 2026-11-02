# Seldom

- Android app that opens Selkies desktops full screen in a WebView, with physical-keyboard capture and a host list.
- Requires Android 11 (API 30) or newer.

## Keyboard

- While a session window has focus, system shortcuts are captured and sent to the remote desktop:
  - Android 16 QPR2+ (API 36.1): `WindowManager.LayoutParams.setKeyboardCaptureEnabled`, called by reflection.
  - Samsung devices: `SemWindowManager.requestMetaKeyEvent`.
- Capture is released when the window loses focus.
- Ctrl+Alt+Shift+Z toggles capture on and off and shows a toast.
- Super is reported to the page as `metaKey` / `getModifierState('Meta')` while held.
- Physical arrow keys are re-dispatched to the WebView so the page receives them.
- Mouse hover is forwarded to the page so the remote cursor follows it; the Android pointer is hidden.

## Hosts

- The hosts screen lists saved hosts (name, URL). The add button creates one; tapping a host opens it.
- Edit or delete a host from its entry.
- URLs must be `https://` with a host and no user info.
- Each host opens in its own task (separate Recents entry); opening it again brings that task forward.
- Only https navigation within the host's origin stays in the WebView; other links open externally.
- HTTP basic auth: saved credentials are tried once, then a dialog asks, with an option to remember. Credentials are stored encrypted with an Android Keystore AES-256-GCM key.
- Back asks for confirmation before disconnecting.

## Install with Obtainium

- Source URL: `https://github.com/markgeejw/seldom`
- Enable pre-release versions; releases are published as prereleases.
- The APK is signed with the kaegr key.

## Building

- JDK 17 and the Android SDK (compileSdk 36) are required.
- `./gradlew testDebugUnitTest lintDebug assembleDebug`
- Debug builds install as `me.kaegr.seldom.debug` (label "Seldom DBG"); release builds use `me.kaegr.seldom`.
- Version comes from `seldomVersionName` and `seldomVersionCode` in `gradle.properties`.

## Signing

- `./gradlew assembleRelease` reads, from the environment:
  - `SELDOM_KEYSTORE`: path to the keystore
  - `SELDOM_KEYSTORE_PASSWORD`: store and key password
  - `SELDOM_KEY_ALIAS`: key alias
- If unset, it reads `keystore`, `password` and `alias` from `~/.local/state/seldom/signing.properties`.
- With neither, release tasks fail; there is no debug-key fallback.

## Releases

- Pushing a tag `v<seldomVersionName>` runs `.github/workflows/release.yml`: tests, lint, signed build, signer check, then a draft prerelease.
- Required secrets: `SELDOM_KEYSTORE_BASE64`, `SELDOM_KEYSTORE_PASSWORD`, `SELDOM_KEY_ALIAS`.
- The expected signer certificate SHA-256 is pinned in `release/signer-sha256.txt`; the release fails if the APK is signed by any other certificate.
- `seldomVersionName` must equal `0.<seldomVersionCode>.0`.

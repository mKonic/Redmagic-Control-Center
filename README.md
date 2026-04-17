# RedMagic Root Control

Starter Android Studio project for a root-only RedMagic hardware control app.

## What it does
- Fan on/off and fan level control
- Fan RPM readout
- Pump on/off
- LED test actions
- Trigger enable/disable
- Slider behavior commands
- Haptic test

## Build
1. Open this folder in Android Studio.
2. Let Gradle sync.
3. Build `app`.
4. Install the generated APK.
5. Grant root on first launch.
6. Enable the accessibility service if you want trigger key interception.

## Notes
- This is based on sysfs/procfs/settings paths documented in the linked Red Magic hardware control guide.
- Different RedMagic models or ROMs may expose different paths.
- If a control does nothing, verify the node exists on-device with `su -c ls` and update the constants in `HardwareController.kt`.
- The project intentionally does not include a signing config.

## Output APK location after local build
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

## Build on GitHub Actions

This repository includes `.github/workflows/android.yml` so GitHub can build a debug APK automatically.

### How to use it

1. Create a new GitHub repository.
2. Upload or push all files from this project.
3. Open the **Actions** tab in GitHub.
4. Run **Android CI** manually, or push to `main`.
5. When the workflow finishes, open the run and download the artifact named `redmagic-root-control-debug-apk`.

### Notes

- This workflow builds a **debug APK**.
- No signing secrets are needed for the debug build.
- If you later want a **release APK**, you will need to add a keystore and GitHub secrets.

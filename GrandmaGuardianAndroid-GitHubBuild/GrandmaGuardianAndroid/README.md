# Grandma Guardian — Android APK

A simple offline scam-message checker for direct APK distribution.

## Features
- French / Arabic / Moroccan Darija interface
- Arabizi normalization: 3, 7, 9, 5, 6, 8, 2
- Detects money, account, sending, passwords/OTP, phone numbers, links,
  urgency, secrecy, remote access, prizes, impersonation and suspicious combinations
- Works offline
- No Google Play dependency
- No server required

## Build an APK in GitHub (no Android Studio download needed on your PC)

1. Create a new GitHub repository.
2. Upload the contents of this project to the repository, including the `.github/workflows/build-apk.yml` file.
3. Open the repository's **Actions** tab and select **Build Grandma Guardian APK**.
4. If prompted, enable workflows, then click **Run workflow**.
5. Wait for the workflow to finish successfully.
6. Open the completed run and download the `GrandmaGuardian-debug-apk` artifact. It downloads as a ZIP; extract it to get `app-debug.apk`.
7. Send `app-debug.apk` to the Android phone and install it. Android may ask to allow installs from the app used to open the APK.

GitHub-hosted builds may take several minutes. You do not need to install Python or Android Studio on the phone.

## Local build (alternative)
Open this folder in Android Studio with an Android SDK installed, then build the debug APK. It normally appears under `app/build/outputs/apk/debug/app-debug.apk`.

## Important
This is a defensive rule-based detector. It cannot guarantee that every scam will be detected and should not be treated as proof that a message is safe.

# MiUNIEventos - Setup Guide

This guide will help you set up the project from scratch, whether you're a teacher evaluating the project or a developer cloning the repository.

## Quick Start

```bash
# 1. Clone the repository
git clone https://github.com/domicoder/miuni-eventos.git
cd miuni-eventos

# 2. Run the setup check
make setup
# or
./scripts/setup.sh

# 3. Follow the instructions to fix any missing configurations
```

---

## Prerequisites

| Requirement    | Version   | Check Command              |
| -------------- | --------- | -------------------------- |
| Android Studio | Hedgehog+ | -                          |
| JDK            | 17+       | `java -version`            |
| Android SDK    | 34        | Android Studio SDK Manager |
| Node.js        | 18+       | `node --version`           |
| Firebase CLI   | Latest    | `firebase --version`       |

---

## Required Configuration Files

### 1. `google-services.json` (CRITICAL)

**This file is required for the app to build.** It contains Firebase configuration.

#### How to get it:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project (or use existing)
3. Click **"Add app"** -> Select **Android**
4. Enter package name: `com.domicoder.miunieventos`
5. Register app and download `google-services.json`
6. Place it in the `app/` directory

#### Required Firebase Services to Enable:

| Service            | How to Enable                                                                |
| ------------------ | ---------------------------------------------------------------------------- |
| **Authentication** | Authentication -> Sign-in method -> Enable **Email/Password** and **Google** |
| **Firestore**      | Build -> Firestore Database -> Create database                               |
| **Storage**        | Build -> Storage -> Get started                                              |

> **Important for Google Sign-In**: You MUST enable the Google sign-in provider in Firebase Authentication for the OAuth client ID to be included in `google-services.json`.

---

### 2. `local.properties` (CRITICAL for Maps)

**This file contains your API keys and SDK path.**

#### How to create it:

```bash
# Option 1: Use make
make local-props

# Option 2: Copy template manually
cp local.properties.template local.properties
```

#### Required content:

```properties
# Android SDK path (Android Studio sets this automatically)
sdk.dir=/path/to/your/Android/sdk

# Google Maps API Key (REQUIRED)
MAPS_API_KEY=your_actual_maps_api_key_here
```

#### How to get a Maps API Key:

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create or select a project
3. Go to **APIs & Services** -> **Credentials**
4. Click **"Create Credentials"** -> **API Key**
5. (Recommended) Restrict the key to **Maps SDK for Android**
6. Copy the key to `local.properties`

---

### 3. SHA-1 Fingerprint (CRITICAL for Google Sign-In)

Google Sign-In requires your app's SHA-1 certificate fingerprint to be registered in Firebase.

#### Get your SHA-1:

```bash
# Using make
make sha1

# Or manually for debug keystore
keytool -list -v \
  -keystore ~/.android/debug.keystore \
  -alias androiddebugkey \
  -storepass android
```

#### Register in Firebase:

1. Go to Firebase Console -> Project Settings
2. Scroll to **"Your apps"** section
3. Click on your Android app
4. Click **"Add fingerprint"**
5. Paste the SHA1 value (format: `XX:XX:XX:XX:...`)
6. Save and **re-download** `google-services.json`

---

## Firebase Services Configuration

### Authentication Setup

1. **Firebase Console** -> Authentication -> Sign-in method
2. Enable **Email/Password**
3. Enable **Google**:
    - Click Google provider
    - Enable it
    - Add project support email
    - Save

### Firestore Setup

1. **Firebase Console** -> Firestore Database
2. Click **"Create database"**
3. Start in **production mode** (our rules will handle security)
4. Select a location close to your users

### Storage Setup

1. **Firebase Console** -> Storage
2. Click **"Get started"**
3. Use default rules for now

### Deploy Security Rules

```bash
# Login to Firebase CLI
firebase login

# Select your project
firebase use your-project-id

# Deploy rules
make firebase-rules
# or
firebase deploy --only firestore,storage
```

---

## Cloud Functions (Optional but Recommended)

Cloud Functions automatically sync organizer status to authentication claims.

### Deploy Functions:

```bash
# Install dependencies first
cd functions
npm install
cd ..

# Deploy
make firebase-functions
# or
firebase deploy --only functions
```

---

## Troubleshooting

### Build Fails: "google-services.json not found"

```
Execution failed for task ':app:processDebugGoogleServices'.
> File google-services.json is missing.
```

**Solution**: Download `google-services.json` from Firebase Console and place in `app/` directory.

---

### Google Sign-In Crashes: "No se pudo obtener el client_id"

```
IllegalStateException: No se pudo obtener el client_id de Google
```

**Solutions**:

1. Enable Google Sign-In in Firebase Authentication
2. Make sure SHA-1 fingerprint is registered
3. Re-download `google-services.json`
4. Rebuild the project (Build -> Rebuild Project)

---

### Maps Show Blank/Error

**Solutions**:

1. Check `MAPS_API_KEY` is set in `local.properties`
2. Verify the API key is valid in Google Cloud Console
3. Make sure Maps SDK for Android is enabled
4. Check if billing is enabled (required for some quotas)

---

### Sign-In Returns Error 10

```
ApiException: 10: DEVELOPER_ERROR
```

**This means SHA-1 fingerprint mismatch.**

Solutions:

1. Run `make sha1` to get your fingerprint
2. Add it to Firebase Console
3. Re-download `google-services.json`
4. Clean and rebuild: `make clean build`

---

### Sign-In Returns Error 12500

```
ApiException: 12500: SIGN_IN_CANCELLED
```

**Solutions**:

1. Check Google Sign-In is enabled in Firebase
2. Verify OAuth consent screen is configured
3. Make sure project support email is set

---

## Project Structure

```
MiUNIEventos/
|-- app/
|   |-- google-services.json     <- Firebase config (YOU MUST ADD)
|   |-- build.gradle.kts
|   +-- src/
|-- functions/                    <- Cloud Functions
|   |-- index.js
|   +-- package.json
|-- local.properties              <- API keys (YOU MUST ADD)
|-- local.properties.template     <- Template for local.properties
|-- firebase.json                 <- Firebase project config
|-- firestore.rules               <- Firestore security rules
|-- storage.rules                 <- Storage security rules
|-- Makefile                      <- Quick commands
|-- scripts/
|   +-- setup.sh                  <- Setup verification script
+-- SETUP.md                      <- This file
```

---

## Configuration Checklist

Use this checklist to ensure everything is set up:

-   [ ] `google-services.json` exists in `app/` directory
-   [ ] Firebase project created
-   [ ] Authentication enabled (Email/Password + Google)
-   [ ] Firestore database created
-   [ ] Storage enabled
-   [ ] SHA-1 fingerprint added to Firebase
-   [ ] `local.properties` created with `MAPS_API_KEY`
-   [ ] Maps API key obtained from Google Cloud Console
-   [ ] Security rules deployed (`make firebase-rules`)
-   [ ] (Optional) Cloud Functions deployed (`make firebase-functions`)

---

## For Teachers/Evaluators

If you're evaluating this project:

1. **Create your own Firebase project** - It's free for development use
2. **Follow the Quick Start** - The setup script will guide you
3. **Use test credentials** - You can create test users for evaluation
4. **Check the Makefile** - Use `make help` for available commands

If you encounter issues, the most common problems are:

-   Missing `google-services.json`
-   SHA-1 fingerprint not registered
-   Google Sign-In not enabled in Firebase

---

## Support

If you're stuck:

1. Run `make setup` to diagnose issues
2. Check the Troubleshooting section above
3. Review Firebase Console configurations
4. Open an issue on GitHub with error logs

---

Built for university communities

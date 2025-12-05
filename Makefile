# =============================================================================
# MiUNIEventos - Makefile
# =============================================================================
# Quick commands for project setup and development
# Usage: make <command>
# =============================================================================

.PHONY: help setup check clean build run firebase-deploy firebase-emulator

# Default target
help:
	@echo ""
	@echo "╔══════════════════════════════════════════════════════════════════════╗"
	@echo "║              MiUNIEventos - Available Commands                       ║"
	@echo "╚══════════════════════════════════════════════════════════════════════╝"
	@echo ""
	@echo "  Setup & Configuration:"
	@echo "    make setup          - Run full project setup check"
	@echo "    make check          - Quick configuration check"
	@echo "    make local-props    - Create local.properties from template"
	@echo ""
	@echo "  Build & Run:"
	@echo "    make build          - Build debug APK"
	@echo "    make build-release  - Build release APK"
	@echo "    make clean          - Clean build artifacts"
	@echo "    make run            - Install and run on connected device"
	@echo ""
	@echo "  Firebase:"
	@echo "    make firebase-login - Login to Firebase CLI"
	@echo "    make firebase-init  - Initialize Firebase project"
	@echo "    make firebase-deploy - Deploy all Firebase services"
	@echo "    make firebase-rules - Deploy Firestore & Storage rules"
	@echo "    make firebase-functions - Deploy Cloud Functions"
	@echo "    make firebase-emulator - Start Firebase emulators"
	@echo ""
	@echo "  Utilities:"
	@echo "    make sha1           - Get SHA-1 fingerprint for Firebase"
	@echo "    make deps           - Install all dependencies"
	@echo ""

# =============================================================================
# SETUP COMMANDS
# =============================================================================

setup:
	@chmod +x scripts/setup.sh
	@./scripts/setup.sh

check:
	@echo "Quick Configuration Check..."
	@echo ""
	@echo "google-services.json:"
	@if [ -f "app/google-services.json" ]; then \
		echo "   [OK] Found"; \
	else \
		echo "   [MISSING] Download from Firebase Console"; \
	fi
	@echo ""
	@echo "local.properties:"
	@if [ -f "local.properties" ]; then \
		echo "   [OK] Found"; \
		if grep -q "^MAPS_API_KEY=.*[a-zA-Z0-9]" local.properties 2>/dev/null; then \
			echo "   [OK] MAPS_API_KEY configured"; \
		else \
			echo "   [WARN] MAPS_API_KEY not set or is placeholder"; \
		fi \
	else \
		echo "   [MISSING] Run: make local-props"; \
	fi
	@echo ""

local-props:
	@if [ -f "local.properties" ]; then \
		echo "[WARN] local.properties already exists!"; \
		read -p "Overwrite? (y/N): " confirm; \
		if [ "$$confirm" = "y" ] || [ "$$confirm" = "Y" ]; then \
			cp local.properties.template local.properties; \
			echo "[OK] Created local.properties from template"; \
			echo "-> Edit local.properties and add your MAPS_API_KEY"; \
		fi \
	else \
		cp local.properties.template local.properties; \
		echo "[OK] Created local.properties from template"; \
		echo "-> Edit local.properties and add your MAPS_API_KEY"; \
	fi

# =============================================================================
# BUILD COMMANDS
# =============================================================================

clean:
	@echo "Cleaning build artifacts..."
	./gradlew clean
	@echo "Clean complete"

build:
	@echo "Building debug APK..."
	./gradlew assembleDebug
	@echo ""
	@echo "APK location: app/build/outputs/apk/debug/app-debug.apk"

build-release:
	@echo "Building release APK..."
	./gradlew assembleRelease
	@echo ""
	@echo "APK location: app/build/outputs/apk/release/"

run:
	@echo "Installing and running on device..."
	./gradlew installDebug
	adb shell am start -n com.domicoder.miunieventos/.MainActivity

# =============================================================================
# FIREBASE COMMANDS
# =============================================================================

firebase-login:
	@echo "Logging into Firebase..."
	firebase login

firebase-init:
	@echo "Initializing Firebase project..."
	firebase use --add

firebase-deploy:
	@echo "Deploying all Firebase services..."
	firebase deploy
	@echo "Deployment complete"

firebase-rules:
	@echo "Deploying Firestore & Storage rules..."
	firebase deploy --only firestore,storage
	@echo "Rules deployed"

firebase-functions:
	@echo "Deploying Cloud Functions..."
	@cd functions && npm install
	firebase deploy --only functions
	@echo "Functions deployed"

firebase-emulator:
	@echo "Starting Firebase Emulators..."
	firebase emulators:start

# =============================================================================
# UTILITY COMMANDS
# =============================================================================

sha1:
	@echo ""
	@echo "SHA-1 Fingerprints for Firebase Console:"
	@echo ""
	@echo "━━━ Debug Certificate ━━━"
	@if [ -f ~/.android/debug.keystore ]; then \
		keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android 2>/dev/null | grep SHA1 || \
		keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android 2>/dev/null | grep "SHA1:"; \
	else \
		echo "Debug keystore not found at ~/.android/debug.keystore"; \
		echo "Build the project once in Android Studio to generate it."; \
	fi
	@echo ""
	@echo "━━━ Release Certificate ━━━"
	@if [ -f "miuni-eventos-release-key.jks" ]; then \
		echo "Found release keystore. Run manually with your keystore password:"; \
		echo "keytool -list -v -keystore miuni-eventos-release-key.jks -alias <your-alias>"; \
	else \
		echo "No release keystore found in project root."; \
	fi
	@echo ""
	@echo "Copy the SHA1 fingerprint and add it to Firebase Console:"
	@echo "   Firebase Console → Project Settings → Your Apps → Add Fingerprint"
	@echo ""

deps:
	@echo "Installing all dependencies..."
	@echo ""
	@echo "Gradle dependencies..."
	./gradlew dependencies --quiet
	@echo ""
	@echo "Firebase Functions dependencies..."
	@cd functions && npm install
	@echo ""
	@echo "All dependencies installed"

# Show example google-services.json location
sample-google-services:
	@echo ""
	@echo "A sample google-services.json structure is available at:"
	@echo "   app/google-services.json.example"
	@echo ""
	@echo "This shows the expected format. Download your actual file from Firebase Console."
	@echo ""


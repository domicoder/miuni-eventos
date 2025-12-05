#!/bin/bash

# =============================================================================
# MiUNIEventos - Project Setup Script
# =============================================================================
# This script helps new developers set up the project by checking all
# required configurations and guiding them through the setup process.
# =============================================================================

# Don't exit on error - we want to show all issues
# set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Project root directory (one level up from scripts)
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo -e "${CYAN}"
echo "========================================================================"
echo "               MiUNIEventos - Project Setup                             "
echo "========================================================================"
echo -e "${NC}"

# Track issues
ISSUES=0
WARNINGS=0

# Function to print status
print_check() {
    if [ "$2" = "ok" ]; then
        echo -e "  ${GREEN}[OK]${NC} $1"
    elif [ "$2" = "warn" ]; then
        echo -e "  ${YELLOW}[WARN]${NC} $1"
        WARNINGS=$((WARNINGS + 1))
    else
        echo -e "  ${RED}[FAIL]${NC} $1"
        ISSUES=$((ISSUES + 1))
    fi
}

print_header() {
    echo ""
    echo -e "${BLUE}--- $1 ---${NC}"
}

# =============================================================================
# CHECK 1: google-services.json
# =============================================================================
print_header "Firebase Configuration (google-services.json)"

if [ -f "$PROJECT_ROOT/app/google-services.json" ]; then
    # Check if it contains valid project info
    if grep -q "project_id" "$PROJECT_ROOT/app/google-services.json" 2>/dev/null; then
        PROJECT_ID=$(grep -o '"project_id": *"[^"]*"' "$PROJECT_ROOT/app/google-services.json" | head -1 | cut -d'"' -f4)
        print_check "google-services.json found (Project: $PROJECT_ID)" "ok"
        
        # Check for OAuth client
        if grep -q "oauth_client" "$PROJECT_ROOT/app/google-services.json"; then
            if grep -q '"client_type": *"3"' "$PROJECT_ROOT/app/google-services.json"; then
                print_check "Web OAuth client found (required for Google Sign-In)" "ok"
            else
                print_check "Web OAuth client (client_type: 3) NOT found - Google Sign-In will fail!" "fail"
                echo -e "    ${YELLOW}-> Enable Google Sign-In in Firebase Console Authentication${NC}"
            fi
        else
            print_check "OAuth clients NOT configured - Google Sign-In will crash!" "fail"
        fi
    else
        print_check "google-services.json appears invalid or empty" "fail"
    fi
else
    print_check "google-services.json NOT FOUND - Build will fail!" "fail"
    echo ""
    echo -e "  ${YELLOW}To fix this:${NC}"
    echo "  1. Go to https://console.firebase.google.com/"
    echo "  2. Create a new project or select existing one"
    echo "  3. Add an Android app with package: com.domicoder.miunieventos"
    echo "  4. Download google-services.json"
    echo "  5. Place it in: $PROJECT_ROOT/app/"
fi

# =============================================================================
# CHECK 2: local.properties
# =============================================================================
print_header "Local Properties (API Keys)"

if [ -f "$PROJECT_ROOT/local.properties" ]; then
    print_check "local.properties file exists" "ok"
    
    # Check for SDK dir
    if grep -q "^sdk.dir=" "$PROJECT_ROOT/local.properties"; then
        print_check "Android SDK path configured" "ok"
    else
        print_check "Android SDK path not set (Android Studio will set this automatically)" "warn"
    fi
    
    # Check for Maps API key
    if grep -q "^MAPS_API_KEY=" "$PROJECT_ROOT/local.properties"; then
        MAPS_KEY=$(grep "^MAPS_API_KEY=" "$PROJECT_ROOT/local.properties" | cut -d'=' -f2)
        if [ -n "$MAPS_KEY" ] && [ "$MAPS_KEY" != "your_actual_maps_api_key_here" ]; then
            # Mask the key for security
            MASKED_KEY="${MAPS_KEY:0:8}...${MAPS_KEY: -4}"
            print_check "Maps API key configured ($MASKED_KEY)" "ok"
        else
            print_check "Maps API key is placeholder - Maps will not work!" "fail"
        fi
    else
        print_check "MAPS_API_KEY not found - Maps feature will crash!" "fail"
    fi
else
    print_check "local.properties NOT FOUND" "fail"
    echo ""
    echo -e "  ${YELLOW}Creating from template...${NC}"
    
    if [ -f "$PROJECT_ROOT/local.properties.template" ]; then
        cp "$PROJECT_ROOT/local.properties.template" "$PROJECT_ROOT/local.properties"
        echo -e "  ${GREEN}[OK]${NC} Created local.properties from template"
        echo -e "  ${YELLOW}-> Please edit local.properties and add your MAPS_API_KEY${NC}"
    else
        echo -e "  ${RED}Template not found, creating basic file...${NC}"
        cat > "$PROJECT_ROOT/local.properties" << 'EOF'
# Android SDK location (will be set automatically by Android Studio)
# sdk.dir=/path/to/your/android/sdk

# Google Maps API Key
# Get your key from: https://console.cloud.google.com/apis/credentials
MAPS_API_KEY=your_maps_api_key_here
EOF
        echo -e "  ${GREEN}[OK]${NC} Created local.properties"
        echo -e "  ${YELLOW}-> Please edit and add your MAPS_API_KEY${NC}"
    fi
fi

# =============================================================================
# CHECK 3: Firebase CLI & Project
# =============================================================================
print_header "Firebase CLI"

if command -v firebase &> /dev/null; then
    FIREBASE_VERSION=$(firebase --version 2>/dev/null)
    print_check "Firebase CLI installed (v$FIREBASE_VERSION)" "ok"
    
    # Check if logged in
    if firebase projects:list &> /dev/null; then
        print_check "Firebase CLI authenticated" "ok"
    else
        print_check "Firebase CLI not logged in" "warn"
        echo -e "    ${YELLOW}-> Run: firebase login${NC}"
    fi
else
    print_check "Firebase CLI not installed" "warn"
    echo -e "    ${YELLOW}-> Install with: npm install -g firebase-tools${NC}"
    echo -e "    ${YELLOW}-> Required for: deploying functions, rules, and hosting${NC}"
fi

# =============================================================================
# CHECK 4: Node.js for Cloud Functions
# =============================================================================
print_header "Node.js (for Cloud Functions)"

if command -v node &> /dev/null; then
    NODE_VERSION=$(node --version)
    print_check "Node.js installed ($NODE_VERSION)" "ok"
    
    # Check node_modules in functions
    if [ -d "$PROJECT_ROOT/functions/node_modules" ]; then
        print_check "Functions dependencies installed" "ok"
    else
        print_check "Functions dependencies not installed" "warn"
        echo -e "    ${YELLOW}-> Run: cd functions && npm install${NC}"
    fi
else
    print_check "Node.js not installed" "warn"
    echo -e "    ${YELLOW}-> Required for Cloud Functions development${NC}"
    echo -e "    ${YELLOW}-> Install from: https://nodejs.org/${NC}"
fi

# =============================================================================
# CHECK 5: Java/JDK
# =============================================================================
print_header "Java Development Kit"

if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    print_check "Java installed: $JAVA_VERSION" "ok"
    
    # Check if it's Java 17+
    if java -version 2>&1 | grep -q "version \"17\|version \"18\|version \"19\|version \"20\|version \"21"; then
        print_check "Java version is 17+ (required)" "ok"
    else
        print_check "Java version should be 17+ for this project" "warn"
    fi
else
    print_check "Java not found in PATH" "fail"
    echo -e "    ${YELLOW}-> Install JDK 17: https://adoptium.net/${NC}"
fi

# =============================================================================
# CHECK 6: Gradle Wrapper
# =============================================================================
print_header "Gradle"

if [ -f "$PROJECT_ROOT/gradlew" ]; then
    print_check "Gradle wrapper present" "ok"
    
    if [ -x "$PROJECT_ROOT/gradlew" ]; then
        print_check "Gradle wrapper is executable" "ok"
    else
        print_check "Gradle wrapper not executable" "warn"
        echo -e "    ${YELLOW}-> Fixing permissions...${NC}"
        chmod +x "$PROJECT_ROOT/gradlew"
        print_check "Fixed gradlew permissions" "ok"
    fi
else
    print_check "Gradle wrapper not found!" "fail"
fi

# =============================================================================
# SUMMARY
# =============================================================================
echo ""
echo -e "${CYAN}========================================================================${NC}"
echo -e "${CYAN}                           SUMMARY                                      ${NC}"
echo -e "${CYAN}========================================================================${NC}"
echo ""

if [ $ISSUES -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}[OK] All checks passed! Project is ready to build.${NC}"
    echo ""
    echo -e "Next steps:"
    echo -e "  1. Open project in Android Studio"
    echo -e "  2. Sync Gradle files"
    echo -e "  3. Run on emulator or device"
elif [ $ISSUES -eq 0 ]; then
    echo -e "${YELLOW}[WARN] $WARNINGS warning(s) found - Project should build but some features may not work${NC}"
else
    echo -e "${RED}[FAIL] $ISSUES critical issue(s) found - Project will NOT build!${NC}"
    if [ $WARNINGS -gt 0 ]; then
        echo -e "${YELLOW}[WARN] Plus $WARNINGS warning(s)${NC}"
    fi
fi

echo ""
echo -e "${BLUE}--- Required Firebase Services ---${NC}"
echo "  * Authentication (with Google Sign-In enabled)"
echo "  * Cloud Firestore"
echo "  * Cloud Storage"
echo "  * Cloud Functions (optional, for organizer claims)"
echo ""

if [ $ISSUES -gt 0 ]; then
    echo -e "${RED}Please fix the critical issues above before building.${NC}"
    exit 1
fi

exit 0

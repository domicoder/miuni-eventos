# MiUNIEventos

A modern Android app for university event management and discovery, built with Jetpack Compose and modern Android development practices.

## Overview

MiUNIEventos helps university students and staff discover, manage, and participate in campus events. The app provides a seamless experience for both event organizers and attendees, featuring real-time updates, QR code scanning for attendance tracking, and integrated maps for event locations.

## Features

### For Attendees

-   **Event Discovery**: Browse and search through university events with advanced filtering
-   **RSVP Management**: Confirm attendance to events with different status options
-   **Event Details**: View comprehensive event information including location, organizer details, and descriptions
-   **Calendar Integration**: Add events directly to your device calendar
-   **Event Sharing**: Share events with friends and colleagues

### For Organizers

-   **QR Code Scanner**: Scan attendee QR codes for real-time attendance tracking
-   **Event Management**: Create and edit events with rich details
-   **Attendance Analytics**: Track event participation and engagement
-   **Organizer Profile**: Manage your organized events and profile information

### Core Features

-   **Modern UI**: Built with Material Design 3 and Jetpack Compose
-   **Offline Support**: Events cached locally for offline viewing
-   **Real-time Updates**: Firebase integration for live event updates
-   **Maps Integration**: Google Maps for event location visualization
-   **Push Notifications**: Stay updated on event changes and reminders
-   **QR Code System**: Complete QR code generation and scanning for event attendance

## QR Code Functionality

The app includes a comprehensive QR code system for event attendance:

### QR Code Format

QR codes contain data in the format: `event_id:user_id`

Example: `event1:user3`

### How to Use

#### For Organizers:

1. Navigate to the "Escanear QR" (Scan QR) tab in the bottom navigation
2. Point the camera at an attendee's QR code
3. The app will automatically process the QR code and check in the attendee
4. View real-time feedback on the scan result

#### For Attendees:

1. Receive a QR code from the event organizer
2. Present the QR code to be scanned
3. Get instant confirmation of check-in

### Technical Implementation

-   **Scanner**: Uses ZXing library for robust QR code detection
-   **Camera**: Integrated camera permissions and lifecycle management
-   **Processing**: Real-time QR code validation and RSVP updates
-   **UI**: Material Design 3 with smooth animations and feedback

## Tech Stack

-   **UI Framework**: Jetpack Compose with Material Design 3
-   **Language**: Kotlin
-   **Architecture**: MVVM with Clean Architecture principles
-   **Dependency Injection**: Hilt
-   **Database**: Room for local storage
-   **Backend**: Firebase (Authentication, Firestore, Analytics, Messaging)
-   **Maps**: Google Maps with Compose integration
-   **Image Loading**: Coil
-   **QR Code**: ZXing library
-   **Navigation**: Jetpack Navigation Compose
-   **State Management**: Kotlin Flow and StateFlow

## Project Structure

```
app/src/main/java/com/domicoder/miunieventos/
├── data/
│   ├── model/          # Data models and entities
│   ├── repository/     # Repository implementations
│   └── local/          # Room database and DAOs
├── di/                 # Hilt dependency injection modules
├── domain/
│   ├── model/          # Domain models
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Business logic use cases
├── ui/
│   ├── components/     # Reusable UI components
│   ├── discover/       # Event discovery screen
│   ├── eventdetail/    # Event detail screen
│   ├── myevents/       # User's events screen
│   ├── profile/        # User profile screen
│   ├── scanner/        # QR code scanner screen
│   ├── navigation/     # Navigation setup
│   └── theme/          # App theming
└── util/               # Utility classes and extensions
```

## Setup Instructions

### Prerequisites

-   Android Studio Hedgehog or later
-   JDK 17
-   Android SDK 34
-   Google Maps API key
-   Firebase project setup

### Installation

1. **Clone the repository**

    ```bash
    git clone https://github.com/domicoder/miuni-eventos.git
    cd miuni-eventos
    ```

2. **Configure Firebase**

    - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
    - Download `google-services.json` and place it in the `app/` directory
    - Enable Authentication, Firestore, Analytics, and Cloud Messaging

3. **Configure Google Maps**

    - Get a Maps API key from [Google Cloud Console](https://console.cloud.google.com/)
    - Copy `local.properties.template` to `local.properties`
    - Replace `your_actual_maps_api_key_here` with your actual Maps API key

4. **Build and Run**

    ```bash
    ./gradlew build
    ```

    Or open the project in Android Studio and run it on an emulator or device.

### Environment Configuration

The project uses the following key configurations:

-   **Java Version**: 17 (required for Android Gradle Plugin 8.1.4)
-   **Kotlin Version**: 1.9.0
-   **Compose Compiler**: 1.5.1
-   **Minimum SDK**: 26 (Android 8.0)
-   **Target SDK**: 34 (Android 14)

## Development Notes

### Location Permission Handling

The app includes proper runtime permission handling for location access:

-   **Permission Declaration**: Location permissions are declared in `AndroidManifest.xml`
-   **Runtime Permission**: The app requests location permissions at runtime using `ActivityResultContracts.RequestMultiplePermissions`
-   **Graceful Degradation**: If location permission is denied, the map still works but without user location features
-   **User-Friendly Dialog**: A dialog explains why location permission is needed before requesting it
-   **Permission State Management**: Uses `PermissionHandler` utility to check and manage permission states

### Build Configuration

The project has been configured to work with modern Android development tools and avoids common build issues:

-   JDK image transformation disabled to prevent SDK 34 compatibility issues
-   Proper Java module access configuration for KAPT
-   Optimized Gradle configuration for faster builds

### Key Dependencies

-   **Android Gradle Plugin**: 8.1.4
-   **Compose BOM**: 2023.10.01
-   **Navigation**: 2.7.5
-   **Room**: 2.6.1
-   **Firebase BOM**: 32.7.0
-   **Hilt**: 2.48

## Some Help

#### 1. Set the JDK Location in Android Studio

1. Go to Preferences (macOS) or File > Settings (Windows/Linux)
2. Navigate to Build, Execution, Deployment > Build Tools > Gradle
3. Under Gradle JDK, select your installed Java 17 JDK (not the "Embedded JDK" or any version below 17).

-   The path should be:
    `/usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home` as a `example`.
    If it doesn't appear, click "Add JDK" and point to that path.

### 2. Sync and Rebuild

-   Click File > Sync Project with Gradle Files
-   Then try to Build or Run your app again

## Contributing (Close for now)

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

-   Material Design 3 for the beautiful UI components
-   Firebase team for the robust backend services
-   Google Maps team for location services
-   The Android and Kotlin communities for excellent documentation and tools

---

Built with ❤️ for university communities

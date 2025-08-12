# QR Scanner Implementation - MiUNIEventos

## Overview

This document describes the enhanced QR scanner functionality implemented in the MiUNIEventos Android application.

## Features

### 1. Test QR Code Generation

-   **Multiple Test QR Codes**: The system provides 9 different test QR codes for development and testing
-   **Interactive Selection**: Users can select from different test QR codes to generate
-   **Visual Feedback**: Selected QR codes are highlighted and displayed with descriptions

### 2. Enhanced QR Code Validation

-   **Format Validation**: Ensures QR codes follow the required format: `event_id:user_id`
-   **Content Extraction**: Safely extracts event and user IDs from QR content
-   **Error Handling**: Provides specific error messages for different validation failures

### 3. Improved User Experience

-   **Better UI**: Enhanced visual design with cards, colors, and proper spacing
-   **Helpful Tips**: Context-aware suggestions based on error types
-   **Scanner Instructions**: Clear guidance for users on how to use the scanner

### 4. Business Logic Enhancements

-   **Event Validation**: Checks if the event exists and is currently in progress
-   **RSVP Verification**: Ensures users have confirmed attendance before check-in
-   **Duplicate Prevention**: Prevents multiple check-ins for the same user at the same event
-   **Timestamp Recording**: Records exact check-in time for audit purposes

## Test QR Codes Available

| QR Code        | Description                             |
| -------------- | --------------------------------------- |
| `event1:user3` | Conferencia AI - Carlos Rodríguez       |
| `event2:user3` | Taller Fotografía - Carlos Rodríguez    |
| `event3:user1` | Torneo Fútbol - Juanito Alimaña         |
| `event1:user2` | Conferencia AI - María García           |
| `event2:user1` | Taller Fotografía - Juanito Alimaña     |
| `event3:user3` | Torneo Fútbol - Carlos Rodríguez        |
| `event4:user1` | Workshop Programación - Juanito Alimaña |
| `event4:user2` | Workshop Programación - María García    |
| `event5:user3` | Concierto Jazz - Carlos Rodríguez       |

## How to Use

### 1. Generate Test QR Codes

1. Navigate to the Scanner screen
2. Select a test QR code from the list
3. The QR code will be generated and displayed
4. Use this QR code to test the scanner functionality

### 2. Scan QR Codes

1. Point the camera at a QR code
2. The system will automatically process the QR content
3. If valid, the user will be checked in
4. If there's an error, helpful suggestions will be displayed

### 3. Handle Errors

The system provides specific error messages and suggestions:

-   **Invalid Format**: Shows the expected format
-   **Event Not Found**: Suggests verifying the event ID
-   **No RSVP**: Reminds that attendance must be confirmed first
-   **Already Checked In**: Indicates duplicate registration

## Technical Implementation

### Files Modified

-   `ScannerScreen.kt` - Enhanced UI with better visual design
-   `ScannerViewModel.kt` - Improved business logic and error handling
-   `QRCodeGenerator.kt` - Added utility functions and more test data
-   `strings.xml` - Added new string resources

### Key Components

-   **QRScanner**: Camera-based QR code scanner using ZXing
-   **Test QR Generation**: Interactive test QR code creation
-   **Error Handling**: Comprehensive error management with user-friendly messages
-   **Validation**: Multi-layer validation for QR content and business rules

### Dependencies

-   `zxing-core`: QR code generation
-   `zxing-android-embedded`: Camera-based QR scanning
-   `compose-material3`: Modern Material Design components

## Future Enhancements

1. **QR Code History**: Track all scanned codes for audit purposes
2. **Offline Support**: Handle cases when database is unavailable
3. **Batch Processing**: Process multiple QR codes simultaneously
4. **Analytics**: Track scanning patterns and success rates
5. **Custom QR Formats**: Support for different QR code structures

## Testing

To test the QR scanner:

1. Use the test QR codes provided
2. Try scanning invalid formats to test error handling
3. Test with different event and user combinations
4. Verify that duplicate check-ins are prevented
5. Check that only valid RSVPs can be processed

## Troubleshooting

### Common Issues

-   **Camera Permission**: Ensure camera permission is granted
-   **QR Format**: Verify QR codes follow the `event_id:user_id` format
-   **Event Existence**: Ensure the event ID exists in the database
-   **RSVP Status**: Confirm that the user has RSVP'd to the event

### Debug Information

-   Check the console logs for detailed error information
-   Use the test QR codes to verify scanner functionality
-   Monitor the ViewModel state changes for debugging

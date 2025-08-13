package com.domicoder.miunieventos.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QRCodeGenerator {

    fun generateQRCode(text: String, width: Int = 512, height: Int = 512): Bitmap? {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 2)
            }

            val writer = QRCodeWriter()
            val bitMatrix: BitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height, hints)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    fun generateAttendanceQRCode(eventId: String, userId: String, width: Int = 512, height: Int = 512): Bitmap? {
        val qrData = "$eventId:$userId"
        return generateQRCode(qrData, width, height)
    }
    
    /**
     * Generate QR code for event check-in
     * This will be used by organizers to generate QR codes for their events
     */
    fun generateEventCheckInQRCode(eventId: String, width: Int = 512, height: Int = 512): Bitmap? {
        val qrData = "checkin:$eventId"
        return generateQRCode(qrData, width, height)
    }
    
    /**
     * Generate QR code for event registration
     * This will be used by organizers to generate QR codes for event registration
     */
    fun generateEventRegistrationQRCode(eventId: String, width: Int = 512, height: Int = 512): Bitmap? {
        val qrData = "register:$eventId"
        return generateQRCode(qrData, width, height)
    }
    
    /**
     * Validate QR code format
     * Expected formats: 
     * - "event_id:user_id" for attendance
     * - "checkin:event_id" for event check-in
     * - "register:event_id" for event registration
     */
    fun isValidQRFormat(qrContent: String): Boolean {
        val parts = qrContent.split(":")
        return when {
            parts.size == 2 && parts[0] == "checkin" -> parts[1].isNotBlank()
            parts.size == 2 && parts[0] == "register" -> parts[1].isNotBlank()
            parts.size == 2 -> parts[0].isNotBlank() && parts[1].isNotBlank()
            else -> false
        }
    }
    
    /**
     * Extract event ID from QR content
     */
    fun extractEventId(qrContent: String): String? {
        return if (isValidQRFormat(qrContent)) {
            when {
                qrContent.startsWith("checkin:") -> qrContent.substringAfter("checkin:")
                qrContent.startsWith("register:") -> qrContent.substringAfter("register:")
                else -> qrContent.split(":")[0]
            }
        } else null
    }
    
    /**
     * Extract user ID from QR content
     */
    fun extractUserId(qrContent: String): String? {
        return if (isValidQRFormat(qrContent) && !qrContent.startsWith("checkin:") && !qrContent.startsWith("register:")) {
            qrContent.split(":")[1]
        } else null
    }
    
    /**
     * Get QR code type
     */
    fun getQRCodeType(qrContent: String): QRCodeType {
        return when {
            qrContent.startsWith("checkin:") -> QRCodeType.CHECK_IN
            qrContent.startsWith("register:") -> QRCodeType.REGISTRATION
            qrContent.contains(":") -> QRCodeType.ATTENDANCE
            else -> QRCodeType.UNKNOWN
        }
    }
}

enum class QRCodeType {
    ATTENDANCE,    // event_id:user_id
    CHECK_IN,      // checkin:event_id
    REGISTRATION,  // register:event_id
    UNKNOWN
}

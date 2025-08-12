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
     * Generate test QR codes for development and testing
     * Returns a list of QR data and their descriptions
     */
    fun generateTestQRCodes(): List<Pair<String, String>> {
        return listOf(
            "event1:user3" to "Conferencia AI - Carlos Rodríguez",
            "event2:user3" to "Taller Fotografía - Carlos Rodríguez", 
            "event3:user1" to "Torneo Fútbol - Juanito Alimaña",
            "event1:user2" to "Conferencia AI - María García",
            "event2:user1" to "Taller Fotografía - Juanito Alimaña",
            "event3:user3" to "Torneo Fútbol - Carlos Rodríguez",
            "event4:user1" to "Workshop Programación - Juanito Alimaña",
            "event4:user2" to "Workshop Programación - María García",
            "event5:user3" to "Concierto Jazz - Carlos Rodríguez"
        )
    }
    
    /**
     * Get valid QR code data for testing
     */
    fun getValidQRCodeData(): String {
        // This returns a valid QR code format that the scanner expects
        return "event1:user3"
    }
    
    /**
     * Get a random test QR code for variety in testing
     */
    fun getRandomTestQRCode(): String {
        val testCodes = generateTestQRCodes()
        return testCodes.random().first
    }
    
    /**
     * Validate QR code format
     * Expected format: "event_id:user_id"
     */
    fun isValidQRFormat(qrContent: String): Boolean {
        val parts = qrContent.split(":")
        return parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()
    }
    
    /**
     * Extract event ID from QR content
     */
    fun extractEventId(qrContent: String): String? {
        return if (isValidQRFormat(qrContent)) {
            qrContent.split(":")[0]
        } else null
    }
    
    /**
     * Extract user ID from QR content
     */
    fun extractUserId(qrContent: String): String? {
        return if (isValidQRFormat(qrContent)) {
            qrContent.split(":")[1]
        } else null
    }
}

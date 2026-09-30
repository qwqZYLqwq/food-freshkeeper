package com.food.freshkeeper.util

import org.junit.Test
import org.junit.Assert.*

class ImageSaverSanitizeTest {
    @Test
    fun testDefaultFallback() {
        assertEquals("Download/food", ImageSaver.DEFAULT_IMAGE_SAVE_PATH)
        assertEquals("Download/food", ImageSaver.sanitizeImageSavePath(null))
        assertEquals("Download/food", ImageSaver.sanitizeImageSavePath(""))
        assertEquals("Download/food", ImageSaver.sanitizeImageSavePath("   "))
    }
}

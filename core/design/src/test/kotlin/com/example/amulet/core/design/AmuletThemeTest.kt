package com.example.amulet.core.design

import com.example.amulet.core.design.foundation.color.AmuletPalette
import org.junit.Assert.assertEquals
import org.junit.Test

class AmuletThemeTest {

    @Test
    fun primaryColor_matchesSpec() {
        assertEquals(0xFF6B73FFu, AmuletPalette.Primary.value)
    }
}

package com.brunoshiroma.vibememory.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SizeSelectionTest {

    @Test
    fun `computes bytes from magnitude and unit`() {
        assertEquals(4L * 1024, SizeSelection(4, SizeUnit.KIB).bytes)
        assertEquals(2L * 1024 * 1024, SizeSelection(2, SizeUnit.MIB).bytes)
        assertEquals(1L * 1024 * 1024 * 1024, SizeSelection(1, SizeUnit.GIB).bytes)
    }

    @Test
    fun `label combines magnitude and unit`() {
        assertEquals("16 KiB", SizeSelection(16, SizeUnit.KIB).label)
        assertEquals("2 GiB", SizeSelection(2, SizeUnit.GIB).label)
    }

    @Test
    fun `rejects non-positive sizes`() {
        // Every unit's bytesPerUnit is itself a multiple of 8, so the only way
        // isValid can fail is when the resulting byte count isn't positive.
        assertFalse(SizeSelection(0, SizeUnit.KIB).isValid)
        assertFalse(SizeSelection(-1, SizeUnit.KIB).isValid)
    }

    @Test
    fun `accepts every default size`() {
        SizeSelection.DEFAULTS.forEach { assertTrue(it.isValid) }
    }
}

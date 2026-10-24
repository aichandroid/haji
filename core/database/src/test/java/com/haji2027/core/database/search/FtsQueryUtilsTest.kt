package com.haji2027.core.database.search

import org.junit.Assert.assertEquals
import org.junit.Test

class FtsQueryUtilsTest {

    @Test
    fun `sanitizeFtsQuery returns empty for null or blank input`() {
        assertEquals("", FtsQueryUtils.sanitizeFtsQuery(null))
        assertEquals("", FtsQueryUtils.sanitizeFtsQuery(""))
        assertEquals("", FtsQueryUtils.sanitizeFtsQuery("   "))
    }

    @Test
    fun `sanitizeFtsQuery removes fts control characters`() {
        assertEquals("", FtsQueryUtils.sanitizeFtsQuery("***"))
        assertEquals("", FtsQueryUtils.sanitizeFtsQuery("\"\"\""))
        assertEquals("", FtsQueryUtils.sanitizeFtsQuery("'''()[]{}~+^-:"))
    }

    @Test
    fun `sanitizeFtsQuery appends wildcard prefix correctly`() {
        assertEquals("arafah*", FtsQueryUtils.sanitizeFtsQuery("arafah"))
        assertEquals("wukuf* arafah*", FtsQueryUtils.sanitizeFtsQuery("wukuf arafah"))
        assertEquals("tawaf* ifadah*", FtsQueryUtils.sanitizeFtsQuery("  tawaf   ifadah  "))
    }

    @Test
    fun `sanitizeFtsQuery cleans punctuation inside words`() {
        assertEquals("doa* arafah*", FtsQueryUtils.sanitizeFtsQuery("doa: 'arafah'"))
    }

    @Test
    fun `sanitizeFtsQuery handles Arabic input with wildcard`() {
        val result = FtsQueryUtils.sanitizeFtsQuery("الله")
        assertEquals("الله*", result)
    }
}

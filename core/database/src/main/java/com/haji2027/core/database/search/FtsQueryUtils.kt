package com.haji2027.core.database.search

/**
 * Utilitas untuk sanitasi query pencarian SQLite FTS4 agar terhindar dari
 * kesalahan sintaksis MATCH (misal: tanda petik atau karakter khusus tanpa pasangan).
 */
object FtsQueryUtils {

    /**
     * Membersihkan string input pencarian dan menambahkan wildcard prefix `*`
     * pada setiap token kata.
     * Contoh: "arafah wukuf" -> "arafah* wukuf*"
     */
    fun sanitizeFtsQuery(query: String?): String {
        if (query.isNullOrBlank()) return ""

        // Hapus karakter khusus sintaks FTS4 yang berisiko merusak parser query
        val cleaned = query
            .replace(Regex("[*\"'\\^\\(\\)\\[\\]\\{\\}\\+\\-\\~\\:]"), " ")
            .trim()

        if (cleaned.isBlank()) return ""

        val tokens = cleaned.split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        if (tokens.isEmpty()) return ""

        return tokens.joinToString(" ") { "$it*" }
    }
}

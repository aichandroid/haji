package com.haji2027.feature.guide.doa

import com.haji2027.core.model.DoaReading

enum class TextSizeScale(val multiplier: Float, val label: String) {
    NORMAL(1.0f, "Normal"),
    LARGE(1.3f, "Besar Lansia")
}

/**
 * State antarmuka untuk Layar SCR-03: Bacaan Doa Multilingual.
 */
data class DoaReadingUiState(
    val isLoading: Boolean = true,
    val reading: DoaReading? = null,
    val textSizeScale: TextSizeScale = TextSizeScale.NORMAL,
    val errorMessage: String? = null
)

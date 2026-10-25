package com.haji2027.feature.tracking.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.haji2027.core.designsystem.theme.HajjDimens
import com.haji2027.core.designsystem.theme.PrimaryDarkNavy
import java.security.MessageDigest

/**
 * Komponen Compose Canvas untuk merender representasi visual kode QR 2D Matrix
 * kontras tinggi (Dark Navy `#0C4A6E` di atas latar putih) tanpa dependensi eksternal berat.
 */
@Composable
fun QrCodeCanvas(
    payload: String,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    matrixDim: Int = 25
) {
    val matrix = remember(payload, matrixDim) {
        generateQrMatrix(payload, matrixDim)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
            .background(Color.White)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size - 24.dp)) {
            val cellSize = this.size.width / matrixDim

            for (row in 0 until matrixDim) {
                for (col in 0 until matrixDim) {
                    if (matrix[row][col]) {
                        drawRect(
                            color = PrimaryDarkNavy,
                            topLeft = Offset(col * cellSize, row * cellSize),
                            size = Size(cellSize + 0.5f, cellSize + 0.5f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Menghasilkan matriks boolean 2D untuk pola QR Code deterministik.
 * Meliputi 3 pola sudut penemu posisi (Finder Patterns 7x7) dan modul data payload.
 */
private fun generateQrMatrix(payload: String, dim: Int): Array<BooleanArray> {
    val matrix = Array(dim) { BooleanArray(dim) }

    // 1. Tanam Finder Patterns (Top-Left, Top-Right, Bottom-Left)
    drawFinderPattern(matrix, 0, 0)
    drawFinderPattern(matrix, 0, dim - 7)
    drawFinderPattern(matrix, dim - 7, 0)

    // 2. Tanam Timing Patterns (Pola garis sinkronisasi horizontal & vertikal)
    for (i in 8 until dim - 8) {
        val bit = (i % 2 == 0)
        matrix[6][i] = bit
        matrix[i][6] = bit
    }

    // 3. Tanam Data Modules berbasis hash deterministik dari string payload
    val hash = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray(Charsets.UTF_8))
    var bitIndex = 0

    for (row in 0 until dim) {
        for (col in 0 until dim) {
            // Hindari menimpa Finder Patterns dan Timing Patterns
            if (isReservedArea(row, col, dim)) continue

            val byteVal = hash[bitIndex % hash.size].toInt()
            val shift = (bitIndex / hash.size) % 8
            val isDark = ((byteVal shr shift) and 1) == 1
            matrix[row][col] = isDark
            bitIndex++
        }
    }

    return matrix
}

private fun drawFinderPattern(matrix: Array<BooleanArray>, startRow: Int, startCol: Int) {
    for (r in 0 until 7) {
        for (c in 0 until 7) {
            val isBorder = r == 0 || r == 6 || c == 0 || c == 6
            val isCenter = r in 2..4 && c in 2..4
            matrix[startRow + r][startCol + c] = isBorder || isCenter
        }
    }
}

private fun isReservedArea(row: Int, col: Int, dim: Int): Boolean {
    // Top-Left Finder
    if (row < 8 && col < 8) return true
    // Top-Right Finder
    if (row < 8 && col >= dim - 8) return true
    // Bottom-Left Finder
    if (row >= dim - 8 && col < 8) return true
    // Timing lines
    if (row == 6 || col == 6) return true
    return false
}

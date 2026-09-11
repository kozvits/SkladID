package com.kozvits.skladid.printer

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TsplCommandBuilderTest {

    private val white = 0xFFFFFFFF.toInt()
    private val black = 0xFF000000.toInt()

    private fun commandsAsAscii(bytes: ByteArray, upTo: Int): String =
        String(bytes, 0, upTo, Charsets.US_ASCII)

    @Test
    fun `header contains SIZE, GAP and CLS with whole-millimetre values formatted without decimals`() {
        val pixels = IntArray(8 * 8) { white }
        val bytes = TsplCommandBuilder.buildFromPixels(pixels, 8, 8, 40f, 30f)
        val text = String(bytes, Charsets.US_ASCII)

        assertThat(text).contains("SIZE 40 mm,30 mm\r\n")
        assertThat(text).contains("GAP 2 mm,0 mm\r\n")
        assertThat(text).contains("CLS\r\n")
    }

    @Test
    fun `header preserves fractional millimetre values`() {
        val pixels = IntArray(8 * 8) { white }
        val bytes = TsplCommandBuilder.buildFromPixels(pixels, 8, 8, 40.5f, 30.25f)
        val text = String(bytes, Charsets.US_ASCII)

        assertThat(text).contains("SIZE 40.5 mm,30.25 mm\r\n")
    }

    @Test
    fun `BITMAP command declares correct byte width and pixel height`() {
        // 17px wide needs ceil(17/8) = 3 bytes per row.
        val width = 17
        val height = 5
        val pixels = IntArray(width * height) { white }
        val bytes = TsplCommandBuilder.buildFromPixels(pixels, width, height, 40f, 30f)
        val text = String(bytes, Charsets.US_ASCII)

        assertThat(text).contains("BITMAP 0,0,3,5,0,")
    }

    @Test
    fun `ends with a PRINT command`() {
        val pixels = IntArray(8 * 8) { white }
        val bytes = TsplCommandBuilder.buildFromPixels(pixels, 8, 8, 40f, 30f)
        val text = String(bytes, Charsets.US_ASCII)

        assertThat(text.trimEnd()).endsWith("PRINT 1,1")
    }

    @Test
    fun `all-white row packs to a zero byte`() {
        val pixels = IntArray(8) { white }
        val packed = extractPackedBytes(pixels, width = 8, height = 1)

        assertThat(packed).hasLength(1)
        assertThat(packed[0].toInt() and 0xFF).isEqualTo(0x00)
    }

    @Test
    fun `all-black row packs to a fully-set byte`() {
        val pixels = IntArray(8) { black }
        val packed = extractPackedBytes(pixels, width = 8, height = 1)

        assertThat(packed).hasLength(1)
        assertThat(packed[0].toInt() and 0xFF).isEqualTo(0xFF)
    }

    @Test
    fun `leftmost black pixel sets the most significant bit`() {
        val pixels = IntArray(8) { white }
        pixels[0] = black
        val packed = extractPackedBytes(pixels, width = 8, height = 1)

        assertThat(packed[0].toInt() and 0xFF).isEqualTo(0x80)
    }

    @Test
    fun `rightmost black pixel sets the least significant bit`() {
        val pixels = IntArray(8) { white }
        pixels[7] = black
        val packed = extractPackedBytes(pixels, width = 8, height = 1)

        assertThat(packed[0].toInt() and 0xFF).isEqualTo(0x01)
    }

    @Test
    fun `each row is padded to a whole byte independently`() {
        // width=9 -> 2 bytes/row; only the first pixel of each row is black.
        val width = 9
        val height = 2
        val pixels = IntArray(width * height) { white }
        pixels[0] = black // row 0, col 0
        pixels[width] = black // row 1, col 0
        val packed = extractPackedBytes(pixels, width, height)

        assertThat(packed).hasLength(4) // 2 bytes/row * 2 rows
        assertThat(packed[0].toInt() and 0xFF).isEqualTo(0x80)
        assertThat(packed[1].toInt() and 0xFF).isEqualTo(0x00)
        assertThat(packed[2].toInt() and 0xFF).isEqualTo(0x80)
        assertThat(packed[3].toInt() and 0xFF).isEqualTo(0x00)
    }

    @Test
    fun `rejects a pixel array whose size does not match width times height`() {
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            TsplCommandBuilder.buildFromPixels(IntArray(10), width = 8, height = 8, labelWidthMm = 40f, labelHeightMm = 30f)
        }
    }

    /** Extracts just the packed monochrome image bytes (between the BITMAP prefix and the footer). */
    private fun extractPackedBytes(pixels: IntArray, width: Int, height: Int): ByteArray {
        val full = TsplCommandBuilder.buildFromPixels(pixels, width, height, 40f, 30f)
        val prefix = "BITMAP 0,0,${(width + 7) / 8},$height,0,".toByteArray(Charsets.US_ASCII)
        val start = indexOf(full, prefix) + prefix.size
        val widthBytes = (width + 7) / 8
        return full.copyOfRange(start, start + widthBytes * height)
    }

    private fun indexOf(haystack: ByteArray, needle: ByteArray): Int {
        outer@ for (i in 0..haystack.size - needle.size) {
            for (j in needle.indices) {
                if (haystack[i + j] != needle[j]) continue@outer
            }
            return i
        }
        throw AssertionError("Needle not found in haystack")
    }
}

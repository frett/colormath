package com.github.ajalt.colorconvert

private fun String.parseHex(range: IntRange) = trimStart('#').slice(range).toInt(16)
private fun Int.renderHex() = toString(16).padStart(2, '0')

data class RGB(val r: Int, val g: Int, val b: Int) {
    init {
        require(r in 0..255) { "r must be in range [0, 255] in $this" }
        require(g in 0..255) { "g must be in range [0, 255] in $this" }
        require(b in 0..255) { "b must be in range [0, 255] in $this" }
    }

    /**
     * Construct an RGB instance from a hex string.
     *
     * @param hex An rgb hex string in the form "#ffffff" or "ffffff"
     */
    constructor(hex: String) :
            this(hex.parseHex(0..1), hex.parseHex(2..3), hex.parseHex(4..5)) {
        require(hex[0] == '#' && hex.length == 7 || hex.length == 6) {
            "Hex string must be in the format \"#ffffff\" or \"ffffff\""
        }
    }

    /**
     * Return this value as a hex string
     * @return A string in the form `"#ffffff"` if [withNumberSign] is true,
     *     or in the form `"ffffff"` otherwise.
     */
    fun toHex(withNumberSign: Boolean = false): String = buildString(7) {
        if (withNumberSign) append('#')
        append(r.renderHex()).append(g.renderHex()).append(b.renderHex())
    }

    fun toHSL(): HSL {
        val r = this.r / 255.0
        val g = this.g / 255.0
        val b = this.b / 255.0
        val min = minOf(r, g, b)
        val max = maxOf(r, g, b)
        val delta = max - min
        var h = when {
            max == min -> 0.0
            r == max -> (g - b) / delta
            g == max -> 2 + (b - r) / delta
            b == max -> 4 + (r - g) / delta
            else -> 0.0
        }

        h = minOf(h * 60, 360.0)
        if (h < 0) h += 360
        val l = (min + max) / 2.0
        val s = when {
            max == min -> 0.0
            l <= 0.5 -> delta / (max + min)
            else -> delta / (2 - max - min)
        }

        return HSL(h.roundToInt(), (s * 100).roundToInt(), (l * 100).roundToInt())
    }

    fun toHSV(): HSV {
        val r = this.r.toDouble()
        val g = this.g.toDouble()
        val b = this.b.toDouble()
        val min = minOf(r, g, b)
        val max = maxOf(r, g, b)
        val delta = max - min

        val s = when (max) {
            0.0 -> 0.0
            else -> (delta / max * 1000) / 10
        }

        var h = when {
            max == min -> 0.0
            r == max -> (g - b) / delta
            g == max -> 2 + (b - r) / delta
            b == max -> 4 + (r - g) / delta
            else -> 0.0
        }

        h = minOf(h * 60, 360.0)

        if (h < 0) {
            h += 360
        }

        val v = ((max / 255) * 1000) / 10

        return HSV(h.roundToInt(), s.roundToInt(), v.roundToInt())
    }

    fun toAnsi16(): Ansi16 = toAnsi16(toHSV().v)

    internal fun toAnsi16(value: Int): Ansi16 {
        if (value == 30) return Ansi16(30)
        val v = Math.round(value / 50.0).toInt()

        val ansi = 30 +
                ((b / 255.0).roundToInt() * 4
                        or ((g / 255.0).roundToInt() * 2)
                        or (r / 255.0).roundToInt())
        return Ansi16(if (v == 2) ansi + 60 else ansi)
    }
}


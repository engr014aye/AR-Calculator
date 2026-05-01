package com.munawarini.arcalculator.utils

/**
 * Offline unit conversion engine supporting Length, Weight, and Currency.
 * All conversions go through a canonical "base unit" to keep conversion tables minimal.
 *
 *  Length  → base = metres
 *  Weight  → base = kilograms
 *  Currency→ base = USD (mocked exchange rates, accurate to ~2026-Q1)
 */
object UnitConverter {

    enum class Category { LENGTH, WEIGHT, CURRENCY }

    data class UnitInfo(val symbol: String, val displayName: String)

    // ── Unit definitions ─────────────────────────────────────────────────────

    val lengthUnits = listOf(
        UnitInfo("m",  "Metre"),
        UnitInfo("km", "Kilometre"),
        UnitInfo("cm", "Centimetre"),
        UnitInfo("mm", "Millimetre"),
        UnitInfo("mi", "Mile"),
        UnitInfo("yd", "Yard"),
        UnitInfo("ft", "Foot"),
        UnitInfo("in", "Inch")
    )

    val weightUnits = listOf(
        UnitInfo("kg", "Kilogram"),
        UnitInfo("g",  "Gram"),
        UnitInfo("mg", "Milligram"),
        UnitInfo("lb", "Pound"),
        UnitInfo("oz", "Ounce"),
        UnitInfo("t",  "Metric Tonne"),
        UnitInfo("st", "Stone")
    )

    val currencyUnits = listOf(
        UnitInfo("USD", "US Dollar"),
        UnitInfo("EUR", "Euro"),
        UnitInfo("GBP", "British Pound"),
        UnitInfo("PKR", "Pakistani Rupee"),
        UnitInfo("INR", "Indian Rupee"),
        UnitInfo("SAR", "Saudi Riyal"),
        UnitInfo("AED", "UAE Dirham"),
        UnitInfo("CAD", "Canadian Dollar"),
        UnitInfo("AUD", "Australian Dollar"),
        UnitInfo("JPY", "Japanese Yen"),
        UnitInfo("CNY", "Chinese Yuan")
    )

    fun unitsFor(category: Category): List<UnitInfo> = when (category) {
        Category.LENGTH   -> lengthUnits
        Category.WEIGHT   -> weightUnits
        Category.CURRENCY -> currencyUnits
    }

    // ── Conversion factors (to base unit) ────────────────────────────────────

    /** Metres per unit */
    private val lengthToMetres = mapOf(
        "m"  to 1.0,
        "km" to 1_000.0,
        "cm" to 0.01,
        "mm" to 0.001,
        "mi" to 1_609.344,
        "yd" to 0.9144,
        "ft" to 0.3048,
        "in" to 0.0254
    )

    /** Kilograms per unit */
    private val weightToKg = mapOf(
        "kg" to 1.0,
        "g"  to 0.001,
        "mg" to 0.000_001,
        "lb" to 0.453_592,
        "oz" to 0.028_349_5,
        "t"  to 1_000.0,
        "st" to 6.350_29
    )

    /** USD per unit (mocked static rates, Q1-2026 approximation) */
    private val currencyToUsd = mapOf(
        "USD" to 1.0,
        "EUR" to 1.08,
        "GBP" to 1.27,
        "PKR" to 0.003_57,    // 1 PKR ≈ 0.00357 USD
        "INR" to 0.012_0,
        "SAR" to 0.266_7,
        "AED" to 0.272_3,
        "CAD" to 0.735_0,
        "AUD" to 0.645_0,
        "JPY" to 0.006_7,
        "CNY" to 0.138_0
    )

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Convert [value] from [fromUnit] to [toUnit] within [category].
     * Returns [Double.NaN] if a unit symbol is unrecognised.
     */
    fun convert(value: Double, fromUnit: String, toUnit: String, category: Category): Double {
        if (fromUnit == toUnit) return value
        return when (category) {
            Category.LENGTH   -> convertViaBase(value, fromUnit, toUnit, lengthToMetres)
            Category.WEIGHT   -> convertViaBase(value, fromUnit, toUnit, weightToKg)
            Category.CURRENCY -> convertViaBase(value, fromUnit, toUnit, currencyToUsd)
        }
    }

    /**
     * Format a conversion result for display.
     * - Up to 8 significant figures
     * - Trailing zeros stripped
     */
    fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "—"
        if (value == kotlin.math.floor(value) && value < 1e12) {
            return value.toLong().toString()
        }
        return "%.8g".format(value).trimEnd('0').trimEnd('.')
    }

    // ── Internal helper ──────────────────────────────────────────────────────

    private fun convertViaBase(
        value: Double,
        from: String,
        to: String,
        table: Map<String, Double>
    ): Double {
        val fromFactor = table[from] ?: return Double.NaN
        val toFactor   = table[to]   ?: return Double.NaN
        return value * fromFactor / toFactor
    }
}

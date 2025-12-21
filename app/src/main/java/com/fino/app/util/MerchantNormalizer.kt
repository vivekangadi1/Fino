package com.fino.app.util

/**
 * Utility for normalizing merchant names from SMS transactions.
 * Handles UPI suffixes, special characters, and common variations.
 */
object MerchantNormalizer {

    /**
     * Normalize merchant name for matching:
     * - Convert to uppercase
     * - Remove UPI suffixes (@paytm, @ybl, @okaxis, etc.)
     * - Remove special characters and numbers
     * - Collapse multiple spaces
     * - Trim whitespace
     *
     * Examples:
     * - "swiggy@paytm" → "SWIGGY"
     * - "HDFC*BANK#001" → "HDFC BANK"
     * - "uber  india" → "UBER INDIA"
     */
    fun normalize(merchantName: String): String {
        return merchantName
            .uppercase()
            .replace(Regex("@[A-Z0-9]+"), "")      // Remove UPI suffixes (@paytm, @ybl, @okaxis)
            .replace(Regex("[^A-Z\\s]"), "")       // Keep only letters and spaces
            .replace(Regex("\\s+"), " ")           // Collapse multiple spaces to single space
            .trim()
    }

    /**
     * Extract base merchant name by removing common prefixes and suffixes.
     * Useful for matching merchants across different bank formats.
     *
     * Examples:
     * - "HDFC BANK SWIGGY" → "SWIGGY"
     * - "AMAZON INDIA PVT LTD" → "AMAZON"
     * - "SBI ZOMATO" → "ZOMATO"
     */
    fun extractBaseName(merchantName: String): String {
        val normalized = normalize(merchantName)

        // Remove common bank prefixes
        val withoutBanks = normalized
            .removePrefix("HDFC BANK ")
            .removePrefix("HDFC ")
            .removePrefix("SBI ")
            .removePrefix("ICICI ")
            .removePrefix("AXIS ")
            .removePrefix("AXIS BANK ")
            .removePrefix("KOTAK ")
            .removePrefix("KOTAK BANK ")

        // Remove common business suffixes
        val cleanedName = withoutBanks
            .removeSuffix(" LTD")
            .removeSuffix(" PVT LTD")
            .removeSuffix(" PRIVATE LIMITED")
            .removeSuffix(" LIMITED")
            .removeSuffix(" INDIA")
            .removeSuffix(" INC")
            .removeSuffix(" CORP")

        return cleanedName.trim()
    }

    /**
     * Check if two merchant names likely refer to the same merchant.
     * Useful for quick pre-filtering before fuzzy matching.
     *
     * Returns true if normalized names are identical or one contains the other.
     */
    fun areLikelySame(merchant1: String, merchant2: String): Boolean {
        val norm1 = normalize(merchant1)
        val norm2 = normalize(merchant2)

        if (norm1 == norm2) return true
        if (norm1.contains(norm2) || norm2.contains(norm1)) return true

        return false
    }
}

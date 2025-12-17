package com.fino.app.ml.matcher

/**
 * Fuzzy string matching using Levenshtein distance.
 * Used to match similar merchant names.
 */
class FuzzyMatcher {

    /**
     * Calculate similarity between two strings.
     * Returns a value between 0.0 (completely different) and 1.0 (identical).
     */
    fun calculateSimilarity(a: String, b: String): Float {
        val s1 = normalize(a)
        val s2 = normalize(b)

        if (s1 == s2) return 1.0f
        if (s1.isEmpty() || s2.isEmpty()) return 0.0f

        val distance = levenshteinDistance(s1, s2)
        val maxLen = maxOf(s1.length, s2.length)

        return 1.0f - (distance.toFloat() / maxLen)
    }

    /**
     * Check if two strings are similar enough to suggest a match.
     * Default threshold is 0.7 (70% similar).
     */
    fun isSimilar(a: String, b: String, threshold: Float = 0.7f): Boolean {
        return calculateSimilarity(a, b) >= threshold
    }

    /**
     * Find the best match from a list of candidates.
     * Returns null if no match meets the threshold.
     */
    fun findBestMatch(query: String, candidates: List<String>, threshold: Float = 0.7f): MatchResult? {
        var bestMatch: String? = null
        var bestScore = 0.0f

        for (candidate in candidates) {
            val score = calculateSimilarity(query, candidate)
            if (score > bestScore && score >= threshold) {
                bestScore = score
                bestMatch = candidate
            }
        }

        return bestMatch?.let { MatchResult(it, bestScore) }
    }

    /**
     * Normalize a string for comparison.
     * Converts to uppercase, trims, and collapses multiple spaces.
     */
    private fun normalize(s: String): String {
        return s.uppercase()
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Calculate Levenshtein distance between two strings.
     * This is the minimum number of single-character edits needed
     * to transform one string into the other.
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        // Initialize base cases
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        // Fill the DP table
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }

        return dp[s1.length][s2.length]
    }

    data class MatchResult(
        val match: String,
        val score: Float
    )
}

package com.fino.app.service.categorization

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keyword-based category matching for transactions.
 * Provides fallback categorization when exact merchant matches aren't available.
 */
@Singleton
class KeywordMatcher @Inject constructor() {

    private val keywordRules = listOf(
        // Transport keywords - Category 2
        KeywordRule(
            categoryId = 2L,
            confidence = 0.90f,
            keywords = listOf("uber", "ola", "rapido", "taxi", "cab", "metro", "bus", "train", "flight", "parking", "toll", "petrol", "diesel", "fuel", "gas station")
        ),

        // Food keywords - Category 1
        KeywordRule(
            categoryId = 1L,
            confidence = 0.88f,
            keywords = listOf("swiggy", "zomato", "food", "restaurant", "cafe", "pizza", "burger", "kfc", "mcdonald", "domino", "starbucks", "coffee", "dinner", "lunch", "breakfast")
        ),

        // Shopping keywords - Category 3
        KeywordRule(
            categoryId = 3L,
            confidence = 0.85f,
            keywords = listOf("amazon", "flipkart", "shop", "store", "mall", "retail", "myntra", "ajio", "fashion", "clothing", "apparel", "purchase")
        ),

        // Bills keywords - Category 6
        KeywordRule(
            categoryId = 6L,
            confidence = 0.92f,
            keywords = listOf("electricity", "water", "gas bill", "broadband", "internet", "wifi", "jio", "airtel", "vi", "vodafone", "bsnl", "mobile", "recharge", "dth", "tata sky", "dish")
        ),

        // Entertainment keywords - Category 5
        KeywordRule(
            categoryId = 5L,
            confidence = 0.87f,
            keywords = listOf("netflix", "prime", "hotstar", "spotify", "youtube", "movie", "cinema", "pvr", "inox", "bookmyshow", "subscription", "stream", "music")
        ),

        // Groceries keywords - Category 9
        KeywordRule(
            categoryId = 9L,
            confidence = 0.86f,
            keywords = listOf("bigbasket", "grofer", "blinkit", "zepto", "dunzo", "grocery", "vegetables", "fruits", "supermarket", "dmart", "reliance fresh")
        ),

        // Health keywords - Category 4
        KeywordRule(
            categoryId = 4L,
            confidence = 0.89f,
            keywords = listOf("hospital", "clinic", "doctor", "medical", "pharmacy", "medicine", "apollo", "netmeds", "1mg", "pharmeasy", "health")
        ),

        // Education keywords - Category 7
        KeywordRule(
            categoryId = 7L,
            confidence = 0.88f,
            keywords = listOf("school", "college", "university", "course", "tuition", "fees", "education", "byju", "unacademy", "book", "exam")
        ),

        // Travel keywords - Category 8
        KeywordRule(
            categoryId = 8L,
            confidence = 0.87f,
            keywords = listOf("hotel", "resort", "travel", "trip", "tour", "makemytrip", "goibibo", "cleartrip", "indigo", "spicejet", "vistara", "airindia", "oyo")
        ),

        // Insurance keywords - Category 13
        KeywordRule(
            categoryId = 13L,
            confidence = 0.90f,
            keywords = listOf("insurance", "policy", "premium", "lic", "hdfc ergo", "icici lombard", "health insurance", "life insurance")
        ),

        // Investments keywords - Category 14
        KeywordRule(
            categoryId = 14L,
            confidence = 0.91f,
            keywords = listOf("mutual fund", "sip", "stock", "equity", "zerodha", "groww", "upstox", "investment", "trading", "demat")
        )
    )

    /**
     * Match keywords in merchant name and SMS body to find category.
     * Returns null if no keywords match.
     */
    fun matchKeywords(merchantName: String, smsBody: String): KeywordMatch? {
        val searchText = "$merchantName $smsBody".lowercase()

        for (rule in keywordRules) {
            for (keyword in rule.keywords) {
                if (searchText.contains(keyword)) {
                    return KeywordMatch(
                        categoryId = rule.categoryId,
                        confidence = rule.confidence,
                        matchedKeyword = keyword
                    )
                }
            }
        }

        return null
    }

    /**
     * Get all keyword rules for a specific category.
     * Useful for debugging and understanding categorization.
     */
    fun getKeywordsForCategory(categoryId: Long): List<String> {
        return keywordRules
            .filter { it.categoryId == categoryId }
            .flatMap { it.keywords }
    }
}

/**
 * Internal data class representing a keyword rule.
 */
private data class KeywordRule(
    val categoryId: Long,
    val confidence: Float,
    val keywords: List<String>
)

/**
 * Result of keyword matching.
 */
data class KeywordMatch(
    val categoryId: Long,
    val confidence: Float,
    val matchedKeyword: String
)

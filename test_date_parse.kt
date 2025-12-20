import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun parseDate(dateStr: String): LocalDateTime {
    val patterns = listOf(
        "dd-MM-yy",
        "dd-MM-yyyy",
        "dd/MM/yyyy",
        "dd/MM/yy",
        "dd-MMM-yy",
        "dd-MMM-yyyy"
    )

    for (pattern in patterns) {
        try {
            val formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
            val date = LocalDate.parse(dateStr.trim(), formatter)
            return date.atStartOfDay()
        } catch (e: Exception) {
            // Try next pattern
        }
    }

    // Default to now if parsing fails
    return LocalDateTime.now()
}

fun main() {
    val testDates = listOf(
        "20-12-24",  // Dec 20, 2024
        "14-12-24",  // Dec 14, 2024
        "20-Dec-24", // Dec 20, 2024
        "14-Dec-24"  // Dec 14, 2024
    )
    
    println("Testing date parsing with pattern 'dd-MM-yy':")
    for (date in testDates) {
        val parsed = parseDate(date)
        println("Input: '$date' -> ${parsed.toLocalDate()} (Year: ${parsed.year}, Month: ${parsed.monthValue}, Day: ${parsed.dayOfMonth})")
    }
}

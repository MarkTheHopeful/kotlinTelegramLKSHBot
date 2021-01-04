package table

import org.apache.commons.text.StringEscapeUtils.unescapeHtml4

fun joinStringArray(input: List<String>, separator: String = "\n"): String {
    var result = ""
    for (i in input.indices) {
        if (i != 0) {
            result += separator
        }
        result += input[i]
    }
    return result
}

fun joinProblemArray(input: List<Problem>, separator: String = "\n"): String {
    val newInput = input.map { it.toString() }
    return joinStringArray(newInput, separator)
}

fun normalizeOutputText(withEscaped: String): String {
    return unescapeHtml4(withEscaped)
}
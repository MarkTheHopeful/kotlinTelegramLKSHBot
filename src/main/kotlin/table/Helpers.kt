package table

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
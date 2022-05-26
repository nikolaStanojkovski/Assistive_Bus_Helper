package mk.ukim.finki.assistivebushelper.util

class ValidationUtils {

    companion object {
        fun isNumeric(toCheck: String): Boolean {
            return toCheck.all { char -> char.isDigit() }
        }
    }
}
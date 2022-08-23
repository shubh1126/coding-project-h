package exceptions

import java.io.PrintWriter
import java.io.StringWriter

internal class ServiceError @JvmOverloads constructor(
    var message: String,
    cause: Throwable = Exception("Service Error")
) {
    private val causeMessage: String? = cause.message
    private val causeStackTrace: String

    init {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        cause.printStackTrace(pw)
        causeStackTrace = sw.toString()
    }
}

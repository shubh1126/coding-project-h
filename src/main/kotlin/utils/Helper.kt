package utils

import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

/**
 * Common helper functions
 */
object Helper {
    fun generateUuid(prefix: String): String = prefix + "-" + UUID.randomUUID().toString()
    fun currentDateTime(): Long = ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli()
}
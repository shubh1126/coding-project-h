package utils

import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

/**
 * Common helper functions
 */
object Helper {
    fun generateUuid(prefix: String): String = prefix + "-" + UUID.randomUUID().toString()
    fun currentDateTime(): Long = ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli()
    fun getCurrentDateTimeBeforeNMinutes(minutes : Long): Long = ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(minutes).toInstant().toEpochMilli()
    fun getCurrentDateTimeAfterNMinutes(minutes : Long): Long = ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(minutes).toInstant().toEpochMilli()
    fun epochToZoneDateTime(epoch: Long): ZonedDateTime =
        ZonedDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC)
}
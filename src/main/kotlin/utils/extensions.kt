package utils

import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

fun Long.dayOfWeek(): Int = ZonedDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneOffset.UTC).dayOfWeek.value
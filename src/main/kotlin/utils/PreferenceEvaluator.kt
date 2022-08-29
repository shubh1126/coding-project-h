package utils

import dtos.*
import dtos.internal.*
import io.dropwizard.jersey.params.DurationParam
import models.*
import java.lang.IllegalArgumentException
import java.time.ZonedDateTime

object PreferenceEvaluator {

    fun matches(
        preferences: List<Preference>,
        target: PreferenceCheckDTO
    ) {
        val (allow, deny) = preferences.partition { it.cardinality == PreferenceCardinality.ALLOW }

        //only one active at the moment
        val availabilityPref = allow.filterIsInstance<AvailabilityPreference>()
            .firstOrNull()
        val durationPref = allow.filterIsInstance<SlotDurationPreference>()
            .firstOrNull()
        require(availabilityPref != null) {
            "Slot cannot be booked. Availability not set by the user"
        }
        // not taking default
        require(durationPref != null) {
            "Slot cannot be booked.Slot duration not set by the user"
        }

        allow.map { preference ->
            when (preference) {
                is AvailabilityPreference -> preference.matches(target.toAvailabilityPreferenceValue())
                is SlotDurationPreference -> preference.matches(target.toSlotDurationPreferenceValue())
                is SlotAdvancePreference -> preference.matches(target.toSlotAdvancePreferenceValue())
                else -> false
            }.let {
                if (!it)
                    throw IllegalArgumentException("Slot cannot be booked. Reason: ${preference.getDisplayMessage()}")
            }
        }

        availabilityPref.checkForSequence(target, durationPref)

        deny.map { preference ->
            when (preference) {
                is HolidayPreference -> preference.matches(target.toUnavailabilityPreferenceValue())
                else -> true
            }.let {
                if (it)
                    throw IllegalArgumentException("Slot cannot be booked. Reason: ${preference.getDisplayMessage()}")

            }
        }
    }

    private fun AvailabilityPreference.checkForSequence(
        target: PreferenceCheckDTO,
        durationPref: SlotDurationPreference
    ) {
        this.availabilityDays.firstOrNull {
            it.dayIndex == Helper.epochToZoneDateTime(target.startAt).dayOfWeek.value
        }?.let { pref ->
            val targetDate = Helper.epochToZoneDateTime(target.startAt)
                .withHour(pref.startTime.getReverseAvFormat().getHour())
                .withMinute(pref.startTime.getReverseAvFormat().getMin())
                .toInstant().toEpochMilli()


            require((target.startAt - targetDate) % durationPref.durationInMilliseconds == 0L) {
                "Slots can only be booked in multiple of duration"
            }
        }
    }
}

fun Preference.getDisplayMessage(): String =
    when (this) {
        is AvailabilityPreference -> "Slot is not available for booking"
        is SlotDurationPreference -> "Duration doesnt matches the host specified duration"
        is SlotAdvancePreference -> "Slot booking time has passed"
        is HolidayPreference -> "Host is on holiday for specified time"
        else -> "Slot preference doesn't matches"
    }


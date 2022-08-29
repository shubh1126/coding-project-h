package dtos

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import models.*
import utils.Helper
import utils.milliSecondsToMinutes

@JsonIgnoreProperties(ignoreUnknown = true)
data class PreferenceDto(
    val slotDurationInMilliSeconds: Long? = null,
    val slotAdvanceDurationInMilliSeconds: Long? = null,
    // allowing null so that its optional
    // empty can be considered as not available
    val availabilitySchedule: Set<AvailabilityItemDTO>? = null
) {

    init {
        require(
            listOfNotNull(
                slotDurationInMilliSeconds,
                slotAdvanceDurationInMilliSeconds,
                availabilitySchedule
            ).isNotEmpty()
        ) {
            "At-least one preference needs to be defined"
        }

        slotAdvanceDurationInMilliSeconds?.also {
            require(it.milliSecondsToMinutes() >=1){
                "Slot advance duration should be a multiple of 1 minute"
            }
        }

        slotDurationInMilliSeconds?.also {
            val duration = it.milliSecondsToMinutes()
            require(duration % 15 == 0L) {
                "Duration should be in multiple of 15 minutes"
            }

            require(duration <= 120) {
                "Duration of a slot cannot be greater than 2 hours"
            }

            // Note: can add check to ensure schedule has a slot duration

        }
    }

    fun getAvailabilityPreference(user: User): AvailabilityPreference? =
        availabilitySchedule?.let {
            AvailabilityPreference(
                availabilityDays = it.map { av -> av.toAvailabilityItem() }.toSet(),
                starAt = Helper.currentDateTime(),
                user = user
            )
        }


    fun getSlotDurationPreference(user: User): SlotDurationPreference? =
        slotDurationInMilliSeconds?.let {
            SlotDurationPreference(
                durationInMilliseconds = it,
                starAt = Helper.currentDateTime(),
                user = user
            )
        }


    @JsonIgnore
    fun getSlotAdvancePreference(user: User): SlotAdvancePreference? =
        slotAdvanceDurationInMilliSeconds?.let {
            SlotAdvancePreference(
                durationInMinutes = it.milliSecondsToMinutes(),
                starAt = Helper.currentDateTime(),
                user = user
            )
        }

    fun getPreferences(user: User): List<Preference> =
        listOfNotNull(
            getSlotAdvancePreference(user),
            getSlotDurationPreference(user),
            getAvailabilityPreference(user)
        )
}


fun String.getHour(): Int = this.substring(0, 2).toInt()
fun String.getMin(): Int = this.substring(3, 5).toInt()

fun String.getAVFormat(): Long = "${this.getHour()}${this.getMin()}".toLong()

fun Long.getReverseAvFormat(): String = this.toString().let {
    if (it.length  == 4) {
        "${it.getHour()}:${it.substring(2, 4).toInt()}"
    } else {
        "0${it.substring(0, 1).toInt()}:${it.substring(1, 3).toInt()}"
    }
}
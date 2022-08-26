package dtos

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import models.*
import utils.Helper
import utils.milliSecondsToMinutes

@JsonIgnoreProperties(ignoreUnknown = true)
data class PreferenceDto(
    val slotDuration: Long? = null,
    val slotAdvanceDuration: Long? = null,
    // allowing null so that its optional
    // empty can be considered as not available
    val availabilitySchedule: Set<AvailabilityItem>? = null
) {

    init {
        require(listOfNotNull(slotDuration, slotAdvanceDuration, availabilitySchedule).isNotEmpty()) {
            "Atleast one preference needs to be define"
        }
    }

    fun getAvailabilityPreference(user: User): AvailabilityPreference? =
        availabilitySchedule?.let {
            AvailabilityPreference(
                availabilityDays = it,
                starAt = Helper.currentDateTime(),
                user = user
            )
        }


    fun getSlotDurationPreference(user: User): SlotDurationPreference? =
        slotDuration?.let {
            SlotDurationPreference(
                durationInMilliseconds = it,
                starAt = Helper.currentDateTime(),
                user = user
            )
        }


    @JsonIgnore
    fun getSlotAdvancePreference(user: User): SlotAdvancePreference? =
        slotAdvanceDuration?.let {
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
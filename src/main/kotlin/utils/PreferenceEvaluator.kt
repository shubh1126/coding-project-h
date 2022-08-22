package utils

import dtos.PreferenceCheckDTO
import dtos.toAvailabilityPreferenceValue
import dtos.toSlotDurationPreferenceValue
import dtos.toUnavailabilityPreferenceValue
import models.AvailabilityPreference
import models.HolidayPreference
import models.Preference
import models.PreferenceCardinality
import models.SlotDurationPreference

object PreferenceEvaluator {

    fun matches(
        preferences : List<Preference>,
        target: PreferenceCheckDTO
    ) : Boolean {
       val (allow, deny) = preferences.partition{ it.cardinality == PreferenceCardinality.ALLOW }

        val allowValue = allow.map { preference ->
            when (preference) {
                is AvailabilityPreference -> preference.matches(target.toAvailabilityPreferenceValue())
                is SlotDurationPreference -> preference.matches(target.toSlotDurationPreferenceValue())
                else -> false
            }
        }.all { it }

        val denyValue = deny.map { preference ->
            when(preference){
                is HolidayPreference -> preference.matches(target.toUnavailabilityPreferenceValue())
                else -> true
            }
        }.all {!it}

       return allowValue && denyValue
    }
}
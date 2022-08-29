package `data-stores`

import models.*
import utils.Helper
import kotlin.reflect.KClass
import kotlin.reflect.jvm.internal.impl.load.java.structure.JavaClass

class PreferenceStore {

    private val preferences: MutableMap<String, MutableList<Preference>> = mutableMapOf()


    @Synchronized
    fun addPreference(preference: Preference): Preference =
        when (preference) {
            is UserPreference -> addPreference(preference)
            else -> throw NotImplementedError("Not yet implemented")
        }

    fun getPreferences(userId: String): List<Preference> = preferences
        .getOrDefault(userId, mutableListOf()).toList()


    @Synchronized
    fun updateUserPreference(oldPreference: UserPreference, newPreference: UserPreference) {
        preferences.computeIfAbsent(oldPreference.user.id)
        { mutableListOf<Preference>() }.also {
            it.remove(oldPreference)
            it.add(newPreference)
        }

    }

    fun < T : UserPreference> getUserPreferences(
        userId: String,
        t : JavaClass,
    ): List<Preference> =
        getPreferences(userId).filter{ it.javaClass == t}


    private fun addPreference(preference: UserPreference): Preference {
        val userPreferences = preferences.computeIfAbsent(
            preference.user.id
        ) {
            mutableListOf()
        }

        // this can be moved away when we connect db
        // as json sub type will add the type info which can be used to query upon
        val preference = when (preference) {
            is SlotDurationPreference ->
                addSlotDurationPreference(
                    userPreferences
                        .filterIsInstance<SlotDurationPreference>()
                        .filter { it.isActive() }, preference
                )
            is AvailabilityPreference ->
                addAvailabilityPreference(
                    userPreferences
                        .filterIsInstance<AvailabilityPreference>()
                        .filter { it.isActive() }, preference
                )
            is HolidayPreference ->
                addHolidayPreference(
                    userPreferences
                        .filterIsInstance<HolidayPreference>(), preference
                )
            is SlotAdvancePreference ->
                addSlotAdvancePreference(
                    userPreferences
                        .filterIsInstance<SlotAdvancePreference>()
                        .filter { it.isActive() }, preference
                )
        }

        return preference.also { userPreferences.add(it) }

    }

    //Note not allowing multiple scheduled preferences here for now,
    // can be added in future
    private fun addSlotDurationPreference(
        preferences: List<SlotDurationPreference>,
        preference: SlotDurationPreference
    ): Preference =
        preference.also {
            preferences.firstOrNull()?.also { old ->
                old.copy(
                    status = PreferenceStatus.INACTIVE,
                    expireAt = Helper.currentDateTime()
                ).also { new ->
                    updateUserPreference(old, new)
                }
            }
        }

    //Note not allowing multiple scheduled preferences here for now,
    // can be added in future
    private fun addAvailabilityPreference(
        preferences: List<AvailabilityPreference>,
        preference: AvailabilityPreference
    ): Preference = preference.also {
        preferences.firstOrNull()?.also { old ->
            old.copy(
                status = PreferenceStatus.INACTIVE,
                expireAt = Helper.currentDateTime()
            ).also { new ->
                updateUserPreference(old, new)
            }
        }
    }

    private fun addHolidayPreference(
        preferences: List<HolidayPreference>,
        preference: HolidayPreference
    ): Preference =
        preference.also {
            require(preferences.none { existingPreference ->
                when {
                    existingPreference.starAt > preference.starAt ->
                        preference.expireAt <= existingPreference.expireAt

                    existingPreference.starAt < preference.starAt ->
                        existingPreference.expireAt <= preference.starAt
                    else -> false
                }
            }) {
                "Preference cannot be added"
            }

            // allowing 1 minute  system buffer owing to latencies and all
            require(preference.starAt > Helper.getCurrentDateTimeBeforeNMinutes(1L)) {
                "Holiday can't be set for past date"
            }
        }

    //Note not allowing multiple scheduled preferences here for now,
    // can be added in future
    private fun addSlotAdvancePreference(
        preferences: List<SlotAdvancePreference>,
        preference: SlotAdvancePreference
    ): Preference = preference.also {
        preferences.firstOrNull()?.also { old ->
            old.copy(
                status = PreferenceStatus.INACTIVE,
                expireAt = Helper.currentDateTime()
            ).also { new ->
                updateUserPreference(old, new)
            }
        }
    }
}
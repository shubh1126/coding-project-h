package managers

import `data-stores`.PreferenceStore
import dtos.AvailabilityItemDTO
import dtos.PreferenceDto
import dtos.PreferenceResponseDto
import dtos.UpdateUserDto
import models.*
import utils.Helper
import utils.logger
import utils.minutesToMilliSeconds
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import kotlin.reflect.full.createInstance

/**
 * User Manager
 *
 * Not supporting delete user as it will
 * involve deletion of all relevant slots and preferences
 */
class PreferenceManager @Inject constructor(
    @Named("prefDB") private val prefDB: PreferenceStore,
    private val userManager: UserManager
) {
    private
    companion object {
        private val logger by logger()
    }

    suspend fun addUserPreferences(
        userId: String,
        preferenceDto: PreferenceDto
    ): PreferenceDto {
        val user = userManager.getUserById(userId)
        preferenceDto
            .getPreferences(user)
            .map {
                prefDB.addPreference(it)
            }

        return preferenceDto
    }

    suspend fun getuserSlotDurationTime(userId: String): Long? =
        prefDB
            .getPreferences(userId)
            .filterIsInstance<SlotDurationPreference>()
            .firstOrNull()?.durationInMilliseconds


    suspend fun getUserPreferencesRaw(
        userId: String
    ): List<Preference> =
        prefDB.getPreferences(userId)
            .filter { it.isActive() }

    suspend fun getUserPreference(
        userId: String
    ): PreferenceResponseDto {

        val preferences = prefDB
            .getPreferences(userId)

        val availabilitySchedule = preferences
            .filterIsInstance<AvailabilityPreference>()
            .let { prefs ->
                prefs.map {
                    it.availabilityDays
                        .map { av ->
                            AvailabilityItemDTO.fromAvailabilityItem(av)
                        }
                }
            }.flatten().toSet()

        val slotDurationInMilliSeconds = preferences
            .filterIsInstance<SlotDurationPreference>()
            .firstOrNull { it.isActive() }?.durationInMilliseconds
        val slotAdvanceDurationInMillis = preferences
            .filterIsInstance<SlotAdvancePreference>()
            .firstOrNull { it.isActive() }?.let { it -> it.durationInMinutes.minutesToMilliSeconds() }


        return PreferenceResponseDto(
            availabilitySchedule = availabilitySchedule,
            slotDurationInMillis = slotDurationInMilliSeconds,
            slotAdvanceDurationInMillis = slotAdvanceDurationInMillis,
            updatedAt = preferences.maxByOrNull { it.starAt }?.starAt
        )
    }
}

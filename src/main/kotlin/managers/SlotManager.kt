package managers

import `data-stores`.SlotStore
import dtos.SlotDto
import dtos.internal.PreferenceCheckDTO
import utils.PreferenceEvaluator
import utils.logger
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.inject.Named

class SlotManager @Inject constructor(
    @Named("prefDB") private val slotDB: SlotStore,
    private val userManager: UserManager,
    private val prefManager: PreferenceManager
) {
    private
    companion object {
        private val logger by logger()
    }

    suspend fun bookSlot(
        userId: String,
        slotDto: SlotDto
    ) {
        val user = userManager.getUserById(userId)

        PreferenceEvaluator.matches(
            preferences = prefManager.getUserPreferencesRaw(userId),
            target = PreferenceCheckDTO(
                startAt = slotDto.startTime,
                endAt = slotDto.endTime
            )
        )

        slotDB.addSlot(slotDto, user)
        //slotDto.checkSlotDurationPreference(userId)

    }


    private suspend fun SlotDto.checkSlotDurationPreference(userId: String) {
        prefManager
            .getuserSlotDurationTime(userId)?.also { duration ->
                require(this.isValidDuration(duration)) {
                    "Not a valid duration for booking"
                }
            } ?: IllegalArgumentException("Unable to book as user has not configured slots")

    }
}
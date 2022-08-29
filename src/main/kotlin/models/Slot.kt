package models

import com.fasterxml.jackson.annotation.JsonIgnore
import utils.Helper
import kotlin.math.abs

enum class SlotStatus {
    ACTIVE,
    INACTIVE
}

/**
 * Mqybe to allow webinar slots
 */
enum class SlotScope {
    PRIVATE,
    PUBLIC
}

data class Slot(
    val id : String = Helper.generateUuid("SLT"),
    val startTime: Long,
    val endTime: Long,
    val host: User,
    // guests need not be registered in system
    val guests: Set<String>,
    val status: SlotStatus = SlotStatus.ACTIVE,
    val scope: SlotScope = SlotScope.PRIVATE,
    val createdAt: Long = Helper.currentDateTime()
) {
    val updatedAt: Long = Helper.currentDateTime()

    @JsonIgnore
    val duration: Long = endTime - startTime

    // can add other logic later, like expiry and all
    fun isActive(): Boolean = status == SlotStatus.ACTIVE

    init {
        require(guests.isNotEmpty()) {
            "A slot needs to have at-least one guest"
        }

        guests.map { guest ->
            require(guest.filter { it == '@' }.count() == 1) {
                "Invalid guest"
            }
            require(guest != host.email) {
                "Host cannot be a guest"
            }
        }
    }


    /**
     * check if two slots are conflicting
     */
    fun isSlotConflicting(slotToBook: Slot): Boolean =
        when {
            this.startTime > slotToBook.startTime ->
                abs(slotToBook.startTime - this.startTime) > slotToBook.duration
            this.startTime < slotToBook.startTime ->
                abs(slotToBook.startTime - this.startTime) > this.duration
            else -> true
        }

}
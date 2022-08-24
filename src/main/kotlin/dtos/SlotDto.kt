package dtos

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import models.Slot
import models.User
import utils.Helper

@JsonIgnoreProperties(ignoreUnknown = true)
data class SlotDto(
    val startTime: Long,
    val endTime: Long,
    val guests: Set<String>,
) {

    init {

        // Note: Allowing 1 minute system delay
        require(startTime >= Helper.getCurrentDateTimeBeforeNMinutes(1L)) {
            "Start time needs to be booked in future"
        }
        require(endTime >= Helper.currentDateTime()) {
            "End time needs to be in future"
        }
        guests.map { guest ->
            require(guest.filter { it == '@' }.count() == 1) {
                "Invalid guest"
            }
        }
    }

    fun toSlot(hostUser: User): Slot =
        Slot(
            startTime = startTime,
            endTime = endTime,
            host = hostUser,
            guests = guests
        )

}
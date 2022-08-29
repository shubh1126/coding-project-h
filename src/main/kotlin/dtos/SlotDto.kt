package dtos

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import models.Slot
import models.User
import utils.Helper
import java.util.concurrent.TimeUnit

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
        //assuming only utc booking
        require(endTime >= Helper.currentDateTime()) {
            "End time needs to be in future"
        }
        require(endTime.minus(startTime) <= TimeUnit.HOURS.toMillis(24)){
            "Slot cannot be greater than a day"
        }

        require(
            Helper.epochToZoneDateTime(startTime).dayOfWeek ==
                    Helper.epochToZoneDateTime(endTime).dayOfWeek){
            "Slot should be mapped for the same day"
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

    fun isValidDuration(duration: Long) : Boolean =
        endTime.minus(startTime) == duration

    companion object{
        fun fromSlot(slot: Slot): SlotDto = SlotDto(
            startTime = slot.startTime,
            endTime = slot.endTime,
            guests = slot.guests
        )
    }
}
package dtos

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import models.AvailabilityItem
import java.time.DateTimeException
import java.time.ZonedDateTime


@JsonIgnoreProperties(ignoreUnknown = true)
data class AvailabilityItemDTO(
    //format hh:mm
    val dayIndex: Int = 0,
    val startTime: String,
    val endTime: String
) {

    init {
        try {

            require(
                ZonedDateTime.now()
                    .withHour(startTime.getHour())
                    .withMinute(startTime.getMin()).isBefore(
                        ZonedDateTime.now()
                            .withHour(endTime.getHour())
                            .withMinute(endTime.getMin())
                    )
            ) {
                "End time should be greater than start time"
            }
        } catch (e: DateTimeException) {
            throw IllegalArgumentException("Please provide valid start or end time")
        }

        require(dayIndex in 1..7) {
            "Please provide a valid day"
        }

    }

    fun toAvailabilityItem(): AvailabilityItem =
        AvailabilityItem(
            dayIndex = dayIndex,
            startTime = startTime.getAVFormat(),
            endTime = endTime.getAVFormat()
        )


    companion object {
        fun fromAvailabilityItem(availabilityItem: AvailabilityItem) =
            AvailabilityItemDTO(
                dayIndex = availabilityItem.dayIndex,
                startTime = availabilityItem.startTime.getReverseAvFormat(),
                endTime = availabilityItem.endTime.getReverseAvFormat()
            )
    }
}
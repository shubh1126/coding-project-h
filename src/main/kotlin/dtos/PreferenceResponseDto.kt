package dtos

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import models.AvailabilityItem

@JsonIgnoreProperties(ignoreUnknown = true)
data class PreferenceResponseDto(
    val slotDurationInMillis: Long? = null,
    val slotAdvanceDurationInMillis: Long? = null,
    // allowing null so that its optional
    // empty can be considered as not available
    val availabilitySchedule: Set<AvailabilityItemDTO> = emptySet(),
    val updatedAt: Long?= null
)
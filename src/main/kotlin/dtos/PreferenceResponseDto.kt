package dtos

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import models.AvailabilityItem

@JsonIgnoreProperties(ignoreUnknown = true)
data class PreferenceResponseDto(
    val slotDurationInMillis: Long,
    val slotAdvanceDurationInMillis: Long,
    // allowing null so that its optional
    // empty can be considered as not available
    val availabilitySchedule: Set<AvailabilityItem>,
    val createdAt: Long,
    val updatedAt: Long

)
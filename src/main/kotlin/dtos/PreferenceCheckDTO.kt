package dtos

import models.AvailabilityItem
import models.PreferenceCandidateValue
import models.SingleCandidateItem
import models.UnavailabilityItem
import utils.dayOfWeek

data class PreferenceCheckDTO(
    val startAt: Long,
    val endAt: Long
)

fun PreferenceCheckDTO.toSlotDurationPreferenceValue() : PreferenceCandidateValue =
    SingleCandidateItem(endAt - startAt)

fun PreferenceCheckDTO.toAvailabilityPreferenceValue() : PreferenceCandidateValue =
    SingleCandidateItem(AvailabilityItem(endAt.dayOfWeek(), startAt, endAt))

fun PreferenceCheckDTO.toUnavailabilityPreferenceValue() : PreferenceCandidateValue =
    SingleCandidateItem(UnavailabilityItem(startAt, endAt))

fun PreferenceCheckDTO.toSlotAdvancePreferenceValue() : PreferenceCandidateValue =
    SingleCandidateItem(startAt)


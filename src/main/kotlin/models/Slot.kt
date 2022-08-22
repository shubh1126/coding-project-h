package models

import utils.Helper

enum class SlotStatus{
    ACTIVE,
    INACTIVE
}

/**
 * Mqybe to allow webinar slots
 */
enum class SlotScope{
    PRIVATE,
    PUBLIC
}

data class Slot(
    val startTime: Long,
    val endTime: Long,
    val host: User,
    val status : SlotStatus = SlotStatus.ACTIVE,
    val scope: SlotScope = SlotScope.PRIVATE,
    val createdAt: Long = Helper.currentDateTime()
) {
    val updatedAt: Long = Helper.currentDateTime()
}
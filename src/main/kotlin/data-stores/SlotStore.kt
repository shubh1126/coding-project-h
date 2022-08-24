package `data-stores`

import dtos.SlotDto
import models.Slot
import models.User

/**
 * Replacement of db, data store for slots
 */
class SlotStore {

    // map of user id and slot
    // basically using user id as partition key
    private val slots: MutableMap<String, MutableList<Slot>> = mutableMapOf()

    @Synchronized
    fun addSlot(slotDto: SlotDto, hostUser: User) {

        val userSlots = slots.computeIfAbsent(hostUser.id) {
            mutableListOf()
        }
        slotDto.toSlot(hostUser).also { slotToBook ->
            require(
                userSlots.filter { it.isActive() }.none {
                    it.isSlotConflicting(slotToBook)
                }
            ) {
                "Slot already booked for the given time"
            }
        }.also { slot ->
            slots.computeIfAbsent(hostUser.id) {
                mutableListOf()
            }.add(slot)
        }
    }

    /**
     * get slot of users
     */
    fun getSlotForUser(user: User, startTime: Long, endTime: Long): List<Slot> =
        slots
            .getOrDefault(user.id, emptyList<Slot>())
            .filter { slot ->
                slot.startTime >= startTime && slot.endTime <= slot.endTime
            }
}
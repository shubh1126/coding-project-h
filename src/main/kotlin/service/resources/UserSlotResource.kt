package service.resources

import dtos.SlotDto
import managers.SlotManager
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.container.AsyncResponse
import javax.ws.rs.container.Suspended
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response


@Path("user/slot")
class UserSlotResource @Inject constructor(
    private val manager: SlotManager
) : ResourceScope() {

    @Path("{userId: USR.*}/{startTime}/{endTime}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun getSlot(
        @PathParam("userId") userId: String,
        @PathParam("startTime") startTime: Long,
        @PathParam("endTime") endTime: Long,
        @Suspended asyncResponse: AsyncResponse
    ) = withAsyncResponse(asyncResponse) {
        Response.ok(getSlots(userId, startTime, endTime)).build()
    }

    @Path("{userId: USR.*}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun bookSlot(
        @PathParam("userId") userId: String,
        request: SlotDto,
        @Suspended asyncResponse: AsyncResponse
    ) = withAsyncResponse(asyncResponse) {
        Response.ok(manager.bookSlot(userId, request)).build()
    }


    private suspend fun getSlots(
        userId: String,
        startTime: Long,
        endTime: Long
    ): List<SlotDto> {
        require(endTime > startTime) {
            "End time should be greater than start time"
        }
        return manager.getSlots(
            userId,
            startTime,
            endTime
        ).map {
            SlotDto.fromSlot(it)
        }
    }
}
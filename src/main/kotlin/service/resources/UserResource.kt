package service.resources

import `data-stores`.UserStore
import dtos.UserDto
import javax.inject.Inject
import javax.inject.Named
import javax.ws.rs.*
import javax.ws.rs.container.AsyncResponse
import javax.ws.rs.container.Suspended
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("user")
class UserResource @Inject constructor(  @Named("userDB") private val userDB : UserStore) : ResourceScope() {

    @Path("test")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun checkPromotionForListing(
        @Suspended asyncResponse: AsyncResponse
    ) = withAsyncResponse(asyncResponse) {
        Response.ok("hello").build()
    }

    @Path("")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun addUser(
        userRequest : UserDto,
        @Suspended asyncResponse: AsyncResponse
    ) = withAsyncResponse(asyncResponse) {
        userDB.addUser(userRequest.toUser())
        Response.ok("hello").build()
    }
}
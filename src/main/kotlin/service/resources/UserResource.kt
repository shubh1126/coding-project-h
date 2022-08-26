package service.resources

import dtos.UpdateUserDto
import dtos.UserDto
import dtos.UserResponseDto
import managers.UserManager
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.container.AsyncResponse
import javax.ws.rs.container.Suspended
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("user")
class UserResource @Inject constructor(
    private val manager: UserManager
) : ResourceScope() {

    @Path("{userEmail}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun getUserByEmail(
        @PathParam("userEmail") userEmail: String,
        @Suspended asyncResponse: AsyncResponse
    ) = withAsyncResponse(asyncResponse) {

        manager.getUser(userEmail).let { user ->
            Response.ok(UserResponseDto.fromUser(user)).build()
        }

    }

    @Path("{userId: USR.*}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun getUserById(
        @PathParam("userId") userId: String,
        @Suspended asyncResponse: AsyncResponse
    ) = withAsyncResponse(asyncResponse) {
        manager.getUserById(userId).let { user ->
            Response.ok(UserResponseDto.fromUser(user)).build()
        }

    }

    @Path("")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun addUser(
        userRequest: UserDto,
        @Suspended asyncResponse: AsyncResponse
    ) = withAsyncResponse(asyncResponse) {
        manager.addUser(userRequest)
            .let { user ->
                Response.ok(UserResponseDto.fromUser(user)).build()
            }
    }

    @Path("{userId: USR.*}")
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun addUser(
        userRequest: UpdateUserDto,
        @PathParam("userId") userId: String,
        @Suspended asyncResponse: AsyncResponse
    ) = withAsyncResponse(asyncResponse) {
        manager.updateUser(userId, userRequest)
        Response
            .status(204)
            .build()
    }
}
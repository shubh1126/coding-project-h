package service.resources

import dtos.PreferenceDto
import managers.PreferenceManager
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.container.AsyncResponse
import javax.ws.rs.container.Suspended
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("user/preference")
class UserPreferenceResource  @Inject constructor(
    private val manager: PreferenceManager
) : ResourceScope() {

    @Path("{userId: USR.*}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun addPref(
        @PathParam("userId") userId: String,
        request : PreferenceDto,
        @Suspended asyncResponse: AsyncResponse
    ) = withAsyncResponse(asyncResponse) {
        manager.addUserPreferences(userId,request)
            .let { pref ->
                Response.ok(pref).build()
            }
    }


    @Path("{userId: USR.*}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun getPrefByUserId(
        @PathParam("userId") userId: String,
        @Suspended asyncResponse: AsyncResponse
    ) = withAsyncResponse(asyncResponse) {
        manager.getUserPreference(userId).let { pref ->
            Response.ok(pref).build()
        }

    }
}
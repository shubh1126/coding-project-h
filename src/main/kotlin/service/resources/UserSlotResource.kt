package service.resources

import managers.SlotManager
import managers.UserManager
import javax.inject.Inject
import javax.ws.rs.Path


@Path("user/slot")
class UserSlotResource@Inject constructor(
    private val manager: SlotManager
) : ResourceScope() {
}
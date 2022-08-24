package `data-stores`

import models.User
import javax.ws.rs.NotFoundException

class UserStore {
    private val users: MutableList<User> = mutableListOf<User>()

    @Synchronized
    fun addUser(user: User) {
        require(users.firstOrNull { it.email == user.email } == null) {
            "User already registered"
        }
        users.add(user)
    }

    fun getUser(email: String): User =
        users.firstOrNull { it.email == email }
            ?: throw NotFoundException("User is not present in the system")

    fun getUserById(id: String): User =
        users.firstOrNull { it.id == id }
            ?: throw NotFoundException("User is not present in the system")

}
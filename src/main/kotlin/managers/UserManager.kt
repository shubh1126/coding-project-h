package managers

import `data-stores`.UserStore
import dtos.UpdateUserDto
import dtos.UserDto
import models.User
import utils.logger
import javax.inject.Inject
import javax.inject.Named

/**
 * User Manager
 *
 * Not supporting delete user as it will
 * involve deletion of all relevant slots and preferences
 */
class UserManager @Inject constructor(
    @Named("userDB") private val userDB: UserStore
) {
    companion object {
        private val logger by logger()
    }

    suspend fun getUser(email: String): User =
        userDB.getUser(email)

    suspend fun getUserById(id: String): User =
        userDB.getUserById(id)

    suspend fun addUser(user: UserDto): User =
        user.toUser().also {
            userDB.addUser(it).also {
                logger.info("User added successfully")
            }
        }

    /**
     * Update user
     */
    suspend fun updateUser(
        userId: String,
        user: UpdateUserDto
    ) {
        userDB.getUserById(userId).also { oldUser ->
            oldUser.copy(name = user.name)
                .also {
                    userDB.updateUser(oldUser, it)
                }
        }
    }
}

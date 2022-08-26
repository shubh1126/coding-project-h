package managers

import `data-stores`.PreferenceStore
import dtos.PreferenceDto
import dtos.PreferenceResponseDto
import dtos.UpdateUserDto
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
class PreferenceManager @Inject constructor(
    @Named("prefDB") private val prefDB: PreferenceStore,
    private val userManager: UserManager
) {
    private
    companion object {
        private val logger by logger()
    }

    suspend fun addUserPreferences(
        userId: String,
        preferenceDto: PreferenceDto
    ): PreferenceResponseDto{
        val user = userManager.getUserById(userId)
        preferenceDto
            .getPreferences(user)
            .map {
                prefDB.addPreference(it)
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

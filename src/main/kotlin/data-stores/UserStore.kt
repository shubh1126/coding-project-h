package `data-stores`

import models.User

class UserStore {
    private val users: MutableList<User> = mutableListOf<User>()

 @Synchronized fun addUser(user: User) {
        require(users.firstOrNull { it.email == user.email } == null) {
            "User already registered"
        }
        users.add(user)
    }
}
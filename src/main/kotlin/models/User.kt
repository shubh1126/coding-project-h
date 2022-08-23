package models

import utils.Helper

data class User(
    val id: String = Helper.generateUuid("USR"),
    val email: String,
    val name: String,
    val createdAt: Long = Helper.currentDateTime()
) {
    var updatedAt: Long = Helper.currentDateTime()

    companion object{
        fun createUser(email : String, name: String) : User = User(email = email, name = name)
    }
}
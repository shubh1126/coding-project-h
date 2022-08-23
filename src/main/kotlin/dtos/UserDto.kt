package dtos

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import models.User

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserDto(
    val email: String,
    val name: String = email.split("@")[0]
){
    init {
        require(email.filter{it == '@'}.count() == 1){
            "Invalid email"
        }
    }

   fun toUser() : User = User.createUser(email, name)
}
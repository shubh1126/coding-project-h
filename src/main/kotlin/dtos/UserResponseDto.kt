package dtos

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import models.User

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserResponseDto(
    val userId: String,
    val name: String,
    val email: String
) {

    companion object {
        fun fromUser(user: User): UserResponseDto = UserResponseDto(
            user.id,
            user.name,
            user.email
        )
    }
}

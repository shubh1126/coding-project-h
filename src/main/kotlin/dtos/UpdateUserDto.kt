package dtos

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


// Supporting only name update for now
@JsonIgnoreProperties(ignoreUnknown = true)
class UpdateUserDto (
    val name: String
){
    init {
        require(name.isNotBlank()){
            "user name cannot be blank"
        }
    }
}
package model

data class UserDTO(
    var profileImageUrl : String? = null,
    var uid: String? = null,
    var useremail: String? = null,
    var username: String? = null,
    var userBirth: String? = null,
    var favoriteCount: Int = 0,
    var favorites: MutableMap<String, Boolean> = HashMap()
)
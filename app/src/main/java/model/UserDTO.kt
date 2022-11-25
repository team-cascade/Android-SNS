package model

data class UserDTO(
    var profileImageUrl : String? = null,
    var uid: String? = null,
    var useremail: String? = null,
    var username: String? = null,
    var userBirth: String? = null,
    var followerCount: Int = 0,
    var followingCount: Int = 0,
    var followers: MutableMap<String, Boolean> = HashMap(),
    var followings: MutableMap<String, Boolean> = HashMap()
)
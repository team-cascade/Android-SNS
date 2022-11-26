package model

data class UserDTO(
    var profileImageUrl : String? = null,
    var uid: String? = null,
    var useremail: String? = null,
    var username: String? = null,
    var userBirth: String? = null,
    var favoriteCount: Int = 0,
    var favorites: MutableMap<String, Boolean> = HashMap()
){
    data class AlarmDTO(
        var destinationUid: String? = null,
        var username: String? = null,
        var uid: String? = null,
        var kind: Int = 0,
        var message: String? = null,
        var timestamp: Long? = null
    )
}
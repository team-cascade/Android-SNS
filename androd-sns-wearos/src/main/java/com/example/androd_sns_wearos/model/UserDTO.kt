package model

//유저 정보 Data Transfer Object
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
){ //유저 알람 정보 관리
    data class AlarmDTO(
        var destinationUid: String? = null,
        var username: String? = null,
        var uid: String? = null,
        var kind: Int = 0,
        var message: String? = null,
        var timestamp: Long? = null
    )
}

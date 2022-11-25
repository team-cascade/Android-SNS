package model

data class ContentDTO(
    var explain: String? = null,
    var imageUrl : String? = null,
    var profileImageUrl : String? = null,
    var uid: String? = null,
    var useremail: String? = null,
    var username: String? = null,
    var userBirth: String? = null,
    var timestamp: Long? = null,
    var favoriteCount: Int = 0,
    var favorites: MutableMap<String, Boolean> = HashMap(),
){
    data class Comment(
        var uid : String? = null,
        var userId : String? = null,
        var comment : String? = null,
        var timestamp: Long? = null)

}

package model

data class ContentDTO(
    var explain: String? = null,
    var imageUrl: String? = null,
    var uid: String? = null,
    var username: String? = null,
    var timestamp: Long? = null,
    var favoriteCount: Int = 0,
    var favorites: MutableMap<String, Boolean> = HashMap(),
){
    data class CommentDTO(
        var uid : String? = null,
        var username : String? = null,
        var comment : String? = null,
        var timestamp: Long? = null)
}

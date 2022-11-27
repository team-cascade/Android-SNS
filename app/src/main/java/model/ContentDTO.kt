package model

//게시글 내용 Data Transfer Object
data class ContentDTO(
    var explain: String? = null,
    var imageUrl: String? = null,
    var uid: String? = null,
    var username: String? = null,
    var timestamp: Long? = null,
    var favoriteCount: Int = 0,
    var favorites: MutableMap<String, Boolean> = HashMap(),
){ // 게시글 댓글 정보 관리
    data class CommentDTO(
        var uid : String? = null,
        var username : String? = null,
        var comment : String? = null,
        var timestamp: Long? = null)
}

package com.example.android_sns

data class contentDTO (

    var explain : String? = null,    // 설명관리
    var imageUrl  : String ?= null,  // url 저장
    var uid : String? = null,    // 어떤 유저가 올렸는지 관리
    var userId : String? = null,   // 유저 아이디
    var timestamp : Long ?=null,   // 몇시 몇분에 올렸는지 관리
    var favoriteCount : Int = 0,   // 몇명이 좋아요 했는지 카운트
    var favorites : Map<String, Boolean> = HashMap()  // 누가 좋아요 했는지
) {
    data class Comment (
        var uid : String ?=null,    // 누가 댓글 남겼는지
        var userId  : String ?= null,   // 댓글을 남긴 유저의 아이디
        var comment : String ? = null,   // 뭐라고 남겼는지
        var timestamp : Long ?=null    // 몇시 몇분에 올렸는지
    )
}


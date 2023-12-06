package com.sw.gurumemo.retrofit

import java.io.Serializable

data class HotPepperResponse(
    val results: Results
)

data class Results(
    val shop: List<Shop>,
    val start: Int,
    val count: Int
)

data class Shop(
    val id: String,
    val name: String,
    val logo_image: String?,
    val name_kana: String,
    val address: String,
    val genre: Genre,
    val keyword: String?,
    val sub_genre: Genre,
    val photo: Photo
)

data class Genre(
    val code: String,
    val name: String
)

data class Photo(
    val pc: PC,
    val mobile: Mobile
)

data class PC(
    val l: String
)
data class Mobile(
    val l: String,
    val s: String
)


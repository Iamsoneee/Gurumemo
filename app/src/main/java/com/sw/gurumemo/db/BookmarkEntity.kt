package com.sw.gurumemo.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BookmarkEntity (
    @PrimaryKey @ColumnInfo(name = "shop_id") var shopId: String,
    @ColumnInfo(name = "logo_image") val logoImage: String,
    @ColumnInfo(name = "shop_name") val shopName: String,
    @ColumnInfo(name = "catch_phrase") val catchPhrase: String,
    @ColumnInfo(name = "access") val access: String,
    @ColumnInfo(name = "is_bookmarked") val isBookmarked: Boolean = true,
    @ColumnInfo(name = "memo") var memo: String? = null

)
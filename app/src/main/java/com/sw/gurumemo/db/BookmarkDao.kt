package com.sw.gurumemo.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface BookmarkDao {
    @Query("SELECT * FROM BookmarkEntity")
    fun getAllBookmarks() : List<BookmarkEntity>

    @Query("SELECT COUNT(*) FROM BookmarkEntity WHERE shop_id = :shopId")
    fun isShopBookmarked(shopId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM BookmarkEntity WHERE shop_id = :shopId")
    fun deleteBookmark(shopId: String)

    @Query("UPDATE BookmarkEntity SET memo = :newMemo WHERE shop_id = :shopId")
    fun updateMemo(shopId: String, newMemo: String)

}
package com.sw.gurumemo.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM BookmarkEntity")
    fun getAll() : List<BookmarkEntity>

    @Query("SELECT COUNT(*) FROM BookmarkEntity WHERE shop_id = :shopId")
    fun isShopBookmarked(shopId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM BookmarkEntity WHERE shop_id = :shopId")
    fun deleteBookmark(shopId: String)

    @Query("UPDATE BookmarkEntity SET memo = :newMemo WHERE shop_id = :shopId")
    fun updateMemo(shopId: String, newMemo: String)

//    @Delete
//    fun deleteBookmark(bookmark: BookmarkEntity)
}
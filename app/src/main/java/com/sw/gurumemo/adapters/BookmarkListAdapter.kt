package com.sw.gurumemo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sw.gurumemo.R
import com.sw.gurumemo.databinding.FragmentBookmarkBinding
import com.sw.gurumemo.databinding.ItemBookmarkBinding
import com.sw.gurumemo.db.BookmarkEntity
import com.sw.gurumemo.retrofit.Shop

class BookmarkListAdapter(private val bookmarkList: ArrayList<BookmarkEntity>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<BookmarkListAdapter.ViewHolder>() {

    inner class ViewHolder(binding: ItemBookmarkBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val root = binding.root
        val iv_thumbnail_image = binding.ivThumbnailImage
        val tv_shop_name = binding.tvShopName
        val tv_catch_phrase = binding.tvCatchPhrase
        val tv_access = binding.tvAccess
        val btn_bookmark_icon = binding.btnBookmarkIcon
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BookmarkListAdapter.ViewHolder {
        val binding: ItemBookmarkBinding =
            ItemBookmarkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookmarkListAdapter.ViewHolder, position: Int) {
        val bookmarkData = bookmarkList[position]

        if(!bookmarkData.logoImage.contains("m30_img_noimage")){
            Glide.with(holder.itemView.context).load(bookmarkData.logoImage)
                .into(holder.iv_thumbnail_image)
        }else{
            Glide.with(holder.itemView.context).load(R.drawable.profile_image_default).into(holder.iv_thumbnail_image)
        }


        holder.tv_shop_name.text = bookmarkData.shopName.toString()
        holder.tv_catch_phrase.text = bookmarkData.catchPhrase.toString()
        holder.tv_access.text = bookmarkData.access.toString()
        when (bookmarkData.isBookmarked) {
            true -> holder.btn_bookmark_icon.isSelected = true
            else -> holder.btn_bookmark_icon.isSelected = false
        }

        holder.btn_bookmark_icon.setOnClickListener {
            listener.onClick(it, position)
        }
    }

    override fun getItemCount(): Int {
        return bookmarkList.size
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

//    fun setItemClickListener(onItemClickListener: OnItemClickListener){
//        this.itemClickListener = onItemClickListener
//    }
//
//    private lateinit var itemClickListener: OnItemClickListener

}
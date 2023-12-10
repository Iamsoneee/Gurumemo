package com.sw.gurumemo.adapters

import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sw.gurumemo.R
import com.sw.gurumemo.ShopDetailActivity
import com.sw.gurumemo.databinding.ItemBookmarkBinding
import com.sw.gurumemo.db.BookmarkEntity


class BookmarkListAdapter(
    private val bookmarkList: ArrayList<BookmarkEntity>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<BookmarkListAdapter.ViewHolder>() {

    inner class ViewHolder(binding: ItemBookmarkBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val root = binding.root
        val iv_thumbnail_image = binding.ivThumbnailImage
        val tv_shop_name = binding.tvShopName
        val tv_catch_phrase = binding.tvCatchPhrase
        val tv_access = binding.tvAccess
        val btn_bookmark_icon = binding.btnBookmarkIcon
        val iv_check_icon = binding.ivCheckIcon
        var et_memo = binding.etMemo

        // TextWatcher 객체 정의
        private val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Before Text Changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // On Text Changed
            }

            override fun afterTextChanged(s: Editable?) {
                // After Text Changed
                if (s?.length!! > 0) {
                    iv_check_icon.visibility = View.VISIBLE
                } else {
                    iv_check_icon.visibility = View.INVISIBLE
                }
                bookmarkList[adapterPosition].memo = s.toString()
            }
        }

        init {
            et_memo.addTextChangedListener(textWatcher)
        }
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

        if (!bookmarkData.logoImage.contains("m30_img_noimage")) {
            Glide.with(holder.itemView.context).load(bookmarkData.logoImage)
                .into(holder.iv_thumbnail_image)
        } else {
            Glide.with(holder.itemView.context).load(R.drawable.profile_image_default)
                .into(holder.iv_thumbnail_image)
        }


        holder.tv_shop_name.text = bookmarkData.shopName
        holder.tv_catch_phrase.text = bookmarkData.catchPhrase
        holder.tv_access.text = bookmarkData.access
        when (bookmarkData.isBookmarked) {
            true -> holder.btn_bookmark_icon.isSelected = true
            else -> holder.btn_bookmark_icon.isSelected = false
        }

        holder.btn_bookmark_icon.setOnClickListener {
            listener.onBookmarkIconClick(it, position)
        }

        holder.iv_check_icon.setOnClickListener {
            listener.onCheckIconClick(it, position, holder.et_memo.text.toString())
        }

        holder.et_memo.setText(bookmarkData.memo)

        holder.root.setOnClickListener {
            listener.onItemClick(it, position, bookmarkData.shopId)
        }

    }

    override fun getItemCount(): Int {
        return bookmarkList.size
    }

    interface OnItemClickListener {
        fun onBookmarkIconClick(v: View, position: Int)

        fun onItemClick(v:View, position: Int, shopId: String)

        fun onCheckIconClick(v: View, position: Int, memo: String)
    }

//    fun setItemClickListener(onItemClickListener: OnItemClickListener){
//        this.itemClickListener = onItemClickListener
//    }
//
//    private lateinit var itemClickListener: OnItemClickListener

}
package com.sw.gurumemo.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.sw.gurumemo.R
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
        val fl_edit_memo_area= binding.flEditMemoArea

        // TextWatcher オブジェクト定義
        private val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (s?.length!! > 0) {
                    iv_check_icon.visibility = View.VISIBLE
                } else {
                    iv_check_icon.visibility = View.INVISIBLE
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
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
            Glide.with(holder.itemView.context).load(bookmarkData.logoImage).override(Target.SIZE_ORIGINAL)
                .into(holder.iv_thumbnail_image)
        } else {
            Glide.with(holder.itemView.context).load(R.drawable.default_shop_logo).apply(
                RequestOptions()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
            )
                .into(holder.iv_thumbnail_image)
        }


        holder.tv_shop_name.text = bookmarkData.shopName

        if (bookmarkData.catchPhrase.isNotEmpty()) {
            holder.tv_catch_phrase.text = bookmarkData.catchPhrase
        } else {
            holder.tv_catch_phrase.text = bookmarkData.genreCatchPhrase
        }

        //　ブックマークトグルボタン
        holder.tv_access.text = bookmarkData.access
        when (bookmarkData.isBookmarked) {
            true -> holder.btn_bookmark_icon.isSelected = true
            else -> holder.btn_bookmark_icon.isSelected = false
        }

        holder.btn_bookmark_icon.setOnClickListener {
            listener.onBookmarkIconClick(it, position)
        }

        //　チェックアイコンをクリックすると、メモ領域のカーソルが消える
        holder.iv_check_icon.setOnClickListener {
            listener.onCheckIconClick(it, position, holder.et_memo.text.toString())
            holder.et_memo.clearFocus()
            holder.fl_edit_memo_area.focusable = View.FOCUSABLE
            holder.fl_edit_memo_area.isFocusableInTouchMode = true
            holder.fl_edit_memo_area.requestFocus()
        }

        holder.et_memo.setText(bookmarkData.memo)


        holder.root.setOnClickListener {
            listener.onItemClick(it, position, bookmarkData.shopId, bookmarkData.shopName)

        }

    }

    override fun getItemCount(): Int {
        return bookmarkList.size
    }

    //　BookmarkFragment で RecyclerView アイテム要素のタッチイベントを実装するためのインターフェース
    interface OnItemClickListener {
        fun onBookmarkIconClick(v: View, position: Int)

        fun onItemClick(v: View, position: Int, shopId: String, shopName: String)

        fun onCheckIconClick(v: View, position: Int, memo: String)
    }
}
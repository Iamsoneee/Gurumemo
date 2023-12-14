package com.sw.gurumemo.adapters

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.sw.gurumemo.R
import com.sw.gurumemo.ShopDetailActivity
import com.sw.gurumemo.databinding.ItemImageSliderBinding
import com.sw.gurumemo.retrofit.Shop

class HomeShopListAdapter(private val context: Context) :
    RecyclerView.Adapter<HomeShopListAdapter.ViewHolder>() {
    private val shops: MutableList<Shop> = mutableListOf()

    class ViewHolder(private val binding: ItemImageSliderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(shop: Shop) {
            binding.apply {
                Glide.with(itemView.context).load(shop.photo.pc.l).override(Target.SIZE_ORIGINAL)
                    .into(ivShop)
                tvShopName.text = shop.name
                tvShopName.visibility = View.VISIBLE
            }
        }

        // 日本に住んでいるが、周辺半径に提供できる店舗情報がない場合、デフォルトの画像を設定
        fun bindDefaultImage() {
            binding.apply {
                Glide.with(itemView.context)
                    .load(R.drawable.default_unavailable_guide_image)
                    .into(ivShop)
                tvShopName.visibility = View.GONE

            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemImageSliderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (shops.isNullOrEmpty()) {
            holder.bindDefaultImage()
        } else {
            val shop = shops[position]
            holder.bind(shop)
            holder.itemView.setOnClickListener {
                val intent = Intent(context, ShopDetailActivity::class.java)
                intent.putExtra("shopData", shop)
                context.startActivity(intent)
            }
        }


    }

    override fun getItemCount(): Int {
        return if (shops.isEmpty()) 1 else shops.size
    }


    fun setData(newShops: List<Shop>) {
        if (newShops.isNotEmpty()) {
            shops.clear()
            shops.addAll(newShops)
            notifyDataSetChanged()
        }
    }

}
package com.sw.gurumemo.adapters

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
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
                Glide.with(itemView.context).load(shop.photo.pc.l)
                    .apply(
                        RequestOptions()
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                    ).into(ivShop)
                Log.d("HomeShopAdapter", shop.photo.mobile.l)
                tvShopName.text = shop.name
            }
        }


        // 추가: 디폴트 이미지를 보여주는 bind 메서드
        fun bindDefaultImage() {
            binding.apply {
                Glide.with(itemView.context)
                    .load(R.drawable.default_shop_logo)
                    .apply(
                        RequestOptions()
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                    )
                    .into(ivShop)
                tvShopName.text = "no shop data"
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemImageSliderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val shop = shops[position]
//        holder.bind(shop)
//        holder.itemView.setOnClickListener {
//            val intent = Intent(context, ShopDetailActivity::class.java)
//            intent.putExtra("shopData", shop)
//            context.startActivity(intent)
//        }

        if (shops.isNullOrEmpty()) {
            Log.e("HomeShopListAdapter", "Default image setting")
            // 데이터가 없을 때 디폴트 이미지를 보여주도록 처리
            holder.bindDefaultImage()
        } else {
            // 데이터가 있을 때 Shop 객체를 bind
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
        return shops.size
    }


    fun setData(newShops: List<Shop>) {
        if (newShops.isNotEmpty()) {
            shops.clear()
            shops.addAll(newShops)
            notifyDataSetChanged()
        }
    }

}
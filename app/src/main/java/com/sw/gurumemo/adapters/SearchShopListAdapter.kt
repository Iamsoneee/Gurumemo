package com.sw.gurumemo.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sw.gurumemo.R
import com.sw.gurumemo.ShopDetailActivity
import com.sw.gurumemo.databinding.ItemShopBinding
import com.sw.gurumemo.retrofit.Shop

class SearchShopListAdapter(private val context: Context) :
    RecyclerView.Adapter<SearchShopListAdapter.ViewHolder>() {
    private val shops: MutableList<Shop> = mutableListOf()

    inner class ViewHolder(private val binding: ItemShopBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(shop: Shop) {
            binding.apply {
                if(!shop.logo_image.contains("m30_img_noimage")){
                Glide.with(itemView.context).load(shop.logo_image).into(ivThumbnailImage)
                }else{
                    Glide.with(itemView.context).load(R.drawable.default_shop_logo).into(ivThumbnailImage)
                }
                tvShopName.text = shop.name
                tvAccess.text = shop.access

                if (shop.genre.catch.isBlank()) {
                    tvCatchPhrase.text = shop.catch
                } else {
                    tvCatchPhrase.text = shop.genre.catch
                }

                tvStationName.text = "${shop.station_name}駅"
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shop = shops[position]
        holder.bind(shop)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ShopDetailActivity::class.java)
            intent.putExtra("shopData", shop)
            context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShopBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return shops.size
    }

    fun setData(newShops: List<Shop>) {
        shops.clear()
        shops.addAll(newShops)
        notifyDataSetChanged()
    }

    fun addData(newShops: List<Shop>) {
        shops.addAll(newShops)
        notifyDataSetChanged()
    }

    fun clearData() {
        shops.clear()
        notifyDataSetChanged()
    }

}
package com.sw.gurumemo.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
                Glide.with(itemView.context).load(shop.photo.pc.l).into(ivShop)
                binding.tvShopName.text = shop.name
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemImageSliderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shop = shops[position]
        holder.bind(shop)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ShopDetailActivity::class.java)
            intent.putExtra("sliderShopData", shop)
            context.startActivity(intent)
        }
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

}
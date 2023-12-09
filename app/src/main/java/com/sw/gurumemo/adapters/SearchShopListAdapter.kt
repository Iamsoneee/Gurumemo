package com.sw.gurumemo.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.sw.gurumemo.R
import com.sw.gurumemo.ShopDetailActivity
import com.sw.gurumemo.databinding.ItemShopBinding
import com.sw.gurumemo.retrofit.Shop

class SearchShopListAdapter(private val context: Context) :
    RecyclerView.Adapter<SearchShopListAdapter.ViewHolder>() {
    private val shops: MutableList<Shop> = mutableListOf()

    inner class ViewHolder(private val binding: ItemShopBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val buttonItem: Button = binding.btnBookmarkIcon

        fun bind(shop: Shop) {
            binding.apply {
                Glide.with(itemView.context).load(shop.logo_image).into(ivThumbnailImage)
                binding.tvShopName.text = shop.name
                binding.tvAccess.text = shop.access

                if (shop.genre.catch.isBlank()) {
                    binding.tvCatchPhrase.text = shop.catch
                } else {
                    binding.tvCatchPhrase.text = shop.genre.catch
                }
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

        holder.buttonItem.setOnClickListener {
        holder.buttonItem.isSelected = !holder.buttonItem.isSelected
            if (holder.buttonItem.isSelected) {
                Log.e("SearchShopListAdapter", "BOOKMARK ADDED")
            } else {
                Log.e("SearchShopListAdapter", "BOOKMARK REMOVED")
            }
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
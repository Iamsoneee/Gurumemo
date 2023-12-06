package com.sw.gurumemo.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sw.gurumemo.ShopDetailActivity
import com.sw.gurumemo.databinding.ItemImageSliderBinding
import com.sw.gurumemo.retrofit.Shop

class ShopListAdapter(private val context: Context) :
    RecyclerView.Adapter<ShopListAdapter.ViewHolder>() {
     private val shops: MutableList<Shop> = mutableListOf()

     class ViewHolder(private val binding: ItemImageSliderBinding) : RecyclerView.ViewHolder(binding.root){
         fun bind(shop: Shop){
             binding.apply {
                 Glide.with(itemView.context).load(shop.photo.pc.l).into(ivShop)
                 binding.tvShopName.text = shop.name
             }
         }
     }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImageSliderBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shop = shops[position]
        holder.bind(shop)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ShopDetailActivity::class.java)
            intent.putExtra("shopID", shop.id)
            context.startActivity(intent)
            (context as? FragmentActivity)?.finish()
        }
    }

    override fun getItemCount(): Int {
        return shops.size
    }

//    interface OnItemClickListener{
//        fun onClick(v: View, position: Int)
//    }
//
//    fun setItemClickListener(onItemClickListener:OnItemClickListener){
//        this.itemClickListener = onItemClickListener
//    }
//
//    private lateinit var itemClickListener : OnItemClickListener

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
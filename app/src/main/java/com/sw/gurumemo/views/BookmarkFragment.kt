package com.sw.gurumemo.views

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sw.gurumemo.Constants
import com.sw.gurumemo.R
import com.sw.gurumemo.ShopDetailActivity
import com.sw.gurumemo.adapters.BookmarkListAdapter
import com.sw.gurumemo.databinding.FragmentBookmarkBinding
import com.sw.gurumemo.db.AppDatabase
import com.sw.gurumemo.db.BookmarkDao
import com.sw.gurumemo.db.BookmarkEntity
import com.sw.gurumemo.retrofit.HotPepperService
import com.sw.gurumemo.retrofit.RetrofitConnection
import com.sw.gurumemo.retrofit.Shop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookmarkFragment : Fragment(), BookmarkListAdapter.OnItemClickListener {

    private var binding: FragmentBookmarkBinding? = null

    private lateinit var db: AppDatabase
    private lateinit var bookmarkDao: BookmarkDao
    private lateinit var bookmarkList: ArrayList<BookmarkEntity>
    private lateinit var adapter: BookmarkListAdapter

    private var shop: Shop? = null

    private val TAG = "BookmarkFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookmarkBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setting ActionBar
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding?.toolbarBookmarkFragment)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            title = resources.getString(R.string.bookmark)
        }

        db = AppDatabase.getInstance(requireContext())
        bookmarkDao = db.getBookmarkDao()

        getAllBookmarkList()

    }

    private fun getAllBookmarkList() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                bookmarkList = ArrayList(bookmarkDao.getAll())
            }
            Log.d(TAG,"$bookmarkList")
            withContext(Dispatchers.Main){
                if (bookmarkList.isEmpty()) {
                    binding?.llNoBookmark?.visibility = View.VISIBLE
                }else{
                binding?.llNoBookmark?.visibility = View.GONE
                }
            }
            setRecyclerView()
        }
    }

    private fun setRecyclerView() {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                adapter = BookmarkListAdapter(bookmarkList, this@BookmarkFragment)
                binding?.rvBookmarkList?.adapter = adapter
                binding?.rvBookmarkList?.layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }

    override fun onBookmarkIconClick(v: View, position: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.delete_bookmark_alert_title)
        builder.setMessage(R.string.delete_bookmark_alert_message)
        builder.setNegativeButton(R.string.delete_bookmark_alert_cancel, null)
        builder.setPositiveButton(
            R.string.delete_bookmark_alert_proceed,
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    deleteBookmark(position)
                }
            })
        builder.show()
    }

    override fun onItemClick(v: View, position: Int, shopId: String, shopName: String) {
        getShopInfo(shopId, shopName)
    }

    override fun onCheckIconClick(v: View, position: Int, memo: String) {
        updateMemo(position, memo)
        v.visibility = View.INVISIBLE
    }

    private fun deleteBookmark(position: Int) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                bookmarkDao.deleteBookmark(bookmarkList[position].shopId)
                bookmarkList.removeAt(position)
                withContext(Dispatchers.Main) {
                    adapter.notifyDataSetChanged()
                    if (bookmarkList.isEmpty()) {
                        binding?.llNoBookmark?.visibility = View.VISIBLE
                    }else{
                        binding?.llNoBookmark?.visibility = View.GONE
                    }
                    Toast.makeText(requireContext(), "ブックマークの削除が完了しました。", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun updateMemo(position: Int, newMemo: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                bookmarkDao.updateMemo(bookmarkList[position].shopId, newMemo)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "メモが保存されました。", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun getShopInfo(shopId: String, shopName: String) {
        val retrofitAPI = RetrofitConnection.getInstance().create(HotPepperService::class.java)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    retrofitAPI.getGourmetData(
                        apiKey = Constants.HOTPEPPER_API_KEY,
                        id = shopId,
                        nameAny = shopName
                    )
                }
               val isShopEmpty = response.results.shop.isEmpty()
                if (!isShopEmpty) {
                    shop = response.results.shop[0]
                    val intent = Intent(requireContext(), ShopDetailActivity::class.java)
                    intent.putExtra("shopData", shop)
                    startActivity(intent)
                } else {
                    Log.e(TAG, "No data found from API request.")
                    Toast.makeText(requireContext(), "店舗情報が見つかりませんでした。", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "API request error: ${e.message}", e)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getAllBookmarkList()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }


}

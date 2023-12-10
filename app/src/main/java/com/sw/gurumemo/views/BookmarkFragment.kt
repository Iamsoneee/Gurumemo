package com.sw.gurumemo.views

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sw.gurumemo.R
import com.sw.gurumemo.adapters.BookmarkListAdapter
import com.sw.gurumemo.databinding.FragmentBookmarkBinding
import com.sw.gurumemo.db.AppDatabase
import com.sw.gurumemo.db.BookmarkDao
import com.sw.gurumemo.db.BookmarkEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookmarkFragment : Fragment(), BookmarkListAdapter.OnItemClickListener {

    private var binding: FragmentBookmarkBinding? = null

    private lateinit var db: AppDatabase
    private lateinit var bookmarkDao: BookmarkDao
    private lateinit var bookmarkList: ArrayList<BookmarkEntity>
    private lateinit var adapter: BookmarkListAdapter

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

        db = AppDatabase.getInstance(requireContext())!!
        bookmarkDao = db.getBookmarkDao()

        getAllBookmarkList()

    }

    private fun getAllBookmarkList() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                bookmarkList = ArrayList(bookmarkDao.getAll())
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
        if (bookmarkList.isNullOrEmpty()) {
            binding?.llNoBookmark?.visibility = View.VISIBLE
        }
        binding?.llNoBookmark?.visibility = View.GONE
    }

    override fun onClick(v: View, position: Int) {
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

    private fun deleteBookmark(position: Int) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                bookmarkDao.deleteBookmark(bookmarkList[position].shopId)
                bookmarkList.removeAt(position)
                withContext(Dispatchers.Main) {
                    adapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "ブックマークの削除が完了しました。", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }


}

package com.sw.gurumemo.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.sw.gurumemo.MainActivity
import com.sw.gurumemo.R
import com.sw.gurumemo.databinding.FragmentBookmarkBinding

class BookmarkFragment : Fragment() {

    private var binding : FragmentBookmarkBinding? = null
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

//        binding?.toolbarBookmarkFragment?.setNavigationOnClickListener {
//            (requireActivity() as MainActivity).binding.bottomNavigationView.selectedItemId = R.id.fragment_home
//        }
    }
    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

}

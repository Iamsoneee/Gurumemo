package com.sw.gurumemo.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sw.gurumemo.MainActivity
import com.sw.gurumemo.R
import com.sw.gurumemo.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var binding : FragmentHomeBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.btnSearch?.setOnClickListener {
            (requireActivity() as MainActivity).binding.bottomNavigationView.selectedItemId = R.id.fragment_search
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

}
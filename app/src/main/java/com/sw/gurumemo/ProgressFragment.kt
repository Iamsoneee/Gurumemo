package com.sw.gurumemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sw.gurumemo.databinding.FragmentHomeBinding
import com.sw.gurumemo.databinding.FragmentProgressBinding

class ProgressFragment : Fragment() {

    private var binding: FragmentProgressBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProgressBinding.inflate(inflater, container, false)
        binding?.pbCircularProgressBar?.visibility = View.VISIBLE
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
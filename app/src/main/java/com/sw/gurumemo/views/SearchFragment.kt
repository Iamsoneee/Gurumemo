package com.sw.gurumemo.views

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.sw.gurumemo.R
import com.sw.gurumemo.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var binding : FragmentSearchBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding?.ivBackButton?.setOnClickListener {
        // TODO - HOME FRAGMENT로 이동해야 함 (Navigation 사용)
        }

    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
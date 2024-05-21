package com.example.wavesound.ui.online


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.wavesound.R

class OnlineFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       super.onCreate(savedInstanceState)
        val root = inflater.inflate(R.layout.fragment_online, container, false)

        return root
    }
}
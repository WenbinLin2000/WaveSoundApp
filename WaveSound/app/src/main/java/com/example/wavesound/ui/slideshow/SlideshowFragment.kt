package com.example.wavesound.ui.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.wavesound.R

class SlideshowFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        val root = inflater.inflate(R.layout.fragment_usuario, container, false)
        val textView: TextView = root.findViewById(R.id.text_slideshow)
        textView.text = "This is slideshow Fragment"


        return root
    }

}
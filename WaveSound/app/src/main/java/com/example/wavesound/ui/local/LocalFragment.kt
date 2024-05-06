package com.example.wavesound.ui.local

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.wavesound.R
import com.example.wavesound.ViewPagerAdapter
import com.example.wavesound.databinding.FragmentLocalBinding
import com.google.android.material.tabs.TabLayoutMediator

class LocalFragment : Fragment() {

    private var _binding: FragmentLocalBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocalBinding.inflate(inflater, container, false)

        val root = binding.root

        val fragmentList = arrayListOf(LocalAllSong(), LocalPlayList(), LocalFavorite())

        binding.apply {
            viewPager.adapter = ViewPagerAdapter(fragmentList, requireActivity().supportFragmentManager, lifecycle)
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                when(position) {
                    0 -> {
                        tab.text = getString(R.string.all_songs)
                        tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_library_music_24)
                    }
                    1 -> {
                        tab.text = getString(R.string.playlists)
                        tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_playlist_play_24)
                    }
                    2 -> {
                        tab.text = getString(R.string.favorites)
                        tab.icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_favorite_24)
                    }
                }
            }.attach()
        }

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.konwerter.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.konwerter.data.Category
import com.example.konwerter.ui.fragments.ConversionFragment

class CategoryPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val categories: List<Category>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        return ConversionFragment.newInstance(categories[position])
    }
}
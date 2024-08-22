package com.hunsu.climbfeedback

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.viewpager2.widget.ViewPager2
import com.hunsu.climbfeedback.databinding.ActivityMainBinding
import com.hunsu.climbfeedback.db.data.ClimbingLogViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager: ViewPager2
    lateinit var viewModel: ClimbingLogViewModel

    companion object{
        lateinit var mainactivity:MainActivity
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainactivity=this
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel= ViewModelProvider(this).get(ClimbingLogViewModel::class.java)


        NavigationUI.setupWithNavController(binding.navBar, findNavController(R.id.nav_host))

    }
}
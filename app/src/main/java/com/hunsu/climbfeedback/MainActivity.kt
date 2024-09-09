package com.hunsu.climbfeedback

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hunsu.climbfeedback.databinding.ActivityMainBinding
import com.hunsu.climbfeedback.db.data.ClimbingLogViewModel
import com.hunsu.climbfeedback.R.layout.activity_main
import com.hunsu.climbfeedback.mainfrag.BookFragment
import com.hunsu.climbfeedback.mainfrag.CalendarFragment
import com.hunsu.climbfeedback.mainfrag.VideoFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager: ViewPager2
    private val manager = supportFragmentManager
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

        showInit()
        initNavBar()
        videoBtn()
    }

    private fun showInit() {
        val transaction = manager.beginTransaction()
            .add(R.id.main_frm,CalendarFragment())
        transaction.commitAllowingStateLoss()
    }

    private fun initNavBar() {

        //binding.navBar.itemIconTintList = null
        binding.navBar.menu.getItem(1).isEnabled = false
        binding.navBar.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.cal_Fragment -> {
                    CalendarFragment().changeFragment()
                }
                R.id.book_Fragment -> {
                    BookFragment().changeFragment()
                }
            }
            return@setOnItemSelectedListener true
        }
//        binding.navBar.setOnItemReselectedListener {
//
//        }
    }


    private fun videoBtn() {
        binding.mainFloatingBtn.setOnClickListener {
            VideoFragment().changeFragment()
        }
    }

    private fun Fragment.changeFragment(){
        manager.beginTransaction().replace(R.id.main_frm, this).commitAllowingStateLoss()
    }

}




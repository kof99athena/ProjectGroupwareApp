package com.athena.projectgroupwareapp.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.athena.projectgroupwareapp.R
import com.athena.projectgroupwareapp.databinding.ActivityMainBinding
import com.athena.projectgroupwareapp.drawer.CalendarActivity
import com.athena.projectgroupwareapp.drawer.ConfirmActivity
import com.athena.projectgroupwareapp.drawer.DailyActivity
import com.athena.projectgroupwareapp.drawer.SalaryActivity
import com.athena.projectgroupwareapp.main.tab1.Tab1Fragment
import com.athena.projectgroupwareapp.main.tab2.Tab2Fragment
import com.athena.projectgroupwareapp.main.tab3.Tab3Fragment
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    lateinit var tab1Fragment : Tab1Fragment
    lateinit var tab2Fragment : Tab2Fragment
    lateinit var tab3Fragment : Tab3Fragment
    private lateinit var binding : ActivityMainBinding
    lateinit var toggle: ActionBarDrawerToggle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tab1Fragment= Tab1Fragment()
        tab2Fragment= Tab2Fragment()
        tab3Fragment= Tab3Fragment()

        supportFragmentManager.beginTransaction().replace(R.id.framelayout,tab1Fragment).commit()

        binding.navBottom.setOnItemSelectedListener{item->
            when(item.itemId) {
                R.id.home -> {
                    supportFragmentManager.beginTransaction().replace(R.id.framelayout,tab1Fragment).commit()
                    true
                }

                R.id.message -> {
                    supportFragmentManager.beginTransaction().replace(R.id.framelayout,tab2Fragment).commit()
                    true
                }
                R.id.people ->{
                    supportFragmentManager.beginTransaction().replace(R.id.framelayout,tab3Fragment).commit()
                    true
                }
                else -> false
            }
        }


        setSupportActionBar(binding.toolbar)
        toggle= ActionBarDrawerToggle(this,binding.drawerLayout,R.string.drawer_open,R.string.drawer_close)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.drawerLayout.addDrawerListener(toggle) //삼선모양과 백버튼이 자동으로 연결된다.
        toggle.syncState()


        binding.menuSalary.setOnClickListener{
            var intent : Intent = Intent(this@MainActivity,SalaryActivity::class.java)
            startActivity(intent)
        }

        binding.menuDaily.setOnClickListener{
            var intent : Intent = Intent(this@MainActivity,DailyActivity::class.java)
            startActivity(intent)
        }

        binding.menuConfirm.setOnClickListener{
            var intent : Intent = Intent(this@MainActivity,ConfirmActivity::class.java)
            startActivity(intent)
        }

        binding.menuCalendar.setOnClickListener{
            var intent : Intent = Intent(this@MainActivity,CalendarActivity::class.java)
            startActivity(intent)
        }

        binding.setting.setOnClickListener{
            Snackbar.make(binding.drawerLayout,"권한이 없습니다. \n관리자에게 문의하세요.",Snackbar.LENGTH_INDEFINITE).setAction("X", {}).show()
        }

        binding.drawerLayout.closeDrawers()

    }//onCreate

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // 이 함수가 있어야 서랍을 열고 닫을 수 있다.
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
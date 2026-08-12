package com.ailenezareti.panelapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ailenezareti.panelapp.Prefs
import com.ailenezareti.panelapp.api.ApiClient
import com.ailenezareti.panelapp.databinding.ActivityMainBinding
import com.ailenezareti.panelapp.model.Child
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var childAdapter: ChildChipAdapter
    var children: List<Child> = emptyList()
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        childAdapter = ChildChipAdapter { child -> onChildSelected(child) }
        binding.childRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.childRecycler.adapter = childAdapter

        binding.logoutButton.setOnClickListener {
            Prefs.clearToken(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                com.ailenezareti.panelapp.R.id.nav_home -> HomeFragment()
                com.ailenezareti.panelapp.R.id.nav_location -> LocationFragment()
                com.ailenezareti.panelapp.R.id.nav_calls -> CallsFragment()
                com.ailenezareti.panelapp.R.id.nav_alerts -> AlertsFragment()
                else -> HomeFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(com.ailenezareti.panelapp.R.id.fragmentContainer, fragment)
                .commit()
            true
        }

        loadChildren()
    }

    fun activeChild(): Child? {
        val id = Prefs.activeChildId(this)
        return children.find { it.id == id } ?: children.firstOrNull()
    }

    private fun onChildSelected(child: Child) {
        Prefs.setActiveChildId(this, child.id)
        childAdapter.setActive(child.id)
        refreshCurrentFragment()
    }

    private fun refreshCurrentFragment() {
        val current = supportFragmentManager.findFragmentById(com.ailenezareti.panelapp.R.id.fragmentContainer)
        (current as? Refreshable)?.refresh()
    }

    private fun loadChildren() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.get(this@MainActivity).getChildren()
                runOnUiThread {
                    if (response.isSuccessful && response.body() != null) {
                        children = response.body()!!.children
                        if (children.isEmpty()) {
                            Toast.makeText(this@MainActivity, com.ailenezareti.panelapp.R.string.no_children, Toast.LENGTH_LONG).show()
                            return@runOnUiThread
                        }
                        var activeId = Prefs.activeChildId(this@MainActivity)
                        if (children.none { it.id == activeId }) {
                            activeId = children.first().id
                            Prefs.setActiveChildId(this@MainActivity, activeId)
                        }
                        childAdapter.submit(children, activeId)

                        if (savedFragmentIsEmpty()) {
                            supportFragmentManager.beginTransaction()
                                .replace(com.ailenezareti.panelapp.R.id.fragmentContainer, HomeFragment())
                                .commit()
                        } else {
                            refreshCurrentFragment()
                        }
                    } else if (response.code() == 401) {
                        Prefs.clearToken(this@MainActivity)
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, com.ailenezareti.panelapp.R.string.network_error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun savedFragmentIsEmpty(): Boolean =
        supportFragmentManager.findFragmentById(com.ailenezareti.panelapp.R.id.fragmentContainer) == null
}

package com.worthit.gastospersonales

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.worthit.gastospersonales.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Same behavior on every Android version. The status bar keeps the screen color; only the
        // app bar below it is colored, so the colored strip never reaches the top edge.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, keyboard.bottom))
            WindowInsetsCompat.CONSUMED
        }

        val navHost = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        val navController = navHost.navController

        // Spent is the only top-level destination; Category and Coin show a back arrow.
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.spentListFragment))
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        binding.toolbar.inflateMenu(R.menu.main_menu)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_language) {
                showLanguageDialog()
                true
            } else {
                NavigationUI.onNavDestinationSelected(item, navController)
            }
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isHome = destination.id == R.id.spentListFragment
            binding.toolbar.menu.setGroupVisible(0, isHome)
            // Home shows the app name; every other screen shows its own name. NavigationUI already
            // sets the label, this only swaps the home label for the app name.
            binding.toolbar.title = if (isHome) getString(R.string.app_name) else destination.label
        }
    }

    // Changing the application locale recreates the activity with the new strings.
    private fun showLanguageDialog() {
        val tags = arrayOf("en", "es")
        val names = arrayOf(getString(R.string.language_english), getString(R.string.language_spanish))
        val current = tags.indexOf(resources.configuration.locales[0].language).coerceAtLeast(0)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.language_title)
            .setSingleChoiceItems(names, current) { dialog, which ->
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tags[which]))
                dialog.dismiss()
            }
            .show()
    }
}

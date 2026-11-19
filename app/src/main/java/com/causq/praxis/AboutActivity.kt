package com.causq.praxis

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.causq.praxis.databinding.ActivityAboutBinding
import java.util.Calendar

class AboutActivity : BaseNavigationActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup navigation using base class
        setupNavigation(binding.drawerLayout, binding.navView, binding.toolbar)

        // Set version info
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            binding.content.tvVersion.text = "Version $version"
        } catch (e: Exception) {
            binding.content.tvVersion.text = "Version 1.0.0"
        }

        // Set copyright with current year
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        binding.content.tvCopyright.text = "© $currentYear CausQ"

        // Click listeners for links
        binding.content.tvDocs.setOnClickListener {
            openUrl("https://docs.praxis.causq.com")
        }

        binding.content.tvHowItWorks.setOnClickListener {
            openUrl("https://docs.praxis.causq.com/about/how-praxis-works")
        }

        binding.content.tvTos.setOnClickListener {
            openUrl("https://praxis.net/tos")
        }

        binding.content.tvPrivacy.setOnClickListener {
            openUrl("https://praxis.net/privacy")
        }

        binding.fab.hide()
    }

    override fun getSelectedNavItemId(): Int {
        return R.id.nav_about
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}

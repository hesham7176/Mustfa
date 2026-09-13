package com.mustfa.mediaexplorer.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

data class InstalledApp(val packageName: String, val label: String, val versionName: String?, val sourcePath: String?, val launchable: Boolean)

class AppManagerRepository(private val context: Context) {
    private val packageManager = context.packageManager

    fun list(includeSystem: Boolean = false): List<InstalledApp> = installedApplications()
        .filter { includeSystem || (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
        .map { info -> InstalledApp(info.packageName, packageManager.getApplicationLabel(info).toString(), packageInfo(info.packageName).versionName, info.sourceDir, packageManager.getLaunchIntentForPackage(info.packageName) != null) }
        .sortedBy { it.label.lowercase() }

    @Suppress("DEPRECATION")
    private fun installedApplications(): List<ApplicationInfo> = if (Build.VERSION.SDK_INT >= 33) packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0)) else packageManager.getInstalledApplications(0)

    @Suppress("DEPRECATION")
    private fun packageInfo(packageName: String) = if (Build.VERSION.SDK_INT >= 33) packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0)) else packageManager.getPackageInfo(packageName, 0)

    fun launch(packageName: String): Result<Unit> = runCatching { context.startActivity(packageManager.getLaunchIntentForPackage(packageName)?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ?: error("Application cannot be launched")) }

    fun detailsIntent(packageName: String): Intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(android.net.Uri.parse("package:$packageName"))
}

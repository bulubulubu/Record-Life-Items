package com.bulubulu.recordlifeitems.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class UpdateViewModel : ViewModel() {
    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    fun checkAndUpdate(context: Context) {
        if (_isChecking.value || _isDownloading.value) return

        viewModelScope.launch {
            _isChecking.value = true
            _statusMessage.value = null

            try {
                val result = withContext(Dispatchers.IO) { checkForUpdate() }
                _isChecking.value = false

                when (result) {
                    is UpdateResult.UpToDate -> {
                        _statusMessage.value = "already_latest"
                    }
                    is UpdateResult.UpdateAvailable -> {
                        _statusMessage.value = "update_found:${result.version}"
                        startDownload(context, result.downloadUrl)
                    }
                }
            } catch (e: Exception) {
                _isChecking.value = false
                _statusMessage.value = "error:${e.message}"
            }
        }
    }

    private fun startDownload(context: Context, url: String) {
        _isDownloading.value = true
        _downloadProgress.value = 0f

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = downloadApk(context, url) { progress ->
                    _downloadProgress.value = progress
                }
                _isDownloading.value = false
                _downloadProgress.value = 1f

                withContext(Dispatchers.Main) {
                    installApk(context, file)
                }
            } catch (e: Exception) {
                _isDownloading.value = false
                _downloadProgress.value = 0f
                _statusMessage.value = "error:${e.message}"
            }
        }
    }

    fun clearStatus() {
        _statusMessage.value = null
    }

    companion object {
        private const val GITHUB_API_URL = "https://api.github.com/repos/bulubulubu/Record-Life-Items/releases/latest"

        fun getCurrentVersion(context: Context): String {
            return try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionName ?: "unknown"
            } catch (e: Exception) { "unknown" }
        }

        private fun checkForUpdate(): UpdateResult {
            val url = URL(GITHUB_API_URL)
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            val response = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val json = JSONObject(response)
            val tagName = json.optString("tag_name", "")
            val latestVer = tagName.removePrefix("v")
            if (latestVer.isBlank()) return UpdateResult.UpToDate

            val currentVer = getCurrentVersion(android.app.Application())
            val currentParts = currentVer.split(".").map { it.toIntOrNull() ?: 0 }
            val latestParts = latestVer.split(".").map { it.toIntOrNull() ?: 0 }

            for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
                val c = currentParts.getOrElse(i) { 0 }
                val l = latestParts.getOrElse(i) { 0 }
                if (l > c) {
                    val assets = json.optJSONArray("assets")
                    var apkUrl = ""
                    if (assets != null) {
                        for (j in 0 until assets.length()) {
                            val asset = assets.getJSONObject(j)
                            if (asset.getString("name").endsWith(".apk")) {
                                apkUrl = asset.getString("browser_download_url")
                                break
                            }
                        }
                    }
                    return UpdateResult.UpdateAvailable(latestVer, apkUrl)
                }
                if (c > l) return UpdateResult.UpToDate
            }
            return UpdateResult.UpToDate
        }

        private fun downloadApk(context: Context, urlStr: String, onProgress: (Float) -> Unit): File {
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 30000
            conn.readTimeout = 30000
            conn.instanceFollowRedirects = true

            val totalSize = conn.contentLength.toLong()
            val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            val file = File(dir, "Record-Life-Items-latest.apk")

            conn.inputStream.use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (totalSize > 0) {
                            onProgress(totalRead.toFloat() / totalSize)
                        }
                    }
                }
            }
            conn.disconnect()
            return file
        }

        private fun installApk(context: Context, file: File) {
            val uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        }
    }
}

private sealed class UpdateResult {
    data object UpToDate : UpdateResult()
    data class UpdateAvailable(val version: String, val downloadUrl: String) : UpdateResult()
}

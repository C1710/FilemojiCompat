package de.c1710.filemojicompat_ui.helpers

import android.content.Context
import android.util.Log
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

private const val VERSION_CACHE = "de.c1710.filemojicompat.version_cache"

class VersionOnline
    @JvmOverloads constructor (
        context: Context,
        private val source: URL,
        regex: Regex = Regex("<version>\\s*(\\d+(\\.\\d+)*)\\s*</version>"),
        // Because Android Java/Kotlin, we cannot get groups by their name...
        groupId: Int = 1
    ) {
    val versionOnline: Future<Version>

    init {
        val client = getOrSetClient(context)

        versionOnline = executor.submit<Version?> {
            val request = Request.Builder()
                .url(source)
                .build()

            val call = client.newCall(request)

            val response = call.execute()

            fromStringOrNull(
                if (!response.isSuccessful) {
                    Log.e(
                        "FilemojiCompat",
                        "getVersionOnline: Could not get version from %s: %s".format(
                            source.toString(),
                            response.message
                        )
                    )
                    null
                } else {
                    val groups = regex.find(response.body?.string() ?: "")?.groups
                    groups?.get(groupId)?.value
                }
            )
        }
    }

    companion object {
        private var client: OkHttpClient? = null
        private val executor: ExecutorService by lazy {
            return@lazy Executors.newSingleThreadExecutor()
        }

        private fun getOrSetClient(context: Context): OkHttpClient {
            if (client == null) {
                client = OkHttpClient.Builder()
                    .cache(Cache(
                        directory = File(context.cacheDir, VERSION_CACHE),
                        maxSize = 5 * 0x100000L // 5 MiB
                    ))
                    .build()
            }

            return client!!
        }
    }
}
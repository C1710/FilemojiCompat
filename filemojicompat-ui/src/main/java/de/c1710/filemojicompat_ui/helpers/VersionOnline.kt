package de.c1710.filemojicompat_ui.helpers

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL

fun getVersionOnline(source: URL, regex: Regex = Regex("<version>\\s*(?<version>\\d+(\\.\\d+)*)\\s*</version")): Version? {
    // https://square.github.io/okhttp/recipes/#synchronous-get-kt-java
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(source)
        .build()

    client.newCall(request).execute().use {
        return fromStringOrNull(
            if (!it.isSuccessful) {
                Log.e(
                    "FilemojiCompat",
                    "getVersionOnline: Could not get version from %s: %s".format(
                        source.toString(),
                        it.message
                    )
                )
                null
            } else {
                val groups = regex.find(it.body?.string() ?: "")?.groups
                if (groups is MatchNamedGroupCollection?) {
                    groups?.get("version")?.value
                } else {
                    Log.e("FilemojiCompat", "getVersionOnline: Cannot use named group")
                    groups?.get(0)?.value
                }
            }
        )
    }
}

private fun fromStringOrNull(string: String?): Version? {
    return if (string != null) {
        Version.fromString(string)
    } else {
        null
    }
}
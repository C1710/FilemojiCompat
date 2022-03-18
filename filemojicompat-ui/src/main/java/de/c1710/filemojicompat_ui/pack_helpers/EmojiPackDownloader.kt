package de.c1710.filemojicompat_ui.pack_helpers

import android.util.Base64
import de.c1710.filemojicompat_ui.interfaces.EmojiPackDownloadListener
import de.c1710.filemojicompat_ui.packs.DownloadableEmojiPack
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okio.*
import java.io.File
import java.io.IOException

// Adapted from https://github.com/square/okhttp/blob/master/samples/guide/src/main/java/okhttp3/recipes/Progress.java;
// rewritten in Kotlin with minor changes and additions to work in this context

/**
 * Handles the download of a [DownloadableEmojiPack]
 */
internal class EmojiPackDownloader(
    pack: DownloadableEmojiPack,
    emojiStorage: File,
    private val isBase64: Boolean = "googlesource.com" in pack.source.toString(),
) {
    private val url = pack.source
    private val fileName = pack.getFileName(false)
    private val downloadLocation = File(emojiStorage, fileName)

    fun download(downloadListener: EmojiPackDownloadListener): Call {
        val client = OkHttpClient.Builder()
            .addNetworkInterceptor { chain: Interceptor.Chain ->
                val response = chain.proceed(chain.request())
                response
                    .newBuilder()
                    .body(response.body?.let {
                        ProgressResponseBody(it, downloadListener)
                    })
                    .build()
            }
            .build()

        val request = Request.Builder()
            .url(url.toHttpUrlOrNull()!!)
            .build()

        val call = client.newCall(request)
        call.enqueue(DownloadedCallback(downloadListener, downloadLocation, isBase64))
        return call
    }

    private class DownloadedCallback(
        val downloadListener: EmojiPackDownloadListener,
        val location: File,
        val isBase64: Boolean
    ) : Callback {
        override fun onFailure(call: Call, e: IOException) {
            downloadListener.onFailure(e)
        }

        override fun onResponse(call: Call, response: Response) {
            assert(response.isSuccessful)
            // https://stackoverflow.com/a/29012988/5070653
            val sink = location.sink(false).buffer()
            if (isBase64) {
                response.body?.source()?.let {
                    val data = it.readByteArray()
                    val decoded = Base64.decode(data, Base64.DEFAULT)
                    sink.write(decoded)
                }
            } else {
                response.body?.source()?.let { sink.writeAll(it) }
            }
            sink.close()
            downloadListener.onDone()
        }

    }

    private class ProgressResponseBody(
        val responseBody: ResponseBody,
        val downloadListener: EmojiPackDownloadListener?
    ) : ResponseBody() {
        var bufferedSource: BufferedSource = source(responseBody.source()).buffer()

        override fun contentLength(): Long = responseBody.contentLength()

        override fun contentType(): MediaType? = responseBody.contentType()

        override fun source(): BufferedSource = bufferedSource

        private fun source(source: Source): Source {
            return object : ForwardingSource(source) {
                var totalBytesRead: Long = 0

                override fun read(sink: Buffer, byteCount: Long): Long {
                    val bytesRead = super.read(sink, byteCount)
                    totalBytesRead += if (bytesRead != -1L) {
                        bytesRead
                    } else {
                        0
                    }

                    downloadListener?.onProgress(totalBytesRead, contentLength())

                    return bytesRead
                }
            }
        }

    }
}


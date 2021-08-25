package de.c1710.filemojicompat_ui.helpers

import de.c1710.filemojicompat_ui.packs.DownloadableEmojiPack
import de.c1710.filemojicompat_ui.structures.EmojiPackList
import okhttp3.*
import okio.*
import java.io.File
import java.io.IOException

// Adapted from https://github.com/square/okhttp/blob/master/samples/guide/src/main/java/okhttp3/recipes/Progress.java;
// rewritten in Kotlin with minor changes and additions to work in this context

class EmojiPackDownloader(
    pack: DownloadableEmojiPack,
    list: EmojiPackList
) {
    private val url = pack.source
    private val fileName = pack.createFileName()
    private val downloadLocation = File(list.emojiStorage, fileName)

    fun download(downloadCallback: DownloadCallback): Call? {
        val client = OkHttpClient.Builder()
            .addNetworkInterceptor {
                    chain: Interceptor.Chain ->
                val response = chain.proceed(chain.request())
                response
                    .newBuilder()
                    .body(response.body?.let {
                        ProgressResponseBody(it, downloadCallback)
                    })
                    .build()
            }
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        val call = client.newCall(request)
        call.enqueue(DownloadedCallback(downloadCallback, downloadLocation))
        return call
    }

    interface DownloadCallback {
        fun onProgress(bytesRead: Long, contentLength: Long)

        fun onFailure(e: IOException)

        fun onDone()
    }

    private class DownloadedCallback(
        val downloadCallback: DownloadCallback,
        val location: File) : Callback {
        override fun onFailure(call: Call, e: IOException) {
            downloadCallback.onFailure(e)
        }

        override fun onResponse(call: Call, response: Response) {
            assert(response.isSuccessful)
            // https://stackoverflow.com/a/29012988/5070653
            val sink = location.sink(false).buffer()
            response.body?.source()?.let { sink.writeAll(it) }
            sink.close()
            downloadCallback.onDone()
        }

    }

    private class ProgressResponseBody(
        val responseBody: ResponseBody,
        val downloadCallback: DownloadCallback?
    ): ResponseBody() {
        var bufferedSource: BufferedSource = source(responseBody.source()).buffer()

        override fun contentLength(): Long = responseBody.contentLength()

        override fun contentType(): MediaType? = responseBody.contentType()

        override fun source(): BufferedSource = bufferedSource

        private fun source(source: Source): Source {
            return object: ForwardingSource(source) {
                var totalBytesRead: Long = 0

                override fun read(sink: Buffer, byteCount: Long): Long {
                    val bytesRead = super.read(sink, byteCount)
                    totalBytesRead += if (bytesRead != -1L) {
                        bytesRead
                    } else {
                        0
                    }

                    downloadCallback?.onProgress(totalBytesRead, contentLength())

                    return bytesRead
                }
            }
        }

    }
}
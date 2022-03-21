package de.c1710.filemojicompat_ui.pack_helpers

import android.util.Base64
import de.c1710.filemojicompat_ui.interfaces.EmojiPackDownloadListener
import de.c1710.filemojicompat_ui.packs.DownloadableEmojiPack
import okhttp3.*
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
    private var callback: DownloadedCallback? = null

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
            .url(url.toString())
            .build()

        val call = client.newCall(request)
        callback = DownloadedCallback(downloadListener, downloadLocation, isBase64)
        call.enqueue(callback!!)
        return call
    }

    private class DownloadedCallback(
        val downloadListener: EmojiPackDownloadListener,
        val location: File,
        val isBase64: Boolean
    ) : Callback {
        var response: Response? = null

        override fun onFailure(call: Call, e: IOException) {
            downloadListener.onFailure(e)
        }

        override fun onResponse(call: Call, response: Response) {
            response.use { response ->
                this.response = response
                if (response.isSuccessful) {
                    // https://stackoverflow.com/a/29012988/5070653
                    val sink = location.sink(false).buffer()
                    try {
                        response.body?.source()?.let {
                            if (isBase64) {
                                sink.writeAll(Base64DecodingSource(it).buffer())
                            }
                            sink.writeAll(it)
                        }
                    } catch (e: IOException) {
                        sink.close()
                        downloadListener.onFailure(e)
                    } finally {
                        sink.close()
                    }
                    downloadListener.onDone()
                } else {
                    downloadListener.onFailure(IOException(response.code.toString()))
                }
            }
        }

        fun cancel() {
            response?.close()
        }
    }

    fun cancel() {
        callback?.cancel()
    }

    private class Base64DecodingSource(val source: BufferedSource): Source {
        val buffer = Buffer()

        override fun close() {
            source.close()
        }

        override fun read(sink: Buffer, byteCount: Long): Long {
            // We always need to read multiples of 4 bytes
            val bytesRead = source.read(buffer, byteCount)
            val bytesProcessed = (buffer.size - buffer.size % 4).toInt()
            val decoded = Base64.decode(buffer.readByteArray(bytesProcessed.toLong()), 0, bytesProcessed, Base64.DEFAULT)
            sink.write(decoded)
            return bytesRead
        }

        override fun timeout(): Timeout {
            return source.timeout()
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


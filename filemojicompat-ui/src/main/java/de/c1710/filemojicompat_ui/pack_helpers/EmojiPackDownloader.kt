package de.c1710.filemojicompat_ui.pack_helpers

import android.util.Base64
import android.util.Log
import de.c1710.filemojicompat_ui.interfaces.EmojiPackDownloadListener
import de.c1710.filemojicompat_ui.packs.DownloadableEmojiPack
import okhttp3.*
import okio.*
import okio.ByteString.Companion.decodeHex
import java.io.EOFException
import java.io.File
import java.io.IOException
import java.io.StreamCorruptedException

// Adapted from https://github.com/square/okhttp/blob/master/samples/guide/src/main/java/okhttp3/recipes/Progress.java;
// rewritten in Kotlin with minor changes and additions to work in this context

/**
 * Handles the download of a [DownloadableEmojiPack]
 */
internal class EmojiPackDownloader(
    pack: DownloadableEmojiPack,
    emojiStorage: File,
    private val isBase64: Boolean = "googlesource.com" in pack.source.toString()
) {
    private val url = pack.source
    private val fileName = pack.getFileName(false)
    private val downloadLocation = File(emojiStorage, fileName)
    private val expectedHash = pack.hash?.decodeHex()

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
        val callback = DownloadedCallback(downloadListener, downloadLocation, isBase64, expectedHash)
        call.enqueue(callback)
        return call
    }

    private class DownloadedCallback(
        val downloadListener: EmojiPackDownloadListener,
        val location: File,
        val isBase64: Boolean,
        val expectedHash: ByteString? = null
    ) : Callback {
        override fun onFailure(call: Call, e: IOException) {
            downloadListener.onFailure(e)
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (it.isSuccessful) {
                    // https://stackoverflow.com/a/29012988/5070653
                    val sink = location.sink(false).buffer()
                    try {
                        it.body?.source()?.let {
                            val hashingSource = HashingSource.sha256(it)
                            if (isBase64) {
                                sink.writeAll(Base64Source(hashingSource).buffer())
                            } else {
                                sink.writeAll(hashingSource)
                            }
                            val actualHash = hashingSource.hash
                            Log.d("EmojiPackDownloader",
                                "SHA-256 of downloaded version of %s: %s".format(location, actualHash))
                            if (expectedHash != null) {
                                if (expectedHash != actualHash) {
                                    val e = StreamCorruptedException("Invalid hash for file %s: Expected %s, got %s".format(location, expectedHash, actualHash))
                                    Log.e("EmojiPackDownloader", "Invalid hash: Expected %s, got %s".format(expectedHash, actualHash))
                                    downloadListener.onFailure(e)
                                } else {
                                    // Everything is alright
                                }
                            } else {
                                Log.d("EmojiPackDownloader", "No hash to check against.")
                                // Everything is alright
                            }
                        }
                    } catch (e: IOException) {
                        sink.close()
                        downloadListener.onFailure(e)
                    } finally {
                        sink.close()
                    }
                    downloadListener.onDone()
                } else {
                    downloadListener.onFailure(IOException(it.code.toString()))
                }
            }
        }
    }

    private class CancelableSink(delegate: Sink) : ForwardingSink(delegate) {
        var cancelled = false

        override fun write(source: Buffer, byteCount: Long) {
            if (!cancelled) {
                super.write(source, byteCount)
            } else {
                close()
                throw EOFException("Write cancelled")
            }
        }

        fun cancel() {
            cancelled = true
        }
    }

    /**
     * Wrapper class that decodes a base64-encoded source
     */
    private class Base64Source(delegate: Source): ForwardingSource(delegate) {
        val buffer = Buffer()

        override fun read(sink: Buffer, byteCount: Long): Long {
            // We always need to read multiples of 4 bytes
            val bytesRead = delegate.read(buffer, byteCount)
            val bytesProcessed = (buffer.size - buffer.size % 4).toInt()
            val decoded = Base64.decode(buffer.readByteArray(bytesProcessed.toLong()), 0, bytesProcessed, Base64.DEFAULT)
            sink.write(decoded)
            return bytesRead
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


package com.jegly.rss.network

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.IOException
import okio.buffer

/**
 * Caps the response body at [maxBytes]. Beyond that the source throws and the read aborts,
 * preventing a hostile feed (multi-GB XML, slowloris-style trickle) from OOM-ing the parser.
 *
 * Content-Length is checked up front when present, but we still wrap the source because servers
 * can lie about it (or send chunked transfer with no length).
 */
class ResponseSizeInterceptor(private val maxBytes: Long = DEFAULT_MAX_BYTES) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val body = response.body ?: return response

        val advertisedLength = body.contentLength()
        if (advertisedLength in 1..Long.MAX_VALUE && advertisedLength > maxBytes) {
            body.close()
            throw IOException("Response body too large: $advertisedLength > $maxBytes bytes")
        }

        val limited = LimitedSource(body.source(), maxBytes).buffer()
        val wrappedBody = limited.asResponseBody(body.contentType(), advertisedLength)
        return response.newBuilder().body(wrappedBody).build()
    }

    private class LimitedSource(delegate: BufferedSource, private val maxBytes: Long) :
        ForwardingSource(delegate) {
        private var bytesRead: Long = 0

        override fun read(sink: Buffer, byteCount: Long): Long {
            val n = super.read(sink, byteCount)
            if (n == -1L) return -1L
            bytesRead += n
            if (bytesRead > maxBytes) {
                throw IOException("Response body exceeded $maxBytes bytes")
            }
            return n
        }
    }

    companion object {
        const val DEFAULT_MAX_BYTES: Long = 5L * 1024 * 1024  // 5 MB — generous for any sane RSS feed
    }
}

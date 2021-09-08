package com.github.rey5137.robotrunnerplugin.http

import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.Map.Entry

internal object Protocol {

    private val FLAG = byteArrayOf('p'.code.toByte(), 'b'.code.toByte())

    private fun read(stream: InputStream): Entry<StatusCode, JSONObject> {
        // 1. FLAG
        var b: Int = stream.read()
        if (b == -1 || b != FLAG[0].toInt()) throw ProtocolException(ProtocolException.UNRECOGNIZED_PROTOCOL)
        b = stream.read()
        if (b == -1 || b != FLAG[1].toInt()) throw ProtocolException(ProtocolException.UNRECOGNIZED_PROTOCOL)

        // 2. VERSION
        b = stream.read()
        if (b == -1) throw ProtocolException(ProtocolException.INCOMPLETE_DATA)
        if (b != Versions.CURRENT[0].toInt()) throw ProtocolException(ProtocolException.INCOMPATIBLE_VERSION)
        b = stream.read()
        if (b == -1) throw ProtocolException(ProtocolException.INCOMPLETE_DATA)
        if (b != Versions.CURRENT[1].toInt()) throw ProtocolException(ProtocolException.INCOMPATIBLE_VERSION)

        // 3. STATUS CODE
        b = stream.read()
        if (b == -1) throw ProtocolException(ProtocolException.INCOMPLETE_DATA)
        val statusCode = StatusCode.fromRawValue(b)
            ?: throw ProtocolException(ProtocolException.INVALID_STATUS_CODE)

        // 4. RESERVED BYTES (2 bytes)
        b = stream.read()
        if (b == -1) throw ProtocolException(ProtocolException.INCOMPLETE_DATA)
        b = stream.read()
        if (b == -1) throw ProtocolException(ProtocolException.INCOMPLETE_DATA)

        // 5. LENGTH (little endian)
        var bodyLen: Int
        b = stream.read()
        if (b == -1) throw ProtocolException(ProtocolException.INCOMPLETE_DATA)
        bodyLen = b
        b = stream.read()
        if (b == -1) throw ProtocolException(ProtocolException.INCOMPLETE_DATA)
        bodyLen = bodyLen or (b shl 8)
        b = stream.read()
        if (b == -1) throw ProtocolException(ProtocolException.INCOMPLETE_DATA)
        bodyLen = bodyLen or (b shl 16)
        b = stream.read()
        if (b == -1) throw ProtocolException(ProtocolException.INCOMPLETE_DATA)
        bodyLen = bodyLen or (b shl 24)

        // 6. JSON OBJECT
        val buffer = ByteArrayOutputStream()
        var readCount: Int
        var restCount = bodyLen
        var buf = ByteArray(Math.min(bodyLen, 1024 * 1024))
        while (stream.read(buf, 0, Math.min(buf.size, restCount)).also { readCount = it } != -1) {
            buffer.write(buf, 0, readCount)
            restCount -= readCount
            if (restCount == 0) {
                break
            }
        }
        if (buffer.size() != bodyLen) {
            throw ProtocolException(ProtocolException.INCOMPLETE_DATA)
        }
        buffer.flush()
        buf = buffer.toByteArray()
        return try {
            val jsonText = String(buf, StandardCharsets.UTF_8)
            val body = JSONObject(jsonText)
            AbstractMap.SimpleEntry(statusCode, body)
        } catch (ex: Exception) {
            throw ProtocolException(ProtocolException.INVALID_BODY)
        }
    }

    @Throws(IOException::class)
    private fun write(stream: OutputStream, statusCode: StatusCode, body: JSONObject) {
        // 1. FLAG 'p', 'b'
        stream.write(FLAG)

        // 2. VERSION
        stream.write(Versions.CURRENT)

        // 3. STATUS CODE
        stream.write(statusCode.rawValue)

        // 4. RESERVED BYTES (2 bytes)
        stream.write(0)
        stream.write(0)

        // make json object
        val buf = body.toString().toByteArray(StandardCharsets.UTF_8)

        // 5. LENGTH (4-byte, little endian)
        val len = buf.size
        val b0 = len and 0xff
        val b1 = len and 0xff00 shr 8
        val b2 = len and 0xff0000 shr 16
        val b3 = len and -0x1000000 shr 24
        stream.write(b0)
        stream.write(b1)
        stream.write(b2)
        stream.write(b3)

        // 6. JSON OBJECT
        stream.write(buf)
        stream.flush()
    }

    @JvmStatic
    @Throws(IOException::class, ProtocolException::class)
    fun readRequest(stream: InputStream): Entry<String, JSONObject> {
        val entry: Entry<StatusCode, JSONObject> = read(stream)
        val statusCode: StatusCode = entry.key
        val body: JSONObject = entry.value
        if (statusCode != StatusCode.REQUEST) {
            throw ProtocolException(ProtocolException.INVALID_STATUS_CODE)
        }
        val method = body.optString(Keys.METHOD)
        val payload = body.optJSONObject(Keys.PAYLOAD)
        return AbstractMap.SimpleEntry(method, payload)
    }

    fun readResponse(stream: InputStream): Entry<StatusCode, Any> {
        val entry: Entry<StatusCode, JSONObject> = read(stream)
        val statusCode: StatusCode = entry.key
        val body: JSONObject = entry.value
        return if (statusCode == StatusCode.GOOD_RESPONSE) {
            AbstractMap.SimpleEntry(
                StatusCode.GOOD_RESPONSE,
                body.opt(Keys.PAYLOAD)
            )
        } else if (statusCode == StatusCode.BAD_RESPONSE) {
            AbstractMap.SimpleEntry<StatusCode, Any>(
                StatusCode.BAD_RESPONSE,
                body.optString(Keys.MESSAGE)
            )
        } else {
            throw ProtocolException(ProtocolException.INVALID_STATUS_CODE)
        }
    }

    fun writeRequest(stream: OutputStream, method: String?, payload: Any?) {
        val body = JSONObject()
        if (method != null) {
            body.put(Keys.METHOD, method)
        }
        if (payload != null) {
            body.put(Keys.PAYLOAD, payload)
        }
        write(stream, StatusCode.REQUEST, body)
    }

    @JvmStatic
    fun writeGoodResponse(stream: OutputStream, payload: Any?) {
        val body = JSONObject()
        if (payload != null) {
            body.put(Keys.PAYLOAD, payload)
        }
        write(stream, StatusCode.GOOD_RESPONSE, body)
    }

    @JvmStatic
    fun writeBadResponse(stream: OutputStream, message: String?) {
        val body = JSONObject()
        if (message != null) {
            body.put(Keys.MESSAGE, message)
        }
        write(stream, StatusCode.BAD_RESPONSE, body)
    }
}
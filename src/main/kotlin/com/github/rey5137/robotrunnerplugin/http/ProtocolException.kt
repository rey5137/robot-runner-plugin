package com.github.rey5137.robotrunnerplugin.http

class ProtocolException(message: String?) : RuntimeException(message) {
    companion object {
        const val UNRECOGNIZED_PROTOCOL = "unrecognized protocol"
        const val INCOMPATIBLE_VERSION = "incompatible protocol version"
        const val INCOMPLETE_DATA = "incomplete data"
        const val INVALID_STATUS_CODE = "invalid status code"
        const val INVALID_BODY = "invalid body"
    }
}
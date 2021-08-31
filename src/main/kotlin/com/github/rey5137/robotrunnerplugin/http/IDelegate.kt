package com.github.rey5137.robotrunnerplugin.http

import org.json.JSONObject

fun interface IDelegate {
    /**
     * An interface that defines how server handles requests.
     *
     * @param method the requested method
     * @param payload the requested payload, must be a JSON value
     * @return the result, must be a JSON value
     */
    fun handleRequest(method: String?, payload: JSONObject?): JSONObject?
}
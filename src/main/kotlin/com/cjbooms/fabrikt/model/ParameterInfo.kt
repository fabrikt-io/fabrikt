package com.cjbooms.fabrikt.model

sealed class RequestParameterLocation {
    companion object {
        operator fun invoke(location: String): RequestParameterLocation =
            when (location) {
                "query" -> QueryParam
                "header" -> HeaderParam
                "path" -> PathParam
                "cookie" -> CookieParam
                else -> throw IllegalStateException("Invalid request parameter location: $location")
            }
    }
}

object QueryParam : RequestParameterLocation() {
    override fun toString() = "query"
}

object HeaderParam : RequestParameterLocation() {
    override fun toString() = "header"
}

object PathParam : RequestParameterLocation() {
    override fun toString() = "path"
}

object CookieParam : RequestParameterLocation() {
    override fun toString() = "cookie"
}

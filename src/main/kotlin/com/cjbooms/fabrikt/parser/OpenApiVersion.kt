package com.cjbooms.fabrikt.parser

internal data class OpenApiVersion(
    val value: String,
    val major: Int,
    val minor: Int,
    val patch: Int?,
) {
    fun isAtLeast(
        major: Int,
        minor: Int,
    ): Boolean = this.major > major || this.major == major && this.minor >= minor

    companion object {
        private val versionPattern = Regex("""^(\d+)\.(\d+)(?:\.(\d+))?(?:[-+].*)?$""")

        fun parse(value: String?): OpenApiVersion? {
            val match = value?.let(versionPattern::matchEntire) ?: return null
            return OpenApiVersion(
                value = value,
                major = match.groupValues[1].toInt(),
                minor = match.groupValues[2].toInt(),
                patch = match.groupValues[3].takeIf(String::isNotEmpty)?.toInt(),
            )
        }
    }
}

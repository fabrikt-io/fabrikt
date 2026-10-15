package examples.multipartDynamicClientParts.client

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class FeignBinaryParts(
    val parameter: String,
    val patterns: Array<String>,
    val fixedNames: Array<String>,
)

internal fun expandBinaryParts(parts: Map<*, *>, declaration: FeignBinaryParts?): Map<*, *> {
    if (declaration == null) return parts
    val dynamic = parts[declaration.parameter] ?: emptyMap<String, FeignMultipartFile>()
    require(dynamic is Map<*, *>) { "Expected a map of dynamic multipart files" }
    val expanded = parts.filterKeys { it != declaration.parameter }.toMutableMap()
    dynamic.forEach { (name, file) ->
        require(name is String && name.isNotEmpty() && name.all {
            it in "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789._-"
        }) { "Dynamic multipart names must contain only ASCII letters, digits, '.', '_' or '-'" }
        require(name !in declaration.fixedNames && name !in expanded) { "Dynamic multipart name conflicts with a fixed property" }
        require(declaration.patterns.any { Regex(it).containsMatchIn(name) }) { "Dynamic multipart name does not match patternProperties" }
        require(file is FeignMultipartFile) { "Expected binary multipart files" }
        expanded[name] = file
    }
    return expanded
}

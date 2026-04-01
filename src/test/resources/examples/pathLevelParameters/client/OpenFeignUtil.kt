package examples.pathLevelParameters.client

import feign.Param

class CsvParamExpander : Param.Expander {
    override fun expand(value: Any): String = when (value) {
        is Collection<*> -> value.joinToString(",")
        else -> value.toString()
    }
}

package examples.normalizedNameConflation.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@JsonClassDiscriminator("${'$'}type")
@ExperimentalSerializationApi
@Serializable
public sealed class Grandparent()

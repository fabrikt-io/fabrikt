package examples.discriminatedOneOf.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("status")
@ExperimentalSerializationApi
public sealed interface StateB : State

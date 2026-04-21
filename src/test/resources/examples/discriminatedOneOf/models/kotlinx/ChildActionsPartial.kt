package examples.discriminatedOneOf.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("actionType")
@ExperimentalSerializationApi
public sealed interface ChildActionsPartial

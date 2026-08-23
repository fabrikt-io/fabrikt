package examples.discriminatorMappingSuffix.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@JsonClassDiscriminator("actionType")
@ExperimentalSerializationApi
@Serializable
public sealed class ParentAction()

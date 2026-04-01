package examples.discriminatedOneOf.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("content_block_location")
@Serializable
public object TwoObject : SomeObjInlinedArray, SomeObjInlinedObject, SomeObjInlinedObjectNoMappings

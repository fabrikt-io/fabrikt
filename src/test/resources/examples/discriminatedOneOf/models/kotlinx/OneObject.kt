package examples.discriminatedOneOf.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("char_location")
@Serializable
public object OneObject : SomeObjInlinedArray, SomeObjInlinedObject, SomeObjInlinedObjectNoMappings

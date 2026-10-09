package examples.sharedCompositionExternal.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes(JsonSubTypes.Type(value = B::class),JsonSubTypes.Type(value =
    C::class),JsonSubTypes.Type(value = E::class))
public sealed interface Mixed

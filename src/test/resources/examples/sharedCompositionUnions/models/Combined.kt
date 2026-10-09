package examples.sharedCompositionUnions.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes(JsonSubTypes.Type(value = CombinedX::class),JsonSubTypes.Type(value =
    CombinedY::class))
public sealed interface Combined : AComposite

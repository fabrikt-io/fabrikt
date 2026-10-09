package examples.sharedCompositionRequestProjection.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes(JsonSubTypes.Type(value = B::class),JsonSubTypes.Type(value = C::class))
public sealed interface Choice : AComposite

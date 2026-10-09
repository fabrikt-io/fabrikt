package examples.composedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes(JsonSubTypes.Type(value = ComposedDeducedCombinedComposedDeducedA::class),JsonSubTypes.Type(value = ComposedDeducedCombinedComposedDeducedB::class))
public sealed interface ComposedDeducedCombined

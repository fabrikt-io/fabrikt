package examples.namedOneOfDeduction.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes(JsonSubTypes.Type(value = DeductionA::class),JsonSubTypes.Type(value = DeductionB::class))
public sealed interface DeductionChoice

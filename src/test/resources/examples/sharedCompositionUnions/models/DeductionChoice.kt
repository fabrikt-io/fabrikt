package examples.sharedCompositionUnions.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes(JsonSubTypes.Type(value = DeductionB::class),JsonSubTypes.Type(value =
    DeductionC::class))
public sealed interface DeductionChoice : AComposite

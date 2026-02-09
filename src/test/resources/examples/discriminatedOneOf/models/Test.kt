package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "type",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = XTest::class, name = "X"),JsonSubTypes.Type(value =
    YTest1::class, name = "Y1"),JsonSubTypes.Type(value = YTest2::class, name = "Y2"))
public sealed interface Test

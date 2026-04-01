package examples.nestedPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.Boolean
import kotlin.Int
import kotlin.String

public data class ThirdLevelChild12(
  @param:JsonProperty("rootField1")
  @get:JsonProperty("rootField1")
  @get:NotNull
  override val rootField1: String,
  @param:JsonProperty("rootField2")
  @get:JsonProperty("rootField2")
  override val rootField2: Boolean? = null,
  @param:JsonProperty("firstLevelField1")
  @get:JsonProperty("firstLevelField1")
  @get:NotNull
  override val firstLevelField1: String,
  @param:JsonProperty("firstLevelField2")
  @get:JsonProperty("firstLevelField2")
  override val firstLevelField2: Int? = null,
  @param:JsonProperty("metadata")
  @get:JsonProperty("metadata")
  @get:NotNull
  @get:Valid
  override val metadata: SecondLevelMetadata,
  @param:JsonProperty(
    "isDeleted",
    required = true,
  )
  @get:JsonProperty("isDeleted")
  @get:NotNull
  public val isDeleted: Boolean,
  @get:JsonProperty("secondLevelDiscriminator")
  @get:NotNull
  @param:JsonProperty("secondLevelDiscriminator")
  override val secondLevelDiscriminator: SecondLevelDiscriminator =
      SecondLevelDiscriminator.THIRD_LEVEL_CHILD2,
) : SecondLevelChild1(rootField1, rootField2, firstLevelField1, firstLevelField2, metadata)

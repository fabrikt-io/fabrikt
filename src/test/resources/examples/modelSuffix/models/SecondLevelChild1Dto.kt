package examples.modelSuffix.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.Boolean
import kotlin.Int
import kotlin.String

public data class SecondLevelChild1Dto(
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
  public val metadata: SecondLevelMetadataDto,
  @get:JsonProperty("firstLevelDiscriminator")
  @get:NotNull
  @param:JsonProperty("firstLevelDiscriminator")
  override val firstLevelDiscriminator: FirstLevelDiscriminatorDto =
      FirstLevelDiscriminatorDto.SECOND_LEVEL_CHILD1,
) : FirstLevelChildDto(rootField1, rootField2, firstLevelField1, firstLevelField2)

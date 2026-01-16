package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.constraints.NotNull
import kotlin.String

public data class OutfitItem(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  public val id: String,
  @param:JsonProperty("item_type")
  @get:JsonProperty("item_type")
  @get:NotNull
  public val itemType: String,
  @param:JsonProperty("outfit_id")
  @get:JsonProperty("outfit_id")
  @get:NotNull
  public val outfitId: String,
)

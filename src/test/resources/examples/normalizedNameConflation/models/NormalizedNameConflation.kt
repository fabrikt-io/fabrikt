package examples.normalizedNameConflation.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class NormalizedNameConflation(
  /**
   * Description of lowercase x
   */
  @param:JsonProperty("x")
  @get:JsonProperty("x")
  public val x: String? = null,
  /**
   * Description of uppercase X
   */
  @param:JsonProperty("X")
  @get:JsonProperty("X")
  public val X: String? = null,
  /**
   * Description of camel case fooBar
   */
  @param:JsonProperty("fooBar")
  @get:JsonProperty("fooBar")
  public val fooBar: String? = null,
  /**
   * Description of snake case foo_bar
   */
  @param:JsonProperty("foo_bar")
  @get:JsonProperty("foo_bar")
  public val foo_bar: String? = null,
  /**
   * Description of aBC
   */
  @param:JsonProperty("aBC")
  @get:JsonProperty("aBC")
  public val aBC: String? = null,
  /**
   * Description of ABC
   */
  @param:JsonProperty("ABC")
  @get:JsonProperty("ABC")
  public val ABC: String? = null,
  /**
   * Description of a_b_c
   */
  @param:JsonProperty("a_b_c")
  @get:JsonProperty("a_b_c")
  public val a_b_c: String? = null,
  /**
   * Description of control_case - this should get normalized because it conflicts with no other
   * property
   */
  @param:JsonProperty("control_case")
  @get:JsonProperty("control_case")
  public val controlCase: String? = null,
)

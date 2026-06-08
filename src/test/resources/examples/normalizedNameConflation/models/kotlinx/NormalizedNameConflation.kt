package examples.normalizedNameConflation.models

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class NormalizedNameConflation(
  /**
   * Description of lowercase x
   */
  @SerialName("x")
  public val x: String? = null,
  /**
   * Description of uppercase X
   */
  @SerialName("X")
  public val X: String? = null,
  /**
   * Description of camel case fooBar
   */
  @SerialName("fooBar")
  public val fooBar: String? = null,
  /**
   * Description of snake case foo_bar
   */
  @SerialName("foo_bar")
  public val foo_bar: String? = null,
  /**
   * Description of aBC
   */
  @SerialName("aBC")
  public val aBC: String? = null,
  /**
   * Description of ABC
   */
  @SerialName("ABC")
  public val ABC: String? = null,
  /**
   * Description of a_b_c
   */
  @SerialName("a_b_c")
  public val a_b_c: String? = null,
  /**
   * Description of control_case - this should get normalized because it conflicts with no other
   * property
   */
  @SerialName("control_case")
  public val controlCase: String? = null,
)

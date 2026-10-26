package com.cjbooms.fabrikt.util

import com.cjbooms.fabrikt.model.OpenApi3Document
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import com.cjbooms.fabrikt.util.SchemaParserExtensions.isOneOfSuperInterface
import com.cjbooms.fabrikt.util.SchemaParserExtensions.isOneOfSuperInterfaceWithDiscriminator
import com.cjbooms.fabrikt.util.SchemaParserExtensions.isOneOfWhereAllTypesInheritFromACommonAllOfSuperType
import com.cjbooms.fabrikt.util.SchemaParserExtensions.safeName
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DiscriminatedHierarchyUnionTest {
    private fun document(change: (ObjectNode) -> Unit = {}): OpenApi3Document {
        val root = YamlObjectMapper.instance.readTree(readTextResource("/examples/redundantHierarchyUnion/api.yaml")) as ObjectNode
        change(root.path("components").path("schemas") as ObjectNode)
        return OpenApi3Document(YamlUtils.parseOpenApi(YamlObjectMapper.instance.writeValueAsString(root)))
    }

    @Test
    fun `a base alternative is recognized by identity`() {
        val union = document().schemas.getValue("NamedUnion")

        assertThat(union.isOneOfWhereAllTypesInheritFromACommonAllOfSuperType()).isTrue()
        assertThat(union.isOneOfSuperInterface()).isFalse()
        assertThat(union.safeName()).isEqualTo("Base")
    }

    @Test
    fun `a common base is found through an indirect first alternative`() {
        val union =
            document { schemas ->
                val alternatives = schemas.path("NamedUnion").path("oneOf") as ArrayNode
                alternatives.remove(0)
                alternatives.add(alternatives.remove(0))
            }.schemas.getValue("NamedUnion")

        assertThat(union.isOneOfWhereAllTypesInheritFromACommonAllOfSuperType()).isTrue()
        assertThat(union.isOneOfSuperInterface()).isFalse()
        assertThat(union.safeName()).isEqualTo("Base")
    }

    @Test
    fun `an unrelated schema with identical fields does not join the hierarchy`() {
        val union =
            document { schemas ->
                schemas.set<ObjectNode>("Other", schemas.path("Base").deepCopy<ObjectNode>())
                (schemas.path("NamedUnion").path("oneOf") as ArrayNode).addObject().put("${'$'}ref", "#/components/schemas/Other")
            }.schemas.getValue("NamedUnion")

        assertThat(union.isOneOfWhereAllTypesInheritFromACommonAllOfSuperType()).isFalse()
        assertThat(union.isOneOfSuperInterface()).isTrue()
        assertThat(union.safeName()).isEqualTo("NamedUnion")
    }

    @Test
    fun `an explicit union discriminator retains its own interface`() {
        val union =
            document { schemas ->
                (schemas.path("NamedUnion") as ObjectNode).set<ObjectNode>(
                    "discriminator",
                    schemas.path("Base").path("discriminator").deepCopy<ObjectNode>(),
                )
            }.schemas.getValue("NamedUnion")

        assertThat(union.isOneOfSuperInterfaceWithDiscriminator()).isTrue()
        assertThat(union.safeName()).isEqualTo("NamedUnion")
    }

    @Test
    fun `a base without a discriminator does not collapse a union`() {
        val union = document { (it.path("Base") as ObjectNode).remove("discriminator") }.schemas.getValue("NamedUnion")

        assertThat(union.isOneOfWhereAllTypesInheritFromACommonAllOfSuperType()).isFalse()
        assertThat(union.isOneOfSuperInterface()).isTrue()
    }
}

package com.cjbooms.fabrikt.models.kotlinx

import com.example.models.InlinePhoneEnvelope
import com.example.models.LandlinePhone
import com.example.models.Module
import com.example.models.ModuleA
import com.example.models.Phone
import com.example.models.State
import com.example.models.StateA
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class KotlinxSerializationOneOfPolymorphicTest {

    @Test
    fun `must deserialize an inline union nested inside another inline object`() {
        val payload = """{"details":{"phone":{"type":"landline","number":"123","area_code":"456"}}}"""
        val result = kotlinx.serialization.json.Json.decodeFromString<InlinePhoneEnvelope>(payload)

        assertThat(result.details!!.phone).isEqualTo(LandlinePhone(number = "123", areaCode = "456"))
        assertThat(kotlinx.serialization.json.Json.decodeFromString<InlinePhoneEnvelope>(kotlinx.serialization.json.Json.encodeToString(result))).isEqualTo(result)
    }

    @Nested
    inner class DefaultClassDiscriminator {

        /**
         * "Phone" class hierarchy uses the default polymorphic discriminator "type"
         */

        @Test
        fun `must serialize Phone with type info`() {
            val phone: Phone = LandlinePhone(number = "1234567890", areaCode = "123")
            val json = kotlinx.serialization.json.Json.encodeToString(phone)

            // Note that "type" is added because we are serializing a subtype of Phone
            // (See https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md#sealed-classes)
            assertThat(json).isEqualTo("""
            {"type":"landline","number":"1234567890","area_code":"123"}
        """.trimIndent())
        }

        @Test
        fun `must serialize LandlinePhone without type info`() {
            val phone: LandlinePhone = LandlinePhone(number = "1234567890", areaCode = "123")
            val json = kotlinx.serialization.json.Json.encodeToString(phone)

            // Note that "type" is not added because we are serializing the specific class LandlinePhone
            // (See https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md#sealed-classes)
            assertThat(json).isEqualTo("""
            {"number":"1234567890","area_code":"123"}
        """.trimIndent())
        }

        @Test
        fun `must deserialize Phone into LandlinePhone`() {
            val json = """
            {"type":"landline","number":"1234567890","area_code":"123"}
        """.trimIndent()
            val phone: Phone = kotlinx.serialization.json.Json.decodeFromString(json)
            assertThat(phone).isEqualTo(LandlinePhone(number = "1234567890", areaCode = "123"))
        }

        @Test
        fun `must deserialize LandlinePhone specific class`() {
            val json = """
            {"number":"1234567890","area_code":"123"}
        """.trimIndent()
            val phone: LandlinePhone = kotlinx.serialization.json.Json.decodeFromString(json)
            assertThat(phone).isEqualTo(LandlinePhone(number = "1234567890", areaCode = "123"))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Nested
    inner class ExplicitClassDiscriminator {

        /**
         * "Module" class hierarchy uses "moduleType" as discriminator
         */

        @Test
        fun `must serialize Module with type info`() {
            val module: Module = ModuleA
            val json = kotlinx.serialization.json.Json.encodeToString(module)

            // Note that "moduleType" is added because we are serializing a subtype of Module
            // (See https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md#sealed-classes)
            assertThat(json).isEqualTo(
                """
                {"moduleType":"a"}
            """.trimIndent()
            )
        }

        @Test
        fun `must serialize ModuleA without type info`() {
            val module: ModuleA = ModuleA
            val json = kotlinx.serialization.json.Json.encodeToString(module)

            // Note that "moduleType" is not added because we are serializing the specific class ModuleA
            // (See https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md#sealed-classes)
            assertThat(json).isEqualTo(
                """
                {}
            """.trimIndent()
            )
        }

        @Test
        fun `must deserialize Module into ModuleA`() {
            val json = """
                {"moduleType":"a"}
            """.trimIndent()
            val module: Module = kotlinx.serialization.json.Json.decodeFromString(json)
            assertThat(module).isEqualTo(ModuleA)
        }

        @Test
        fun `must deserialize ModuleA specific class`() {
            val json = """
                {}
            """.trimIndent()
            val module: ModuleA = kotlinx.serialization.json.Json.decodeFromString(json)
            assertThat(module).isEqualTo(ModuleA)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Nested
    inner class EnumDiscriminator {

        /**
         * "State" class hierarchy uses enum "status" as discriminator
         */

        @Test
        fun `must serialize State with type info`() {
            val module: State = StateA
            val json = kotlinx.serialization.json.Json.encodeToString(module)

            // Note that "moduleType" is added because we are serializing a subtype of Module
            // (See https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md#sealed-classes)
            assertThat(json).isEqualTo(
                """
                {"status":"a"}
            """.trimIndent()
            )
        }

        @Test
        fun `must deserialize State into StateA`() {
            val json = """
                {"status":"a"}
            """.trimIndent()
            val state: State = kotlinx.serialization.json.Json.decodeFromString(json)
            assertThat(state).isEqualTo(StateA)
        }
    }
}

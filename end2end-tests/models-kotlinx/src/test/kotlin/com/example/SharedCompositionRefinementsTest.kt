package com.example

import com.example.compositionrefinements.models.Cat
import com.example.compositionrefinements.models.CatProfile
import com.example.compositionrefinements.models.Dog
import com.example.compositionrefinements.models.Pet
import com.example.compositionrefinements.models.PetComposite
import com.example.compositionrefinements.models.ProfileComposite
import com.example.compositionrequestprojection.models.AComposite as RequestContract
import com.example.compositionrequestprojection.models.B as RequestB
import com.example.compositionresponseprojection.models.AComposite as ResponseContract
import com.example.compositionresponseprojection.models.B as ResponseB
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SharedCompositionRefinementsTest {
    private val catJson = """{"id":"cat-1","name":"Cat"}"""
    private val profileJson = """{"pet":$catJson,"pets":[$catJson],"petsByName":{"one":$catJson},"optionalPet":$catJson}"""

    @Test
    fun `refined models expose their base contract without changing serialization or defaults`() {
        val model = Json.decodeFromString<CatProfile>(profileJson)
        val view: ProfileComposite = model
        val cat: Cat = model.pet
        assertThat(cat.id).isEqualTo("cat-1")
        assertThat(view.pet.id).isEqualTo("cat-1")
        assertThat(view.pets.map { it.id }).containsExactly("cat-1")
        assertThat(view.petsByName.getValue("one")?.id).isEqualTo("cat-1")
        assertThat(view.optionalPet?.id).isEqualTo("cat-1")
        assertThat(view.note).isEqualTo("untitled")
        assertThat(Json.decodeFromString<CatProfile>(Json.encodeToString(model))).isEqualTo(model)
        assertThat(model.copy()).isEqualTo(model)
    }

    @Test
    fun `existing discriminated inheritance preserves the inline-first subtype`() {
        val pet = Json.decodeFromString<Pet>("""{"kind":"dog","id":"dog-1","age":2}""")
        assertThat(pet).isInstanceOf(Dog::class.java)
        assertThat((pet as PetComposite).id).isEqualTo("dog-1")
        assertThat(Json.decodeFromString<Pet>(Json.encodeToString(pet))).isEqualTo(pet)
    }

    @Test
    fun `projected contracts only require retained model properties`() {
        val request = Json.decodeFromString<RequestB>("""{"secret":"secret"}""")
        assertThat(request).isInstanceOf(RequestContract::class.java)
        assertThat(request.secret).isEqualTo("secret")
        assertThat(Json.encodeToString(request)).doesNotContain("\"id\"")
        val response = Json.decodeFromString<ResponseB>("""{"id":"response-1"}""")
        val view: ResponseContract = response
        assertThat(view.id).isEqualTo("response-1")
        assertThat(Json.encodeToString(response)).doesNotContain("secret")
        assertThat(Json.decodeFromString<ResponseB>(Json.encodeToString(response))).isEqualTo(response)
    }
}
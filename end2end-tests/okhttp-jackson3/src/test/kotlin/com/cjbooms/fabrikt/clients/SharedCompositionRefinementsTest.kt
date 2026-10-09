package com.cjbooms.fabrikt.clients

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
import tools.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SharedCompositionRefinementsTest {
    private val mapper = jacksonObjectMapper()
    private val catJson = """{"kind":"cat","id":"cat-1","name":"Cat"}"""
    private val profileJson = """{"pet":$catJson,"pets":[$catJson],"petsByName":{"one":$catJson},"optionalPet":$catJson}"""

    @Test
    fun `refined models retain their concrete types and expose the base contract`() {
        val model = mapper.readValue(profileJson, CatProfile::class.java)
        val view: ProfileComposite = model
        val cat: Cat = model.pet
        assertThat(cat.id).isEqualTo("cat-1")
        assertThat(view.pet.id).isEqualTo("cat-1")
        assertThat(view.pets.map { it.id }).containsExactly("cat-1")
        assertThat(view.petsByName.getValue("one")?.id).isEqualTo("cat-1")
        assertThat(view.optionalPet?.id).isEqualTo("cat-1")
        assertThat(view.note).isEqualTo("untitled")
        assertThat(mapper.readValue(mapper.writeValueAsString(model), CatProfile::class.java)).isEqualTo(model)
        assertThat(model.copy()).isEqualTo(model)
    }

    @Test
    fun `discriminator inheritance remains correct when the inline allOf branch comes first`() {
        val pet = mapper.readValue("""{"kind":"dog","id":"dog-1","age":2}""", Pet::class.java)
        assertThat(pet).isInstanceOf(Dog::class.java)
        assertThat((pet as PetComposite).id).isEqualTo("dog-1")
        assertThat(mapper.readValue(mapper.writeValueAsString(pet), Pet::class.java)).isEqualTo(pet)
    }

    @Test
    fun `contracts reflect retained request and response fields without requiring filtered fields`() {
        val request = mapper.readValue("""{"secret":"secret"}""", RequestB::class.java)
        assertThat(request).isInstanceOf(RequestContract::class.java)
        assertThat(request.secret).isEqualTo("secret")
        assertThat(mapper.writeValueAsString(request)).contains("secret").doesNotContain("\"id\"")
        val response = ResponseB(id = "response-1")
        val view: ResponseContract = response
        assertThat(view.id).isEqualTo("response-1")
        assertThat(mapper.writeValueAsString(response)).contains("response-1").doesNotContain("secret")
    }
}
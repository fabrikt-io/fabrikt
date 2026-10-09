# Shared composition contracts

Enable `--http-model-opts SHARED_COMPOSITION_CONTRACTS` to expose explicitly referenced object composition through generated Kotlin interfaces. The option is disabled by default.

If `B` includes `A` through `allOf`, Fabrikt generates an `AComposite` interface from A's generated properties and makes compatible models A and B implement it. Transitive inclusion also counts: a model D including B can expose A's contract without becoming a subclass of the concrete data class A.

```yaml
components:
  schemas:
    A:
      type: object
      required: [id]
      properties:
        id:
          type: string
    B:
      allOf:
        - $ref: '#/components/schemas/A'
        - type: object
          properties:
            label:
              type: string
```

The generated models retain their concrete constructors and properties:

```kotlin
interface AComposite {
    val id: String
}

data class A(override val id: String) : AComposite

data class B(
    override val id: String,
    val label: String? = null,
) : AComposite
```

The example omits the existing serialization and validation annotations for readability. Those annotations remain on the concrete models; the contract does not introduce another serialization root or duplicate validation constraints.

```kotlin
val ids = values.filterIsInstance<AComposite>().map { it.id }
val shared = value as? AComposite
println(shared?.id)
```

Membership follows resolved schema references, rather than matching field names. An unrelated schema with an `id` field does not implement `AComposite`. A successful contract check exposes the existing object without allocating a copy; it does not make B an instance of concrete class A or validate a payload against every OpenAPI constraint.

Contract properties use the Kotlin types that Fabrikt actually generates. A model whose generated properties cannot implement the contract safely is omitted from that contract with a warning; its existing properties remain unchanged. Contract names use the model name plus `Composite`, and Fabrikt disambiguates collisions using its existing model name allocation.

## Typed unions

With typed `oneOf` generation enabled, contract checks follow the concrete variant. If `Mixed = oneOf[B, C, Other]`, and only B and C include A through `allOf`, Mixed does not extend `AComposite`. Deserializing Mixed into B or C produces an object that passes `is AComposite`; deserializing Other does not, even if Other declares a matching `id` field.

```kotlin
val values: List<Mixed> = obtainValues()
val ids = values.filterIsInstance<AComposite>().map { it.id }
```

When every generated alternative safely exposes A's contract, the union itself extends it:

```kotlin
sealed interface Shared : AComposite

fun process(value: Shared) {
    println(value.id)
}
```

The same guarantee applies to supported `allOf`/`oneOf` combinations: each generated composed alternative includes the referenced object, so its union can expose that object's contract. A property mismatch on any alternative prevents the union from promising the contract. Named object alternatives can also expose their own contracts through direct `oneOf` references.

Existing discriminator mappings, Jackson subtype deduction, and Kotlinx polymorphic serialization remain responsible for selecting the concrete model. Shared interfaces carry neither subtype mappings nor their own serializers. They do not add runtime checks enforcing `oneOf` exclusivity, and unions generated as an untyped fallback cannot provide typed variant guarantees.

Kotlinx retains its existing discriminator configuration requirements. If a concrete property has the same serialized name as its class discriminator, use its supported array polymorphism configuration (`Json { useArrayPolymorphism = true }`) for polymorphic serialization; enabling contracts does not resolve that existing collision.

## External composition

Resolved references keep the identity of their source schema across documents. Two paths that resolve to the same definition share one contract; equally named schemas from different documents keep separate contracts, even when their fields match. Transitive external references and standalone schema files are supported. Contract names follow the existing generated model names, including any collision suffixes.

External loading retains the configured reference resolution mode. Enabling contracts additionally discovers explicitly composed ancestors needed to express their contracts; unrelated external definitions do not gain membership.

Filtered property projections follow in the final part of [#747](https://github.com/fabrikt-io/fabrikt/issues/747).

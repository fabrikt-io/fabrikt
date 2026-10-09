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

The initial implementation covers named object schemas and local direct or transitive `allOf` references. Integration with typed `oneOf` unions, external references, and filtered property projections follows in the remaining parts of #747.

package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.configurations.Packages
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type
import java.util.UUID

object OpenFeignClientLibraryFiles {
    private const val DOLLAR = "$"
    private val encoder = ClassName("feign.codec", "Encoder")
    private val encodeException = ClassName("feign.codec", "EncodeException")
    private val requestTemplate = ClassName("feign", "RequestTemplate")
    private val reflectType = Type::class.asClassName()
    private val uuid = UUID::class.asClassName()
    private val byteArrayOutputStream = ByteArrayOutputStream::class.asClassName()
    private val serializeJsonType = LambdaTypeName.get(parameters = arrayOf(ANY), returnType = BYTE_ARRAY)

    fun multipartEncoder(packages: Packages): FileSpec =
        FileSpec
            .builder(packages.client, "FeignMultipartEncoder")
            .addType(jsonPartsAnnotation())
            .addType(multipartFile())
            .addType(multipartEncoderType(packages))
            .build()

    private fun jsonPartsAnnotation(): TypeSpec =
        TypeSpec
            .annotationBuilder("FeignJsonParts")
            .addAnnotation(
                AnnotationSpec
                    .builder(Target::class)
                    .addMember("%T.FUNCTION", AnnotationTarget::class)
                    .build(),
            ).addAnnotation(
                AnnotationSpec
                    .builder(Retention::class)
                    .addMember("%T.RUNTIME", AnnotationRetention::class)
                    .build(),
            ).primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .addParameter(ParameterSpec.builder("names", STRING).addModifiers(KModifier.VARARG).build())
                    .build(),
            ).addProperty(PropertySpec.builder("names", STRING).initializer("names").build())
            .build()

    private fun multipartFile(): TypeSpec =
        TypeSpec
            .classBuilder("FeignMultipartFile")
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .addParameter("bytes", BYTE_ARRAY)
                    .addParameter("filename", STRING)
                    .addParameter(
                        ParameterSpec.builder("contentType", STRING).defaultValue("%S", "application/octet-stream").build(),
                    ).build(),
            ).addProperty(PropertySpec.builder("bytes", BYTE_ARRAY).initializer("bytes").build())
            .addProperty(PropertySpec.builder("filename", STRING).initializer("filename").build())
            .addProperty(PropertySpec.builder("contentType", STRING).initializer("contentType").build())
            .build()

    private fun multipartEncoderType(packages: Packages): TypeSpec =
        TypeSpec
            .classBuilder("FeignMultipartEncoder")
            .addKdoc("Register on Feign's builder; delegates requests without generated multipart metadata.")
            .addSuperinterface(encoder)
            .primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .addParameter("delegate", encoder)
                    .addParameter("serializeJson", serializeJsonType)
                    .build(),
            ).addProperty(PropertySpec.builder("delegate", encoder, KModifier.PRIVATE).initializer("delegate").build())
            .addProperty(PropertySpec.builder("serializeJson", serializeJsonType, KModifier.PRIVATE).initializer("serializeJson").build())
            .addFunction(encodeFunction(packages))
            .build()

    private fun encodeFunction(packages: Packages): FunSpec {
        val jsonParts = ClassName(packages.client, "FeignJsonParts")
        val multipartFile = ClassName(packages.client, "FeignMultipartFile")
        return FunSpec
            .builder("encode")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("value", ANY.copy(nullable = true))
            .addParameter("bodyType", reflectType)
            .addParameter("template", requestTemplate)
            .addCode(
                CodeBlock
                    .builder()
                    .addStatement("val jsonParts = template.methodMetadata()?.method()?.getAnnotation(%T::class.java)", jsonParts)
                    .beginControlFlow("if (jsonParts == null)")
                    .addStatement("delegate.encode(value, bodyType, template)")
                    .addStatement("return")
                    .endControlFlow()
                    .addStatement("""val parts = value as? Map<*, *> ?: throw %T("Expected named multipart parts")""", encodeException)
                    .addStatement("""val boundary = "fabrikt-" + %T.randomUUID()""", uuid)
                    .addStatement("val output = %T()", byteArrayOutputStream)
                    .addStatement("fun text(value: String) { output.write(value.toByteArray(Charsets.UTF_8)) }")
                    .addStatement(
                        """fun quoted(value: String): String = value.replace("\r", "%%0D").replace("\n", "%%0A").replace("\"", "%%22")""",
                    ).beginControlFlow("fun part(name: String, bytes: ByteArray, contentType: String, filename: String? = null)")
                    .addStatement("""require(contentType.none { it == '\r' || it == '\n' }) { "Invalid multipart content type" }""")
                    .addStatement("""text("--${DOLLAR}boundary\r\nContent-Disposition: form-data; name=\"$DOLLAR{quoted(name)}\"")""")
                    .addStatement("""if (filename != null) text("; filename=\"$DOLLAR{quoted(filename)}\"")""")
                    .addStatement("""text("\r\nContent-Type: ${DOLLAR}contentType\r\n\r\n")""")
                    .addStatement("output.write(bytes)")
                    .addStatement("""text("\r\n")""")
                    .endControlFlow()
                    .beginControlFlow("parts.forEach { (key, item) ->")
                    .addStatement("""val name = key as? String ?: throw %T("Expected a string multipart name")""", encodeException)
                    .beginControlFlow("when")
                    .addStatement("item == null -> Unit")
                    .addStatement("""name in jsonParts.names -> part(name, serializeJson(item), "application/json")""")
                    .addStatement("item is %T -> part(name, item.bytes, item.contentType, item.filename)", multipartFile)
                    .add("item is Iterable<*> -> item.forEach { file ->\n")
                    .indent()
                    .addStatement("""require(file is %T) { "Expected binary multipart files" }""", multipartFile)
                    .addStatement("part(name, file.bytes, file.contentType, file.filename)")
                    .unindent()
                    .add("}\n")
                    .addStatement("""else -> part(name, item.toString().toByteArray(Charsets.UTF_8), "text/plain; charset=UTF-8")""")
                    .endControlFlow()
                    .endControlFlow()
                    .addStatement("""text("--${DOLLAR}boundary--\r\n")""")
                    .addStatement("""template.removeHeader("Content-Type")""")
                    .addStatement("""template.header("Content-Type", "multipart/form-data; boundary=${DOLLAR}boundary")""")
                    .addStatement("template.body(output.toByteArray(), null)")
                    .build(),
            ).build()
    }
}

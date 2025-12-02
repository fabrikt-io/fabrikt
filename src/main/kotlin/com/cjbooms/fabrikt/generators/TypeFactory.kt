package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName

object TypeFactory {

    fun createMapOfMapsStringToStringAny() =
        Map::class.asClassName().parameterizedBy(
            String::class.asTypeName(),
            Map::class.asTypeName().parameterizedBy(
                String::class.asTypeName(),
                Any::class.asTypeName().maybeMakeMapValueNullable()
            ).maybeMakeMapValueNullable()
        )

    fun createMutableMapOfMapsStringToStringType(type: TypeName) =
        ClassName("kotlin.collections", "MutableMap").parameterizedBy(
            String::class.asTypeName(),
            Map::class.asTypeName().parameterizedBy(
                String::class.asTypeName(),
                type.maybeMakeMapValueNullable()
            ).maybeMakeMapValueNullable()
        )

    fun createMutableMapOfStringToType(type: TypeName) =
        ClassName("kotlin.collections", "MutableMap").parameterizedBy(
            String::class.asTypeName(),
            type.maybeMakeMapValueNullable()
        )

    fun createMapOfStringToType(type: TypeName) =
        Map::class.asClassName().parameterizedBy(
            String::class.asTypeName(),
            type.maybeMakeMapValueNullable()
        )

    fun createMapOfStringToNonNullType(type: TypeName) =
        Map::class.asClassName().parameterizedBy(
            String::class.asTypeName(),
            type
        )

    fun createList(clazz: TypeName) =
        List::class.asClassName().parameterizedBy(clazz)

    fun createSet(clazz: TypeName) =
        LinkedHashSet::class.asClassName().parameterizedBy(clazz)

    fun TypeName.maybeMakeMapValueNullable(): TypeName =
        if (MutableSettings.modelOptions.contains(ModelCodeGenOptionType.NON_NULL_MAP_VALUES)) this
        else this.copy(nullable = true)
}

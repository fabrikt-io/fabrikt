package com.cjbooms.fabrikt.util

import com.cjbooms.fabrikt.cli.GeneratedVisibility
import com.cjbooms.fabrikt.generators.MutableSettings
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeSpec

object GeneratedVisibilityModifiers {
    fun TypeSpec.withVisibility(): TypeSpec =
        if (MutableSettings.visibility == GeneratedVisibility.PUBLIC || KModifier.PRIVATE in modifiers) {
            this
        } else {
            toBuilder().apply { modifiers.makeInternal() }.build()
        }

    fun FileSpec.withVisibility(): FileSpec {
        if (MutableSettings.visibility == GeneratedVisibility.PUBLIC) return this
        return toBuilder()
            .apply {
                members.clear()
                members.addAll(
                    this@withVisibility.members.map {
                        when (it) {
                            is TypeSpec -> it.withVisibility()
                            is FunSpec -> it.toBuilder().apply { modifiers.makeInternal() }.build()
                            is PropertySpec -> it.toBuilder().apply { modifiers.makeInternal() }.build()
                            is TypeAliasSpec -> it.toBuilder().apply { modifiers.makeInternal() }.build()
                            else -> it
                        }
                    },
                )
            }.build()
    }

    private fun MutableCollection<KModifier>.makeInternal() {
        if (KModifier.PRIVATE !in this) {
            remove(KModifier.PUBLIC)
            add(KModifier.INTERNAL)
        }
    }
}

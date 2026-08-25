package com.cjbooms.fabrikt.generators.controller

import com.cjbooms.fabrikt.model.ControllerLibraryType
import com.cjbooms.fabrikt.model.KotlinTypes

interface ControllerInterfaceGenerator {
    fun generate(): KotlinTypes

    fun generateLibrary(): Collection<ControllerLibraryType>
}

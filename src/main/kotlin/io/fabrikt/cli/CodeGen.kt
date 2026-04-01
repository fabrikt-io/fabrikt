package io.fabrikt.cli

object CodeGen {

    @JvmStatic
    fun main(args: Array<String>) {
        // Delegate to the original com.cjbooms entry point for backward compatibility
        com.cjbooms.fabrikt.cli.CodeGen.main(args)
    }
}

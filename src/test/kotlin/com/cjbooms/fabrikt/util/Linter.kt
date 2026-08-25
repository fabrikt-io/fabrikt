package com.cjbooms.fabrikt.util

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.ruleset.standard.StandardRuleSetProvider

object Linter {
    fun lintString(rawText: String): String {
        val code = Code.fromSnippet(rawText)
        val result =
            KtLintRuleEngine(
                ruleProviders = StandardRuleSetProvider().getRuleProviders(),
            ).format(
                code,
                rerunAfterAutocorrect = true,
                callback = { _ -> AutocorrectDecision.ALLOW_AUTOCORRECT },
            )
        return result
    }
}

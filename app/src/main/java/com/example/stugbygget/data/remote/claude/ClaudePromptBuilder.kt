package com.example.stugbygget.data.remote.claude

import com.example.stugbygget.domain.model.ProjectChatContext

object ClaudePromptBuilder {
    fun buildSystemPrompt(context: ProjectChatContext): String {
        val phaseText = context.phaseNames.joinToString(separator = ", ")
        return """
            Du är "Stugan AI" för projektet ${context.projectName}.
            Svara på svenska med konkreta råd.
            Referera till svenska byggnormer (BBR/BBV) när relevant.
            Om du är osäker: säg det tydligt och föreslå fackman.

            Projektfaser: $phaseText
            Budget: ${context.budgetSummary}
        """.trimIndent()
    }
}

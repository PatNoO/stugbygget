package com.example.stugbygget.data.remote.claude

import com.example.stugbygget.domain.model.ProjectChatContext
import org.junit.Assert.assertTrue
import org.junit.Test

class ClaudePromptBuilderTest {

    @Test
    fun `prompt contains project name`() {
        val context = ProjectChatContext(
            projectName = "Sommarstugan",
            phaseNames = listOf("Rivning"),
            budgetSummary = "Budget ej satt"
        )

        val prompt = ClaudePromptBuilder.buildSystemPrompt(context)

        assertTrue(prompt.contains("Sommarstugan"))
    }

    @Test
    fun `prompt contains phase names`() {
        val context = ProjectChatContext(
            projectName = "StugBygget",
            phaseNames = listOf("Rivning", "Tak", "El"),
            budgetSummary = "Budget ej satt"
        )

        val prompt = ClaudePromptBuilder.buildSystemPrompt(context)

        assertTrue(prompt.contains("Rivning"))
        assertTrue(prompt.contains("Tak"))
        assertTrue(prompt.contains("El"))
    }

    @Test
    fun `prompt contains budget summary`() {
        val context = ProjectChatContext(
            projectName = "StugBygget",
            phaseNames = listOf("Rivning"),
            budgetSummary = "Total budget: 250000 SEK"
        )

        val prompt = ClaudePromptBuilder.buildSystemPrompt(context)

        assertTrue(prompt.contains("Total budget: 250000 SEK"))
    }

    @Test
    fun `prompt contains swedish instruction`() {
        val context = ProjectChatContext(
            projectName = "StugBygget",
            phaseNames = emptyList(),
            budgetSummary = "Budget ej satt"
        )

        val prompt = ClaudePromptBuilder.buildSystemPrompt(context)

        assertTrue(prompt.contains("svenska"))
    }

    @Test
    fun `prompt handles empty phase list`() {
        val context = ProjectChatContext(
            projectName = "StugBygget",
            phaseNames = emptyList(),
            budgetSummary = "Budget ej satt"
        )

        // Should not throw and must produce a non-blank prompt
        val prompt = ClaudePromptBuilder.buildSystemPrompt(context)

        assertTrue(prompt.isNotBlank())
    }

    @Test
    fun `multiple phases joined with comma separator`() {
        val context = ProjectChatContext(
            projectName = "StugBygget",
            phaseNames = listOf("Fas A", "Fas B"),
            budgetSummary = "Budget ej satt"
        )

        val prompt = ClaudePromptBuilder.buildSystemPrompt(context)

        assertTrue(prompt.contains("Fas A, Fas B"))
    }
}

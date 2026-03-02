package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.domain.model.TodoItem
import com.example.stugbygget.domain.model.TodoPriority
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class TodoValidatorTest {

    @Test(expected = IllegalArgumentException::class)
    fun `validate throws when text is blank`() {
        TodoValidator.validate(
            TodoItem(
                id = "1",
                text = "",
                done = false,
                phaseId = "phase-1",
                assignee = "Pat",
                priority = TodoPriority.MEDIUM,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )
    }

    @Test
    fun `validate accepts valid todo`() {
        val todo = TodoItem(
            id = "2",
            text = "Beställ isolering",
            done = false,
            phaseId = "phase-2",
            assignee = "Pat",
            priority = TodoPriority.HIGH,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        TodoValidator.validate(todo)
        assertEquals("Beställ isolering", todo.text)
    }
}

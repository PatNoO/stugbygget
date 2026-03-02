package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.domain.model.TodoItem
import com.example.stugbygget.domain.model.TodoPriority
import com.google.firebase.Timestamp

object TodoDocumentMapper {

    fun fromMap(id: String, map: Map<String, Any?>): TodoItem {
        return TodoItem(
            id = id,
            text = map["text"] as? String ?: "",
            done = map["done"] as? Boolean ?: false,
            phaseId = map["phaseId"] as? String ?: "",
            assignee = map["assignee"] as? String ?: "",
            priority = parsePriority(map["priority"] as? String),
            createdAt = (map["createdAt"] as? Timestamp)?.toDate()?.toInstant() ?: java.time.Instant.now(),
            updatedAt = (map["updatedAt"] as? Timestamp)?.toDate()?.toInstant() ?: java.time.Instant.now()
        )
    }

    fun toMap(todo: TodoItem): Map<String, Any> = mapOf(
        "text" to todo.text,
        "done" to todo.done,
        "phaseId" to todo.phaseId,
        "assignee" to todo.assignee,
        "priority" to todo.priority.name,
        "createdAt" to Timestamp(todo.createdAt.epochSecond, 0),
        "updatedAt" to Timestamp(todo.updatedAt.epochSecond, 0)
    )

    private fun parsePriority(raw: String?): TodoPriority = when (raw) {
        TodoPriority.HIGH.name -> TodoPriority.HIGH
        TodoPriority.LOW.name -> TodoPriority.LOW
        else -> TodoPriority.MEDIUM
    }
}

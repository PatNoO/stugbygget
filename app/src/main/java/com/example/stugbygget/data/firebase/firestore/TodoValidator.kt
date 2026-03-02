package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.domain.model.TodoItem

object TodoValidator {

    fun validate(todo: TodoItem) {
        require(todo.text.isNotBlank()) { "Todo text must not be blank" }
        require(todo.phaseId.isNotBlank()) { "Todo phaseId must not be blank" }
        require(todo.assignee.isNotBlank()) { "Todo assignee must not be blank" }
    }
}

package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.core.offline.OfflineSyncCoordinator
import com.example.stugbygget.domain.model.TodoItem
import com.example.stugbygget.domain.repository.TodoRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreTodoRepository(
    private val firestore: FirebaseFirestore,
    private val offlineSyncCoordinator: OfflineSyncCoordinator
) : TodoRepository {

    override fun observeTodos(
        projectId: String,
        phaseId: String?,
        assignee: String?
    ): Flow<List<TodoItem>> = callbackFlow {
        val query = firestore.collection("projects")
            .document(projectId)
            .collection("todos")
        var cachedTodos: List<TodoItem> = emptyList()

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(cachedTodos)
                return@addSnapshotListener
            }

            val todos = snapshot?.documents.orEmpty()
                .mapNotNull { document ->
                    val map = document.data ?: return@mapNotNull null
                    runCatching { TodoDocumentMapper.fromMap(document.id, map) }.getOrNull()
                }
                .filter { todo -> phaseId == null || todo.phaseId == phaseId }
                .filter { todo -> assignee == null || todo.assignee == assignee }
                .sortedByDescending { it.createdAt }
            cachedTodos = todos
            trySend(todos)
        }

        awaitClose { registration.remove() }
    }

    override suspend fun upsertTodo(projectId: String, todo: TodoItem) {
        TodoValidator.validate(todo)
        offlineSyncCoordinator.runOrQueue {
            firestore.collection("projects")
                .document(projectId)
                .collection("todos")
                .document(todo.id)
                .set(TodoDocumentMapper.toMap(todo))
                .await()
        }
    }

    override suspend fun toggleTodo(projectId: String, todoId: String, done: Boolean) {
        offlineSyncCoordinator.runOrQueue {
            firestore.collection("projects")
                .document(projectId)
                .collection("todos")
                .document(todoId)
                .update(
                    mapOf(
                        "done" to done,
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
        }
    }

    override suspend fun deleteTodo(projectId: String, todoId: String) {
        offlineSyncCoordinator.runOrQueue {
            firestore.collection("projects")
                .document(projectId)
                .collection("todos")
                .document(todoId)
                .delete()
                .await()
        }
    }
}

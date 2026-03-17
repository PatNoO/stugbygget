package com.example.stugbygget.feature.todos.ui

import com.example.stugbygget.domain.model.TodoItem
import com.example.stugbygget.domain.model.TodoPriority
import com.example.stugbygget.domain.repository.TodoRepository
import com.example.stugbygget.domain.usecase.ObserveTodosUseCase
import com.example.stugbygget.domain.usecase.ToggleTodoUseCase
import com.example.stugbygget.domain.usecase.UpsertTodoUseCase
import com.example.stugbygget.domain.usecase.mockTodo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodosViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state has isLoading false after flow emits`() = runTest {
        val vm = buildViewModel(todos = emptyList())

        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.errorMessage)
    }

    // ── Todo loading ──────────────────────────────────────────────────────────

    @Test
    fun `todos are loaded into state`() = runTest {
        val todos = listOf(mockTodo("t1"), mockTodo("t2"))
        val vm = buildViewModel(todos = todos)

        assertEquals(todos, vm.uiState.value.todos)
    }

    @Test
    fun `availablePhases derived from loaded todos`() = runTest {
        val todos = listOf(
            mockTodo("t1", phaseId = "phase-B"),
            mockTodo("t2", phaseId = "phase-A"),
            mockTodo("t3", phaseId = "phase-A"),
        )
        val vm = buildViewModel(todos = todos)

        assertEquals(listOf("phase-A", "phase-B"), vm.uiState.value.availablePhases)
    }

    @Test
    fun `availableAssignees derived from loaded todos`() = runTest {
        val todos = listOf(
            mockTodo("t1", assignee = "Charlie"),
            mockTodo("t2", assignee = "Alice"),
            mockTodo("t3", assignee = "Alice"),
        )
        val vm = buildViewModel(todos = todos)

        assertEquals(listOf("Alice", "Charlie"), vm.uiState.value.availableAssignees)
    }

    @Test
    fun `error from repository sets errorMessage`() = runTest {
        val vm = buildViewModel(throwError = true)

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    // ── Filters ───────────────────────────────────────────────────────────────

    @Test
    fun `onPhaseFilterSelected updates selectedPhase`() = runTest {
        val vm = buildViewModel()

        vm.onPhaseFilterSelected("phase-A")

        assertEquals("phase-A", vm.uiState.value.selectedPhase)
    }

    @Test
    fun `onPhaseFilterSelected with null clears filter`() = runTest {
        val vm = buildViewModel()
        vm.onPhaseFilterSelected("phase-A")

        vm.onPhaseFilterSelected(null)

        assertNull(vm.uiState.value.selectedPhase)
    }

    @Test
    fun `onAssigneeFilterSelected updates selectedAssignee`() = runTest {
        val vm = buildViewModel()

        vm.onAssigneeFilterSelected("Bob")

        assertEquals("Bob", vm.uiState.value.selectedAssignee)
    }

    // ── Add-todo sheet ────────────────────────────────────────────────────────

    @Test
    fun `onShowAddSheet sets showAddSheet and clears draft`() = runTest {
        val vm = buildViewModel()
        vm.onDraftTextChanged("Some task")

        vm.onShowAddSheet()

        assertTrue(vm.uiState.value.showAddSheet)
        assertEquals("", vm.uiState.value.draftText)
        assertNull(vm.uiState.value.addError)
    }

    @Test
    fun `onDismissAddSheet clears sheet and error`() = runTest {
        val vm = buildViewModel()
        vm.onShowAddSheet()

        vm.onDismissAddSheet()

        assertFalse(vm.uiState.value.showAddSheet)
        assertNull(vm.uiState.value.addError)
    }

    @Test
    fun `draft field updates reflected in state`() = runTest {
        val vm = buildViewModel()

        vm.onDraftTextChanged("Fix the door")
        vm.onDraftAssigneeChanged("Alice")
        vm.onDraftPriorityChanged(TodoPriority.HIGH)
        vm.onDraftPhaseIdChanged("phase-2")

        assertEquals("Fix the door", vm.uiState.value.draftText)
        assertEquals("Alice", vm.uiState.value.draftAssignee)
        assertEquals(TodoPriority.HIGH, vm.uiState.value.draftPriority)
        assertEquals("phase-2", vm.uiState.value.draftPhaseId)
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    fun `submit with blank text sets addError`() = runTest {
        val vm = buildViewModel()

        vm.onSubmitTodo()

        assertNotNull(vm.uiState.value.addError)
    }

    // ── Successful submit ─────────────────────────────────────────────────────

    @Test
    fun `valid submit calls upsert and closes sheet`() = runTest {
        val repo = FakeTodoRepository()
        val vm = buildViewModel(repo = repo)

        vm.onDraftTextChanged("Install shelves")
        vm.onSubmitTodo()

        assertNotNull(repo.lastUpserted)
        assertEquals("Install shelves", repo.lastUpserted?.text)
        assertFalse(vm.uiState.value.showAddSheet)
        assertNull(vm.uiState.value.addError)
    }

    @Test
    fun `submit uses trimmed text`() = runTest {
        val repo = FakeTodoRepository()
        val vm = buildViewModel(repo = repo)

        vm.onDraftTextChanged("  Install shelves  ")
        vm.onSubmitTodo()

        assertEquals("Install shelves", repo.lastUpserted?.text)
    }

    @Test
    fun `submit failure sets addError without closing sheet`() = runTest {
        val repo = FakeTodoRepository(throwOnUpsert = true)
        val vm = buildViewModel(repo = repo)
        vm.onShowAddSheet()

        vm.onDraftTextChanged("Install shelves")
        vm.onSubmitTodo()

        assertNotNull(vm.uiState.value.addError)
    }

    // ── Toggle ────────────────────────────────────────────────────────────────

    @Test
    fun `onTodoToggle delegates to repository`() = runTest {
        val repo = FakeTodoRepository()
        val vm = buildViewModel(repo = repo)

        vm.onTodoToggle("todo-42", true)

        assertEquals("todo-42", repo.lastToggledId)
        assertEquals(true, repo.lastToggledDone)
    }

    @Test
    fun `toggle failure sets errorMessage`() = runTest {
        val repo = FakeTodoRepository(throwOnToggle = true)
        val vm = buildViewModel(repo = repo)

        vm.onTodoToggle("todo-1", true)

        assertNotNull(vm.uiState.value.errorMessage)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildViewModel(
        todos: List<TodoItem> = emptyList(),
        throwError: Boolean = false,
        repo: FakeTodoRepository = FakeTodoRepository(todos = todos, throwOnObserve = throwError)
    ): TodosViewModel = TodosViewModel(
        observeTodosUseCase = ObserveTodosUseCase(repo),
        toggleTodoUseCase = ToggleTodoUseCase(repo),
        upsertTodoUseCase = UpsertTodoUseCase(repo),
        projectId = "project-1"
    )

    private class FakeTodoRepository(
        private val todos: List<TodoItem> = emptyList(),
        private val throwOnObserve: Boolean = false,
        private val throwOnUpsert: Boolean = false,
        private val throwOnToggle: Boolean = false
    ) : TodoRepository {
        var lastUpserted: TodoItem? = null
        var lastToggledId: String? = null
        var lastToggledDone: Boolean? = null

        override fun observeTodos(
            projectId: String,
            phaseId: String?,
            assignee: String?
        ): Flow<List<TodoItem>> {
            if (throwOnObserve) throw RuntimeException("Firestore unavailable")
            return flowOf(todos)
        }

        override suspend fun upsertTodo(projectId: String, todo: TodoItem) {
            if (throwOnUpsert) throw RuntimeException("Write failed")
            lastUpserted = todo
        }

        override suspend fun toggleTodo(projectId: String, todoId: String, done: Boolean) {
            if (throwOnToggle) throw RuntimeException("Toggle failed")
            lastToggledId = todoId
            lastToggledDone = done
        }

        override suspend fun deleteTodo(projectId: String, todoId: String) = Unit
    }
}

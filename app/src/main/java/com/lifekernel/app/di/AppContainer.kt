package com.lifekernel.app.di

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.lifekernel.app.core.time.Clock
import com.lifekernel.app.core.time.DefaultClock
import com.lifekernel.app.data.local.LifeKernelDatabase
import com.lifekernel.app.data.prefs.SettingsPreferences
import com.lifekernel.app.data.prefs.SettingsRepositoryImpl
import com.lifekernel.app.data.repository.BackupRepositoryImpl
import com.lifekernel.app.data.repository.SubtaskRepositoryImpl
import com.lifekernel.app.data.repository.TagRepositoryImpl
import com.lifekernel.app.data.repository.TaskRepositoryImpl
import com.lifekernel.app.domain.repository.BackupRepository
import com.lifekernel.app.domain.repository.SettingsRepository
import com.lifekernel.app.domain.repository.SubtaskRepository
import com.lifekernel.app.domain.repository.TagRepository
import com.lifekernel.app.domain.repository.TaskRepository
import com.lifekernel.app.infra.backup.writer.BackupWriter
import com.lifekernel.app.domain.usecase.AddSubtaskUseCase
import com.lifekernel.app.domain.usecase.CompleteTaskUseCase
import com.lifekernel.app.domain.usecase.CreateTaskUseCase
import com.lifekernel.app.domain.usecase.DeleteSubtaskUseCase
import com.lifekernel.app.domain.usecase.DeleteTaskUseCase
import com.lifekernel.app.domain.usecase.GetTagsForTaskUseCase
import com.lifekernel.app.domain.usecase.GetTaskByIdUseCase
import com.lifekernel.app.domain.usecase.ObserveAllTagsUseCase
import com.lifekernel.app.domain.usecase.ObservePlannerTasksUseCase
import com.lifekernel.app.domain.usecase.ObserveSubtasksForTaskUseCase
import com.lifekernel.app.domain.usecase.ReopenTaskUseCase
import com.lifekernel.app.domain.usecase.SearchAndFilterTasksUseCase
import com.lifekernel.app.domain.usecase.SyncTaskTagsUseCase
import com.lifekernel.app.domain.usecase.ToggleSubtaskUseCase
import com.lifekernel.app.domain.usecase.UpdateTaskUseCase
import com.lifekernel.app.feature.planner.PlannerViewModel
import com.lifekernel.app.feature.settings.SettingsViewModel
import com.lifekernel.app.feature.tasks.TaskDetailViewModel
import kotlinx.coroutines.Dispatchers

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("No AppContainer provided. Ensure AppNavHost or MainActivity provides LocalAppContainer.")
}

/**
 * Dependency injection container for manual DI across Life Kernel.
 *
 * Invariant: All singletons (database, clock, repositories) are registered here.
 * No service locators or hidden singletons outside this container.
 */
interface AppContainer {
    val clock: Clock
    val database: LifeKernelDatabase
    val taskRepository: TaskRepository
    val tagRepository: TagRepository
    val subtaskRepository: SubtaskRepository
    val settingsRepository: SettingsRepository
    val backupWriter: BackupWriter
    val backupRepository: BackupRepository
    val observePlannerTasksUseCase: ObservePlannerTasksUseCase
    val completeTaskUseCase: CompleteTaskUseCase
    val reopenTaskUseCase: ReopenTaskUseCase
    val createTaskUseCase: CreateTaskUseCase
    val getTaskByIdUseCase: GetTaskByIdUseCase
    val updateTaskUseCase: UpdateTaskUseCase
    val deleteTaskUseCase: DeleteTaskUseCase
    val observeAllTagsUseCase: ObserveAllTagsUseCase
    val searchAndFilterTasksUseCase: SearchAndFilterTasksUseCase
    val getTagsForTaskUseCase: GetTagsForTaskUseCase
    val syncTaskTagsUseCase: SyncTaskTagsUseCase
    val observeSubtasksForTaskUseCase: ObserveSubtasksForTaskUseCase
    val addSubtaskUseCase: AddSubtaskUseCase
    val toggleSubtaskUseCase: ToggleSubtaskUseCase
    val deleteSubtaskUseCase: DeleteSubtaskUseCase
    fun createPlannerViewModel(): PlannerViewModel
    fun createSettingsViewModel(): SettingsViewModel
    fun createTaskDetailViewModel(taskId: Long): TaskDetailViewModel
}

/**
 * Default implementation of [AppContainer], initialized in [com.lifekernel.app.LifeKernelApp].
 */
class DefaultAppContainer(
    private val context: Context
) : AppContainer {
    override val clock: Clock by lazy { DefaultClock() }
    override val database: LifeKernelDatabase by lazy { LifeKernelDatabase.create(context) }

    override val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(
            database = database,
            clock = clock,
            dispatcher = Dispatchers.IO
        )
    }

    override val tagRepository: TagRepository by lazy {
        TagRepositoryImpl(
            database = database,
            clock = clock,
            dispatcher = Dispatchers.IO
        )
    }

    override val subtaskRepository: SubtaskRepository by lazy {
        SubtaskRepositoryImpl(
            database = database,
            clock = clock,
            dispatcher = Dispatchers.IO
        )
    }

    override val settingsRepository: SettingsRepository by lazy {
        val dataStore = SettingsPreferences.createDataStore(context)
        SettingsRepositoryImpl(
            dataStore = dataStore,
            dispatcher = Dispatchers.IO
        )
    }

    override val backupWriter: BackupWriter by lazy {
        BackupWriter(
            database = database,
            settingsRepository = settingsRepository,
            clock = clock,
            dispatcher = Dispatchers.IO
        )
    }

    override val backupRepository: BackupRepository by lazy {
        BackupRepositoryImpl(
            backupWriter = backupWriter,
            dispatcher = Dispatchers.IO
        )
    }

    override val observePlannerTasksUseCase: ObservePlannerTasksUseCase by lazy {
        ObservePlannerTasksUseCase(
            taskRepository = taskRepository,
            clock = clock
        )
    }

    override val completeTaskUseCase: CompleteTaskUseCase by lazy {
        CompleteTaskUseCase(
            taskRepository = taskRepository
        )
    }

    override val reopenTaskUseCase: ReopenTaskUseCase by lazy {
        ReopenTaskUseCase(
            taskRepository = taskRepository
        )
    }

    override val createTaskUseCase: CreateTaskUseCase by lazy {
        CreateTaskUseCase(
            taskRepository = taskRepository,
            clock = clock
        )
    }

    override val getTaskByIdUseCase: GetTaskByIdUseCase by lazy {
        GetTaskByIdUseCase(
            taskRepository = taskRepository
        )
    }

    override val updateTaskUseCase: UpdateTaskUseCase by lazy {
        UpdateTaskUseCase(
            taskRepository = taskRepository,
            clock = clock
        )
    }

    override val deleteTaskUseCase: DeleteTaskUseCase by lazy {
        DeleteTaskUseCase(
            taskRepository = taskRepository
        )
    }

    override val observeAllTagsUseCase: ObserveAllTagsUseCase by lazy {
        ObserveAllTagsUseCase(
            tagRepository = tagRepository
        )
    }

    override val getTagsForTaskUseCase: GetTagsForTaskUseCase by lazy {
        GetTagsForTaskUseCase(
            tagRepository = tagRepository
        )
    }

    override val syncTaskTagsUseCase: SyncTaskTagsUseCase by lazy {
        SyncTaskTagsUseCase(
            tagRepository = tagRepository
        )
    }

    override val observeSubtasksForTaskUseCase: ObserveSubtasksForTaskUseCase by lazy {
        ObserveSubtasksForTaskUseCase(
            subtaskRepository = subtaskRepository
        )
    }

    override val addSubtaskUseCase: AddSubtaskUseCase by lazy {
        AddSubtaskUseCase(
            subtaskRepository = subtaskRepository
        )
    }

    override val toggleSubtaskUseCase: ToggleSubtaskUseCase by lazy {
        ToggleSubtaskUseCase(
            subtaskRepository = subtaskRepository
        )
    }

    override val deleteSubtaskUseCase: DeleteSubtaskUseCase by lazy {
        DeleteSubtaskUseCase(
            subtaskRepository = subtaskRepository
        )
    }

    override val searchAndFilterTasksUseCase: SearchAndFilterTasksUseCase by lazy {
        SearchAndFilterTasksUseCase(
            taskRepository = taskRepository
        )
    }

    override fun createPlannerViewModel(): PlannerViewModel {
        return PlannerViewModel(
            observePlannerTasksUseCase = observePlannerTasksUseCase,
            completeTaskUseCase = completeTaskUseCase,
            reopenTaskUseCase = reopenTaskUseCase,
            createTaskUseCase = createTaskUseCase,
            searchAndFilterTasksUseCase = searchAndFilterTasksUseCase,
            observeAllTagsUseCase = observeAllTagsUseCase,
            clock = clock
        )
    }

    override fun createSettingsViewModel(): SettingsViewModel {
        return SettingsViewModel(
            settingsRepository = settingsRepository
        )
    }

    override fun createTaskDetailViewModel(taskId: Long): TaskDetailViewModel {
        return TaskDetailViewModel(
            taskId = taskId,
            getTaskByIdUseCase = getTaskByIdUseCase,
            updateTaskUseCase = updateTaskUseCase,
            deleteTaskUseCase = deleteTaskUseCase,
            observeAllTagsUseCase = observeAllTagsUseCase,
            syncTaskTagsUseCase = syncTaskTagsUseCase,
            observeSubtasksForTaskUseCase = observeSubtasksForTaskUseCase,
            addSubtaskUseCase = addSubtaskUseCase,
            toggleSubtaskUseCase = toggleSubtaskUseCase,
            deleteSubtaskUseCase = deleteSubtaskUseCase,
            clock = clock
        )
    }
}

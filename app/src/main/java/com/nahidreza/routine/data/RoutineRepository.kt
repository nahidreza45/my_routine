package com.nahidreza.routine.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nahidreza.routine.model.AppDatabaseState
import com.nahidreza.routine.model.AppSettings
import com.nahidreza.routine.model.SharedScheduleBackup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed interface DecodedBackup {
    data class FullBackup(val state: AppDatabaseState) : DecodedBackup
    data class ScheduleOnly(val schedule: SharedScheduleBackup) : DecodedBackup
}

private val Context.routineDataStore: DataStore<Preferences> by preferencesDataStore(name = "routine_db")

class RoutineRepository(private val context: Context) {

    private val storageJson = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        prettyPrint = false
    }

    private val exportJson = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        prettyPrint = true
    }

    private val stateKey = stringPreferencesKey("app_database_json_v2")

    val stateFlow: Flow<AppDatabaseState> = context.routineDataStore.data.map { prefs ->
        val raw = prefs[stateKey]
        if (raw.isNullOrBlank()) {
            createEmptyState()
        } else {
            runCatching { storageJson.decodeFromString<AppDatabaseState>(raw) }
                .getOrElse { createEmptyState() }
        }
    }

    suspend fun ensureInitialized() {
        val prefs = context.routineDataStore.data.first()
        if (prefs[stateKey].isNullOrBlank()) {
            saveState(createEmptyState())
        }
    }

    suspend fun saveState(state: AppDatabaseState) {
        val serialized = storageJson.encodeToString(AppDatabaseState.serializer(), state)
        context.routineDataStore.edit { prefs ->
            prefs[stateKey] = serialized
        }
    }

    suspend fun updateState(transform: (AppDatabaseState) -> AppDatabaseState): AppDatabaseState {
        var updated = createEmptyState()
        context.routineDataStore.edit { prefs ->
            val currentRaw = prefs[stateKey]
            val current = if (currentRaw.isNullOrBlank()) {
                createEmptyState()
            } else {
                runCatching { storageJson.decodeFromString<AppDatabaseState>(currentRaw) }
                    .getOrElse { createEmptyState() }
            }
            updated = transform(current)
            prefs[stateKey] = storageJson.encodeToString(AppDatabaseState.serializer(), updated)
        }
        return updated
    }

    fun exportToJson(state: AppDatabaseState): String {
        return exportJson.encodeToString(AppDatabaseState.serializer(), state)
    }

    fun exportScheduleOnlyJson(state: AppDatabaseState): String {
        val payload = SharedScheduleBackup(
            subjects = state.subjects,
            entries = state.entries,
            holidays = state.holidays,
            semesterStartDateIso = state.settings.semesterStartDateIso,
            manualWeekOffset = state.settings.manualWeekOffset
        )
        return exportJson.encodeToString(SharedScheduleBackup.serializer(), payload)
    }

    fun decodeBackup(rawJson: String): Result<DecodedBackup> {
        return runCatching {
            val element = storageJson.parseToJsonElement(rawJson)
            val obj: JsonObject = element.jsonObject
            val kind = obj["kind"]?.jsonPrimitive?.contentOrNull
            when {
                kind == SharedScheduleBackup.KIND_SCHEDULE_ONLY -> {
                    DecodedBackup.ScheduleOnly(
                        storageJson.decodeFromJsonElement(SharedScheduleBackup.serializer(), obj)
                    )
                }
                "ctRecords" in obj || "settings" in obj || "cancellations" in obj -> {
                    DecodedBackup.FullBackup(
                        storageJson.decodeFromJsonElement(AppDatabaseState.serializer(), obj)
                    )
                }
                "subjects" in obj || "entries" in obj -> {
                    DecodedBackup.ScheduleOnly(
                        storageJson.decodeFromJsonElement(SharedScheduleBackup.serializer(), obj)
                    )
                }
                else -> throw IllegalArgumentException("Unrecognized routine JSON format")
            }
        }
    }

    companion object {
        fun mergeSharedSchedule(
            current: AppDatabaseState,
            schedule: SharedScheduleBackup
        ): AppDatabaseState {
            val validEntryIds = schedule.entries.mapTo(HashSet()) { it.id }
            return current.copy(
                subjects = schedule.subjects,
                entries = schedule.entries,
                holidays = schedule.holidays,
                cancellations = current.cancellations.filter { it.entryId in validEntryIds },
                settings = current.settings.copy(
                    semesterStartDateIso = schedule.semesterStartDateIso,
                    manualWeekOffset = schedule.manualWeekOffset
                )
            )
        }

        fun createEmptyState(currentSettings: AppSettings = AppSettings()): AppDatabaseState {
            return AppDatabaseState(
                subjects = emptyList(),
                entries = emptyList(),
                cancellations = emptyList(),
                holidays = emptyList(),
                ctRecords = emptyList(),
                settings = currentSettings
            )
        }
    }
}

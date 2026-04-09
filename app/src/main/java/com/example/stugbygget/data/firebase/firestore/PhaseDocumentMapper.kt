package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.domain.model.RenovationPhase
import com.google.firebase.Timestamp
import java.time.Instant
import java.util.Date

object PhaseDocumentMapper {

    fun fromMap(id: String, map: Map<String, Any?>): RenovationPhase {
        val startDate = (map["startDate"] as? Timestamp)?.toDate()?.toInstant()
            ?: throw IllegalArgumentException("Missing startDate")
        val endDate = (map["endDate"] as? Timestamp)?.toDate()?.toInstant()
            ?: throw IllegalArgumentException("Missing endDate")

        return RenovationPhase(
            id = id,
            name = map["name"] as? String ?: "",
            room = map["room"] as? String ?: "",
            description = map["description"] as? String ?: "",
            startDate = startDate,
            endDate = endDate,
            progress = (map["progress"] as? Number)?.toInt()?.coerceIn(0, 100) ?: 0,
            color = map["color"] as? String ?: "#8B2E16",
            icon = map["icon"] as? String ?: "🔧"
        )
    }

    fun toMap(phase: RenovationPhase): Map<String, Any> = mapOf(
        "name" to phase.name,
        "room" to phase.room,
        "description" to phase.description,
        "startDate" to Timestamp(Date.from(phase.startDate)),
        "endDate" to Timestamp(Date.from(phase.endDate)),
        "progress" to phase.progress,
        "color" to phase.color,
        "icon" to phase.icon,
    )

    fun mockPhase(id: String = "phase-1"): RenovationPhase = RenovationPhase(
        id = id,
        name = "Rivning & städning",
        room = "Hela stugan",
        startDate = Instant.parse("2026-06-02T00:00:00Z"),
        endDate = Instant.parse("2026-06-08T00:00:00Z"),
        progress = 0,
        color = "#8B2E16",
        icon = "🧹"
    )
}

package com.example.stugbygget.data.firebase.firestore

import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Test

class PhaseDocumentMapperTest {

    @Test
    fun `fromMap maps valid phase payload`() {
        val map = mapOf(
            "name" to "Tak & isolering",
            "room" to "Tak",
            "startDate" to Timestamp(Date(1_717_286_400_000L)),
            "endDate" to Timestamp(Date(1_717_804_800_000L)),
            "progress" to 42,
            "color" to "#2E6B8A",
            "icon" to "🏠"
        )

        val phase = PhaseDocumentMapper.fromMap("phase-2", map)

        assertEquals("phase-2", phase.id)
        assertEquals("Tak & isolering", phase.name)
        assertEquals("Tak", phase.room)
        assertEquals(42, phase.progress)
        assertEquals("#2E6B8A", phase.color)
        assertEquals("🏠", phase.icon)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fromMap throws when startDate is missing`() {
        val map = mapOf(
            "name" to "Tak & isolering",
            "room" to "Tak"
        )

        PhaseDocumentMapper.fromMap("phase-2", map)
    }
}

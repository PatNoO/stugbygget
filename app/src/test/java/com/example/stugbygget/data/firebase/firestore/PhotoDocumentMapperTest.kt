package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.domain.model.PhotoPhase
import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoDocumentMapperTest {

    @Test
    fun `fromMap maps photo values`() {
        val map = mapOf(
            "roomName" to "Kök",
            "phase" to "BEFORE",
            "description" to "Före renovering",
            "storagePath" to "projects/p1/photos/a.jpg",
            "downloadUrl" to "https://example.com/a.jpg",
            "takenAt" to Timestamp(Date(1_717_286_400_000L)),
            "uploadedBy" to "uid-1"
        )

        val photo = PhotoDocumentMapper.fromMap("photo-1", map)

        assertEquals("photo-1", photo.id)
        assertEquals("Kök", photo.roomName)
        assertEquals(PhotoPhase.BEFORE, photo.phase)
        assertEquals("uid-1", photo.uploadedBy)
    }
}

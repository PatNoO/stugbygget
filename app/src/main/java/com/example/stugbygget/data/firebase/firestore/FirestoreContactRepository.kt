package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.domain.model.Contact
import com.example.stugbygget.domain.repository.ContactRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreContactRepository(
    private val firestore: FirebaseFirestore
) : ContactRepository {

    override fun observeContacts(projectId: String): Flow<List<Contact>> = callbackFlow {
        val collection = firestore.collection("projects")
            .document(projectId)
            .collection("contacts")

        val registration = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val contacts = snapshot?.documents.orEmpty()
                .mapNotNull { doc ->
                    val map = doc.data ?: return@mapNotNull null
                    runCatching { ContactDocumentMapper.fromMap(doc.id, map) }.getOrNull()
                }
                .sortedBy { it.name }
            trySend(contacts)
        }

        awaitClose { registration.remove() }
    }

    override suspend fun upsertContact(projectId: String, contact: Contact) {
        ContactValidator.validate(contact)
        firestore.collection("projects")
            .document(projectId)
            .collection("contacts")
            .document(contact.id)
            .set(ContactDocumentMapper.toMap(contact))
            .await()
    }

    override suspend fun deleteContact(projectId: String, contactId: String) {
        firestore.collection("projects")
            .document(projectId)
            .collection("contacts")
            .document(contactId)
            .delete()
            .await()
    }
}

package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.domain.model.Contact
import com.example.stugbygget.domain.model.ContactRole

object ContactDocumentMapper {

    fun fromMap(id: String, map: Map<String, Any?>): Contact = Contact(
        id = id,
        name = map["name"] as? String ?: "",
        phone = map["phone"] as? String ?: "",
        email = map["email"] as? String ?: "",
        role = parseRole(map["role"] as? String)
    )

    fun toMap(contact: Contact): Map<String, Any> = mapOf(
        "name" to contact.name,
        "phone" to contact.phone,
        "email" to contact.email,
        "role" to contact.role.name
    )

    private fun parseRole(raw: String?): ContactRole = when (raw) {
        ContactRole.CONTRACTOR.name -> ContactRole.CONTRACTOR
        ContactRole.SUPPLIER.name -> ContactRole.SUPPLIER
        ContactRole.TEAM_MEMBER.name -> ContactRole.TEAM_MEMBER
        else -> ContactRole.OTHER
    }
}

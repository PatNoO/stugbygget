package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.domain.model.Contact

object ContactValidator {
    fun validate(contact: Contact) {
        require(contact.name.isNotBlank()) { "Contact name must not be blank." }
        require(contact.id.isNotBlank()) { "Contact id must not be blank." }
    }
}

package com.example.stugbygget.domain.model

enum class ContactRole {
    CONTRACTOR,
    SUPPLIER,
    TEAM_MEMBER,
    OTHER
}

data class Contact(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val role: ContactRole
)

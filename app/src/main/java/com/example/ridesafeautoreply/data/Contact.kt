package com.example.ridesafeautoreply.data

import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val name: String,
    val phoneNumber: String
)

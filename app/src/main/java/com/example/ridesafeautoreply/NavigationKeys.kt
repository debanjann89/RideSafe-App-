package com.example.ridesafeautoreply

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey
@Serializable data object RidingStatus : NavKey
@Serializable data object Settings : NavKey
@Serializable data object ContactsSelection : NavKey
@Serializable data object MessageCustomization : NavKey
@Serializable data object RideHistory : NavKey

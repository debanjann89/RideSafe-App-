package com.example.ridesafeautoreply

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.ridesafeautoreply.ui.home.HomeScreen
import com.example.ridesafeautoreply.ui.riding.RidingStatusScreen
import com.example.ridesafeautoreply.ui.settings.SettingsScreen
import com.example.ridesafeautoreply.ui.contacts.ContactsScreen
import com.example.ridesafeautoreply.ui.message.MessageScreen
import com.example.ridesafeautoreply.ui.history.RideHistoryScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Main)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          HomeScreen(
            onNavigate = { navKey -> backStack.add(navKey) },
            modifier = Modifier.safeDrawingPadding()
          )
        }
        entry<RidingStatus> {
          RidingStatusScreen(
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.safeDrawingPadding()
          )
        }
        entry<Settings> {
          SettingsScreen(
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.safeDrawingPadding()
          )
        }
        entry<ContactsSelection> {
          ContactsScreen(
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.safeDrawingPadding()
          )
        }
        entry<MessageCustomization> {
          MessageScreen(
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.safeDrawingPadding()
          )
        }
        entry<RideHistory> {
          RideHistoryScreen(
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.safeDrawingPadding()
          )
        }
      },
  )
}

package com.example.foodiebuddy.ui.ingredients

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.foodiebuddy.R
import com.example.foodiebuddy.errors.handleError
import com.example.foodiebuddy.navigation.NavigationActions
import com.example.foodiebuddy.navigation.Route
import com.example.foodiebuddy.ui.PrimaryScreen
import com.example.foodiebuddy.viewModels.UserViewModel

@Composable
fun FridgeHome(userViewModel: UserViewModel, navigationActions: NavigationActions) {
    // pressing the Android back button on this screen does not change it
    BackHandler {
        navigationActions.navigateTo(Route.FRIDGE, true)
    }

    val context = LocalContext.current

    val userData by userViewModel.userData.collectAsState()
    userViewModel.fetchUserData({
        if (it) { handleError(context, "Could not fetch user data") }
    }){}

    PrimaryScreen(
        navigationActions = navigationActions,
        title = stringResource(R.string.title_fridge),
        navigationIndex = 2,
        topBarIcons = {},
        userViewModel = userViewModel,
        floatingButton = {},
        content = {paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    Text(text = "Hello")
                }
            }
        }
    )
}
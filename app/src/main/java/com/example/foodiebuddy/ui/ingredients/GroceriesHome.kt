package com.example.foodiebuddy.ui.ingredients

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.example.foodiebuddy.R
import com.example.foodiebuddy.data.OwnedIngredient
import com.example.foodiebuddy.errors.handleError
import com.example.foodiebuddy.navigation.NavigationActions
import com.example.foodiebuddy.navigation.Route
import com.example.foodiebuddy.ui.DialogWindow
import com.example.foodiebuddy.ui.LoadingPage
import com.example.foodiebuddy.ui.MiniLoading
import com.example.foodiebuddy.ui.OptionsMenu
import com.example.foodiebuddy.ui.PrimaryScreen
import com.example.foodiebuddy.ui.theme.MyTypography
import com.example.foodiebuddy.viewModels.UserViewModel

@Composable
fun GroceriesHome(userViewModel: UserViewModel, navigationActions: NavigationActions) {
    val screenState = remember { mutableStateOf(ScreenState.VIEWING) }
    val loading = remember { mutableStateOf(false) }
    val userData by userViewModel.userData.collectAsState()

    // pressing the Android back button on this screen does not change it
    BackHandler {
        navigationActions.navigateTo(Route.GROCERIES, true)
        if (screenState.value == ScreenState.EDITING) screenState.value = ScreenState.VIEWING
    }

    val context = LocalContext.current

    val userPersonal by userViewModel.userPersonal.collectAsState()
    val groceries = remember { mutableStateOf(userPersonal.groceryList) }

    // these variables hold all the modifications the user is making before they are bulked updated if the user saves the modifications
    val newItems = groceries.value.mapValues { mutableListOf<OwnedIngredient>() }
    val removedItems = groceries.value.mapValues { mutableListOf<String>() }
    val editedCategories = mutableMapOf<String, String>()
    val newCategoryName = remember { mutableStateOf("") }
    val newCategories = remember { mutableStateOf(mapOf<String, MutableList<OwnedIngredient>>()) }
    val removedCategories = remember { mutableStateListOf<String>() }
    val unavailableCategoryNames = groceries.value.keys.toMutableStateList()

    val showCategoryAlert = remember { mutableStateOf(false) }
    var deletingCategory = ""
    val showDeleteAlert = remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        screenState.value = ScreenState.LOADING
        userViewModel.fetchUserData({
            if (it) { handleError(context, "Could not fetch user data") }
        }){}
        userViewModel.fetchUserPersonal({
            if (it) {
                handleError(context, "Could not fetch user personal")
                screenState.value = ScreenState.VIEWING
            }
        }){
            screenState.value = ScreenState.VIEWING
        }
    }

    LaunchedEffect(userPersonal) {
        groceries.value = userPersonal.groceryList.toMutableMap()
    }

    if (loading.value) {
        LoadingPage()
    } else {
        PrimaryScreen(
            navigationActions = navigationActions,
            title = stringResource(R.string.title_groceries),
            navigationIndex = 1,
            topBarIcons = { OptionsMenu(R.drawable.options,
                stringResource(R.string.button_clearList) to { showDeleteAlert.value = true },
                stringResource(R.string.button_sendToFridge) to {
                    loading.value = true
                    userViewModel.groceriesToFridge({
                        if (it) {
                            handleError(context, "Failed to send grocery items to fridge")
                            loading.value = false
                        }
                    }) {
                        userViewModel.fetchUserPersonal({
                            if (it) {
                                handleError(context, "Could not fetch user personal")
                                loading.value = false
                            }
                        }) {
                            loading.value = false
                        }
                    }
                }
            ) },
            userViewModel = userViewModel,
            floatingButton = { FloatingButton(screenState) {
                loading.value = true
                loadModifications(userViewModel, userPersonal, groceries, {it.groceryList}, false, context, loading, newItems, removedItems, editedCategories, newCategories, removedCategories)
            }},
            content = { paddingValues ->
                when (screenState.value) {
                    ScreenState.LOADING -> {
                        MiniLoading(paddingValues)
                    }

                    ScreenState.VIEWING -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            clearTemporaryModifications(userPersonal, groceries, {it.groceryList}, newItems, removedItems, editedCategories, newCategories, removedCategories, unavailableCategoryNames)

                            if (groceries.value.isNotEmpty() && !groceries.value.all { it.value.isEmpty() }) {
                                items(groceries.value.toSortedMap().keys.toList(), key = {it}) { category ->
                                    if (groceries.value[category]?.isNotEmpty() == true) {
                                        IngredientCategoryView(category, groceries.value[category] ?: mutableListOf(), true) { ingredient, isTicked ->
                                            userViewModel.updateIngredientTick(ingredient.uid, isTicked, {
                                                if (it) { handleError(context, "Could not update ingredient tick") }
                                            }) {}
                                        }
                                    }
                                }
                            } else {
                                item {
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp),
                                        text = stringResource(R.string.txt_emptyGroceries),
                                        style = MyTypography.bodyLarge,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                    ScreenState.EDITING -> {
                        LazyColumn(
                            Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            newCategoryName.value = ""
                            item {
                                AddCategory(newCategoryName, newCategories, unavailableCategoryNames, context)
                            }

                            // new categories are displayed first, in added order, to make editing easier
                            items(newCategories.value.keys.reversed().toList(), key = {it})  { category ->
                                IngredientCategoryEdit(
                                    category,
                                    newCategories.value[category] ?: mutableListOf(),
                                    true,
                                    newCategories.value[category] ?: mutableListOf(),
                                    editedCategories,
                                    onRemoveItem = { _, name ->
                                        newCategories.value[category]?.removeIf { ingredient ->
                                            ingredient.displayedName == name
                                        }
                                    },
                                    onRemoveCategory = {
                                        val mutableNewCategories = newCategories.value.toMutableMap()
                                        mutableNewCategories.remove(it)
                                        newCategories.value = mutableNewCategories
                                        unavailableCategoryNames.remove(it)
                                    },
                                    context,
                                    userViewModel
                                )
                            }

                            // existing categories are display next in alphabetical order
                            items(groceries.value.toSortedMap().keys.toList(), key = {it}) { category ->
                                if (category !in removedCategories) {
                                    IngredientCategoryEdit(
                                        category,
                                        groceries.value[category] ?: mutableListOf(),
                                        true,
                                        newItems[category] ?: mutableListOf(),
                                        editedCategories,
                                        onRemoveItem = { uid, _ ->
                                            removedItems[category]?.add(uid)
                                        },
                                        onRemoveCategory = {
                                            deletingCategory = it
                                            showCategoryAlert.value = true
                                        },
                                        context,
                                        userViewModel
                                    )
                                }
                            }
                            // spacing for the keyboard (cuz doing things properly with ime paddings fucks things up)
                            item {
                                Spacer(modifier = Modifier
                                    .fillMaxWidth()
                                    .height(
                                        (WindowInsets.ime
                                            .asPaddingValues()
                                            .calculateBottomPadding()
                                                - paddingValues.calculateBottomPadding())
                                            .coerceAtLeast(0.dp)
                                    ))
                            }
                        }

                    }
                }
                if (showCategoryAlert.value) {
                    DialogWindow(
                        visible = showCategoryAlert,
                        content = stringResource(R.string.alert_deleteCatGroceries),
                        confirmText = stringResource(R.string.button_delete),
                        confirmColour = Color.Red,
                        additionOnDismiss = { deletingCategory = "" }
                    ) {
                        removedCategories.add(deletingCategory)
                        groceries.value = groceries.value.filterKeys { key -> key !in removedCategories }
                        unavailableCategoryNames.remove(deletingCategory)
                        deletingCategory = ""
                        showCategoryAlert.value = false
                    }
                }
                if (showDeleteAlert.value) {
                    DialogWindow(
                        visible = showDeleteAlert,
                        content = stringResource(R.string.alert_clearList),
                        confirmText = stringResource(R.string.button_delete),
                        confirmColour = Color.Red
                    ) {
                        showDeleteAlert.value = false
                        loading.value = true
                        userViewModel.clearIngredients(false, {
                            if (it) {
                                handleError(context, "Failed to clear fridge")
                                loading.value = false
                            }
                        }) {
                            userViewModel.fetchUserPersonal({
                                if (it) {
                                    handleError(context, "Could not fetch user personal")
                                    loading.value = false
                                }
                            }) {
                                loading.value = false
                            }
                        }
                    }
                }
            }
        )
    }
}
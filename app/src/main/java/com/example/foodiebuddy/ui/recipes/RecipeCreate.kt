package com.example.foodiebuddy.ui.recipes

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.foodiebuddy.R
import com.example.foodiebuddy.data.Diet
import com.example.foodiebuddy.data.Origin
import com.example.foodiebuddy.data.RecipeIngredient
import com.example.foodiebuddy.data.Tag
import com.example.foodiebuddy.data.getString
import com.example.foodiebuddy.errors.handleError
import com.example.foodiebuddy.navigation.NavigationActions
import com.example.foodiebuddy.navigation.Route
import com.example.foodiebuddy.viewModels.RecipeViewModel
import com.example.foodiebuddy.viewModels.UserViewModel
import kotlinx.coroutines.processNextEventInCurrentThread

@Composable
fun RecipeCreate(userVM: UserViewModel, recipeVM: RecipeViewModel, navigationActions: NavigationActions) {
    val context = LocalContext.current
    val editingPicture = remember { mutableStateOf(false) }
    val showPictureOptions = remember { mutableStateOf(false) }
    val showAlert = remember { mutableStateOf(false) }

    val userData by userVM.userData.collectAsState()
    val userID = userVM.getCurrentUserID()
    val username = remember { mutableStateOf("") }

    LaunchedEffect(userData) {
        username.value = userData.username
    }

    val nameState = remember { mutableStateOf("") }
    val currentPicture = remember { mutableStateOf(Uri.EMPTY) }
    val pictureState = remember { mutableStateOf(Uri.EMPTY) }
    val instructionsState = remember { mutableStateListOf("") }
    val ingredientsState = remember { mutableStateListOf<RecipeIngredient>() }
    val originState = remember { mutableStateOf(Origin.NONE) }
    val dietState = remember { mutableStateOf(Diet.NONE) }
    val tagsState = remember { mutableStateListOf<Tag>() }

    if (editingPicture.value) {
        SetRecipePicture(
            picture = pictureState.value,
            onCancel = {
                editingPicture.value = false
                pictureState.value = currentPicture.value
            },
            onSave = { uri ->
                pictureState.value = uri
                currentPicture.value = uri
                editingPicture.value = false
            })
        BackHandler {
            editingPicture.value = false
            pictureState.value = currentPicture.value
        }
    } else {
        EditRecipe(
            context = context,
            onGoBack = { showAlert.value = true },
            title = stringResource(R.string.title_createRecipe),
            name = nameState,
            picture = pictureState,
            instructions = instructionsState,
            ingredients = ingredientsState,
            origin = originState,
            diet = dietState,
            tags = tagsState,
            showPictureOptions = showPictureOptions,
            onEditPicture = { editingPicture.value = true },
            onRemovePicture = {
                pictureState.value = Uri.EMPTY
                currentPicture.value = Uri.EMPTY
            },
            onSave = {
                recipeVM.createRecipe(
                    userID, username.value, nameState.value, pictureState.value,
                    instructionsState, ingredientsState,
                    originState.value, dietState.value, tagsState,
                    { if (it) handleError(context, "Could not create recipe") }
                ) {
                    navigationActions.navigateTo("${Route.RECIPE}/${it}")
                }
            })
    }
}
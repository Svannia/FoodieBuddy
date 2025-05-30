package com.example.foodiebuddy.ui


import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.foodiebuddy.R
import com.example.foodiebuddy.navigation.BOTTOM_DESTINATIONS
import com.example.foodiebuddy.navigation.BURGER_DESTINATIONS
import com.example.foodiebuddy.navigation.NavigationActions
import com.example.foodiebuddy.ui.theme.MyTypography
import com.example.foodiebuddy.viewModels.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * This creates the layout for a "secondary screen".
 * It mainly contains a body and an invisible top bar with an optional title and a GoBack button.
 *
 * @param title display in the top bar (can be empty)
 * @param navigationActions to handle screen navigation
 * @param route optional Route to navigate to when pressing the Back button, instead of navigating back
 * @param navExtraActions optional extra block to run when navigating back (e.g navigating back from CreateAccount screen also signs out)
 * @param topBarIcons extra composable on the right-side of the top bar (optional)
 * @param content screen body
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryScreen(
    title: String,
    navigationActions: NavigationActions,
    route: String ?= null,
    navExtraActions: () -> Unit,
    topBarIcons: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Box {
                CenterAlignedTopAppBar(
                    title = { Text(text = title, style = MyTypography.titleMedium)},
                    navigationIcon = {
                        GoBackButton(navigationActions, navExtraActions, route)
                    },
                    actions = {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            topBarIcons()
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background, scrolledContainerColor = MaterialTheme.colorScheme.background)
                )}
        },
        content = { content(it) }
    )
}

/**
 * This creates the layout for a "primary screen".
 * It mainly contains a body and a larger top bar in a different colour.
 * There is a burger menu in the top bar that opens on the left-side.
 * There is a navigation bar at the bottom to change between the different primary screens.
 *
 * @param navigationActions to handle screen navigation
 * @param title display in the top bar
 * @param navigationIndex indicates which primary screen is currently selected (for bottom navigation bar)
 * @param topBarIcons composable for icons on the right-side of the top bar
 * @param userViewModel to access user data
 * @param floatingButton composable for the floating button
 * @param content screen body
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimaryScreen(
    navigationActions: NavigationActions,
    title: String,
    navigationIndex: Int,
    topBarIcons: @Composable () -> Unit,
    userViewModel: UserViewModel,
    floatingButton:  @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val userData by userViewModel.userData.collectAsState()
    val nameState = remember { mutableStateOf(userData.username) }
    val pictureState = remember { mutableStateOf(userData.picture) }

    LaunchedEffect(userData) {
        nameState.value = userData.username
        pictureState.value = userData.picture
    }

    // burger menu that opens on the side
    ModalNavigationDrawer(
        modifier = Modifier.fillMaxSize(),
        drawerState = drawerState,
        drawerContent = {
            // the opened menu contains some of the user's info, a Profile button and a Settings button
            ModalDrawerSheet(
                modifier = Modifier.requiredWidth(200.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.size(16.dp))
                    RoundImage(64.dp, pictureState.value, stringResource(R.string.desc_profilePic))
                    Text(text = nameState.value, style = MyTypography.bodyLarge)
                    Divider(color = MaterialTheme.colorScheme.outline, thickness = 3.dp)
                }
                BURGER_DESTINATIONS.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(text = stringResource(item.text), style = MyTypography.bodyLarge) },
                        selected = false,
                        onClick = {
                            navigationActions.navigateTo(item.route)
                            scope.launch { drawerState.close() }
                        },
                        icon = {
                            Icon(
                                modifier = Modifier.size(22.dp),
                                painter = painterResource(item.icon),
                                contentDescription = stringResource(R.string.desc_dstIcon)
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        // screen content
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Box {
                    // the top bar contains an icon to open the burger menu described above, the screen title and any additional top bar icons
                    CenterAlignedTopAppBar(
                        title = { Text(text = title, style = MyTypography.titleMedium) },
                        navigationIcon = { BurgerMenu(scope, drawerState) },
                        actions = { topBarIcons() }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline, thickness = 3.dp, modifier = Modifier.align(Alignment.BottomStart))
                }

            },
            bottomBar = { BottomNavBar(navigationActions, navigationIndex) },
            floatingActionButton = floatingButton,
            content = { content(it) }
        )
    }

}

/**
 * This creates a column with a small loading animation that can be used as a screen content.
 *
 * @param paddingValues to be used in the main column
 */
@Composable
fun MiniLoading(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.size(16.dp))
        LoadingAnimation(30f, 10f)
    }
}

/**
 * A simple plain screen with a rotating loading animation.
 */
@Composable
fun LoadingPage() {
    // ensures that the user cannot go back while on the loading page.
    BackHandler {}
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoadingAnimation(100f, 10f)
    }
}

/**
 * A rotating animation to be used for when waiting for information to load/change.
 *
 * @param size diameter of the loading circle
 * @param strokeWidth width of the circle
 */
@Composable
fun LoadingAnimation(size: Float, strokeWidth: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = stringResource(R.string.desc_loading))
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing)
        ), label = stringResource(R.string.desc_loading)
    )

    val primaryColour = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.size((size).dp)) {
        drawArc(
            color = primaryColour,
            startAngle = angle,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

/**
 * Crops an image into a disk (e.g for profile pictures).
 *
 * @param size diameter of the round image
 * @param picture Uri of the picture to be cropped
 * @param contentDescription image description
 */
@Composable
fun RoundImage(size: Dp, picture: Uri, contentDescription: String) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.Transparent)
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = rememberAsyncImagePainter(picture),
            contentDescription = contentDescription,
            contentScale = ContentScale.FillBounds
        )
    }
}

/**
 * Crops an image into a square (e.g for recipe pictures).
 *
 * @param size length of the square image
 * @param picture Uri of the picture to be cropped
 * @param contentDescription image description
 */
@Composable
fun SquareImage(size: Dp, picture: Uri, contentDescription: String) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RectangleShape)
            .background(Color.Transparent)
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = rememberAsyncImagePainter(picture),
            contentDescription = contentDescription,
            contentScale = ContentScale.FillBounds
        )
    }
}

/**
 * Crops an image square and adds an icon over-layered on the top right corner.
 *
 * @param icon identifier for the icon to be used as the IconButton
 * @param iconColor colour of the icon
 * @param pictureSize length of the square image
 * @param picture URI of the picture to be cropped
 * @param contentDescription image description
 * @param onIconClick block that runs when the icon is pressed
 */
@Composable
fun IconSquareImage(icon: Int, iconColor: Color, pictureSize: Dp, picture: Uri, contentDescription: String, onIconClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(pictureSize)
            .clip(RectangleShape)
            .background(Color.Transparent)
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = rememberAsyncImagePainter(picture),
            contentDescription = contentDescription,
            contentScale = ContentScale.FillBounds
        )
        Icon(
            painter = painterResource(id = icon),
            contentDescription = stringResource(R.string.desc_imageIcon),
            modifier = Modifier
                .size(pictureSize / 3.5f)
                .align(Alignment.TopEnd)
                .clickable { onIconClick() }
                .padding(4.dp),
            tint = iconColor
        )
    }
}

/**
 * Creates a square image that can be selected with visual effects similar as usual Gallery apps.
 *
 * @param isSelected is the image initially selected or not
 * @param pictureSize length of the square image
 * @param picture URI of the picture to be cropped
 * @param contentDescription image description
 * @param onClick block that runs when (un)selecting the image
 */
@Composable
fun SelectableSquareImage(isSelected: Boolean, pictureSize: Dp, picture: Uri, contentDescription: String, onClick: (Boolean) -> Unit) {
    val selected = remember { mutableStateOf(isSelected) }

    Box(
        modifier = Modifier
            .size(pictureSize)
            .clip(RectangleShape)
            .background(Color.Transparent)
            .clickable {
                selected.value = !selected.value
                onClick(selected.value)
            }
    ) {
        // image itself
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = rememberAsyncImagePainter(picture),
            contentDescription = contentDescription,
            contentScale = ContentScale.FillBounds
        )
        // white cast when it is selected
        if (selected.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.4f))
            )
        }
        // round checkbox
        Box(
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .clip(CircleShape)
                .background(
                    if (selected.value) MaterialTheme.colorScheme.primary else Color.Black.copy(
                        alpha = 0.4f
                    )
                )
                .border(
                    width = if (selected.value) 0.dp else 2.dp,
                    color = Color.White,
                    shape = CircleShape
                )
                .clickable {
                    selected.value = !selected.value
                    onClick(selected.value)
                },
            contentAlignment = Alignment.Center
        ) {
            if (selected.value) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(R.drawable.tick),
                    contentDescription = stringResource(R.string.desc_add),
                    tint = Color.Black
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
/**
 * Creates a pager to swipe horizontally swipe between multiple pictures. Also features a pager indicator at the bottom.
 *
 * @param pictures list of the pictures that can be swiped through
 */
fun ImagePager(pictures: List<Uri>) {
    val pagerState = rememberPagerState(pageCount = { pictures.size })

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RectangleShape)
            .background(Color.Transparent),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) { page ->
            SquareImage(
                    size = LocalConfiguration.current.screenWidthDp.dp,
                    picture = pictures[page],
                    contentDescription = stringResource(R.string.desc_recipePicture)
                )
            }
        // pager indicator
        if (pictures.size > 1) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pictures.size) { index ->
                    val colour = if (index == pagerState.currentPage) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(colour)
                    )
                }
            }
        }
    }
}

/**
 * Rewritten basic TextField composable for constant design throughout the app.
 * Always use this instead of the normal TextField.
 *
 * @param value text passed by the user in the text field
 * @param onValueChange block that runs with the new input value when it is edited
 * @param icon display at the beginning of the text field (use a negative int for no icon)
 * @param placeHolder text displayed in the empty text field
 * @param singleLine whether or not the value of the text field can contain line breaks
 * @param maxLength maximum amount of characters allowed in the text field
 * @param autoCap whether or not to activate the AutoCap on the keyboard when starting to type. True by default
 * @param focusRequester optional FocusRequester when the TextField needs to be manually put into focus
 * @param onFocusedChanged needed if there is a FocusRequester: block that runs when focus is changed
 * @param showMaxChara whether or not to show supporting text with the max amount of character. True by default
 * @param width width of the TextField
 * @param height optional height for the TextField, usually used for writing big blocks of text
 * @param keyboardActions optional overriding of default keyboard actions
 * @param keyboardOptions optional overriding of default keyboard options
 */
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    icon: Int,
    placeHolder: String,
    singleLine: Boolean,
    maxLength: Int,
    autoCap: Boolean = true,
    focusRequester: FocusRequester = FocusRequester.Default,
    onFocusedChanged: (FocusState) -> Unit = {},
    showMaxChara: Boolean = true,
    width: Dp,
    height: Dp? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    TextField(
        modifier = if (singleLine) {
            Modifier
                .width(width)
                .padding(0.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { onFocusedChanged(it) }
        } else {
            Modifier
                .width(width)
                .height(height!!)
                .padding(0.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { onFocusedChanged(it) }
        },
        value = value,
        onValueChange = {
            if (it.length <= maxLength) {
                onValueChange(it)
            }
        },
        textStyle = MyTypography.bodyLarge,
        prefix = {
            if (icon >= 0) {
                Row{
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = stringResource(R.string.desc_textFieldIcon),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                }
            }
        },
        placeholder = {
            Text(text = placeHolder, style = MyTypography.bodySmall)
        },
        singleLine = singleLine,
        supportingText = {
            if (showMaxChara) {
                Text(text = stringResource(R.string.field_maxChar, maxLength), style = MyTypography.labelSmall)
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary
        ),
        keyboardActions = keyboardActions,
        keyboardOptions = keyboardOptions.copy(
            capitalization = if (autoCap) KeyboardCapitalization.Sentences
                else KeyboardCapitalization.None
        )
    )
}

/**
 * TextField that only accepts numerical value.
 *
 * @param value number passed by the user in the TextField, can be float or integer
 * @param onValueChange block that runs with the new input when it is edited
 * @param isInteger whether the input should be treated as integer or float
 * @param placeHolder text displayed in the empty TextField
 * @param width width of the TextField
 */
@Composable
fun CustomNumberField(
    value: Number,
    onValueChange: (Number) -> Unit,
    isInteger: Boolean,
    placeHolder: String,
    width: Dp
) {
    val text = remember { mutableStateOf(value.toString()) }

    TextField(
        modifier = Modifier
            .width(width)
            .padding(0.dp),
        value = if (value.toFloat() < 0) placeHolder
                else text.value,
        onValueChange = { input ->
            val filteredInput = if (isInteger) input.filter { it.isDigit() }
                                else input.filter { it.isDigit() || it == '.' }
            text.value = filteredInput
            if (isInteger) {
                if (input.isBlank()) onValueChange(-1)
                else filteredInput.toIntOrNull()?.let { onValueChange(it) }
            }
            else filteredInput.toFloatOrNull()?.let{ onValueChange(it) }
        },
        textStyle = MyTypography.bodyLarge,
        placeholder = {
            Text(text = placeHolder, style = MyTypography.bodySmall)
        },
        singleLine = true,
        supportingText = {},
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isInteger) KeyboardType.Number else KeyboardType.Decimal,
            imeAction = ImeAction.Done
        )
    )
}


/**
 * Creates a dialog window that can pop and be dismissed.
 * Warning: always call this function after all other composable elements in code, so that it appears on top of the screen.
 *
 * @param visible whether or not this window should be visible
 * @param content text inside the window
 * @param confirmText text within the confirm button
 * @param confirmColour colour of the confirm text and button
 * @param additionOnDismiss optional block that runs when dismissing the dialog window
 * @param onConfirm block that runs if the confirm button is pressed
 */
@Composable
fun DialogWindow(
    visible: MutableState<Boolean>,
    content: String,
    confirmText: String,
    confirmColour: Color,
    additionOnDismiss: () -> Unit = {},
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            visible.value = false
            additionOnDismiss()
        },
        text = {
            Text(
                text = content,
                style = MyTypography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(
                modifier = Modifier
                    .border(
                        width = 2.dp,
                        color = confirmColour,
                        shape = RoundedCornerShape(50)
                    )
                    .background(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(50)
                    ),
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = confirmText,
                    style = MyTypography.bodyLarge,
                    color = confirmColour
                )
            }
        },
        dismissButton = {
            TextButton(
                modifier = Modifier
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.inversePrimary,
                        shape = RoundedCornerShape(50)
                    )
                    .background(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(50)
                    ),
                onClick = {
                    visible.value = false
                    additionOnDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = stringResource(R.string.button_cancel),
                    style = MyTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.inversePrimary
                )
            }
        }
    )
}

/**
 * Creates a dialog window that can pop and be dismissed; it contains a long field to input text.
 * Warning: always call this function after all other composable elements in code, so that it appears on top of the screen.
 *
 * @param visible whether or not this window should be visible
 * @param confirmText text within the confirm button
 * @param confirmColour colour of the confirm text and button
 * @param onConfirm block that runs if the confirm button is pressed
 * @param content content of the dialog window
 */
@Composable
fun InputDialogWindow(
    visible: MutableState<Boolean>,
    confirmText: String,
    confirmColour: Color,
    onConfirm: () -> Unit,
    content: @Composable (() -> Unit)
) {
    AlertDialog(
        onDismissRequest = { visible.value = false },
        text = content,
        confirmButton = {
            TextButton(
                modifier = Modifier
                    .border(
                        width = 2.dp,
                        color = confirmColour,
                        shape = RoundedCornerShape(50)
                    )
                    .background(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(50)
                    ),
                onClick = { onConfirm() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = confirmText,
                    style = MyTypography.bodyLarge,
                    color = confirmColour
                )
            }
        },
        dismissButton = {
            TextButton(
                modifier = Modifier
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.inversePrimary,
                        shape = RoundedCornerShape(50)
                    )
                    .background(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(50)
                    ),
                onClick = { visible.value = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = stringResource(R.string.button_cancel),
                    style = MyTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.inversePrimary
                )
            }
        }
    )
}

/**
 * Element that creates an icon button that opens a drop-down menu of options when pressed.
 *
 * @param icon identifier for the icon to be used as the IconButton
 * @param options non-exhaustive number of pairs.
 * Each pair contains a string for the name of the action appearing in the drop-down menu,
 * and a block to run when that button is pressed.
 */
@Composable
fun OptionsMenu(icon: Int, vararg options: Pair<String, () -> Unit>) {
    val menuExpanded = remember { mutableStateOf(false) }

    Row{
        IconButton(
            onClick = { menuExpanded.value = !menuExpanded.value }
        ) {
            Icon(
                painter = painterResource(icon),
                modifier = Modifier.size(28.dp),
                contentDescription = stringResource(R.string.button_options)
            )
        }
        DropdownMenu(
            expanded = menuExpanded.value,
            onDismissRequest = { menuExpanded.value = false }
        ) {
            for ((text, block) in options) {
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        menuExpanded.value = false
                        block()
                    }
                )
            }
        }
    }
}

/**
 * A button used to navigate back in the screens navigation history.
 *
 * @param navigationActions to handle screen navigation
 * @param navExtraActions optional extra block to run when navigating back (e.g navigating back from CreateAccount screen also signs out)
 * @param route optional route to navigate to instead of navigating back
 */
@Composable
fun GoBackButton(navigationActions: NavigationActions, navExtraActions: () -> Unit, route: String ?= null) {
    IconButton(
        onClick = {
            if (route != null) navigationActions.navigateTo(route)
            else navigationActions.goBack()
            navExtraActions()
        }
    ) {
        Icon(
            painter = painterResource(R.drawable.go_back),
            contentDescription = stringResource(R.string.desc_goBack)
        )
    }
}

/**
 * Icon that handles the burger menu.
 *
 * @param scope needed to launch a coroutine to open the burger menu
 * @param drawerState value that determines if the burger menu is opened or closed
 */
@Composable
private fun BurgerMenu(scope: CoroutineScope, drawerState: DrawerState) {
    IconButton(
        onClick = { scope.launch { drawerState.open() }}
    ) {
        Icon(
            painter = painterResource(R.drawable.burger_menu),
            contentDescription = stringResource(R.string.desc_burgerMenu),
            modifier = Modifier.size(28.dp)
        )
    }
}

/**
 * This creates the navigation bar at the bottom of the main screens.
 *
 * @param navigationActions to handle screen navigation
 * @param navigationIndex from the BOTTOM_NAVIGATIONS list, index of the current destination
 */
@Composable
private fun BottomNavBar(
    navigationActions: NavigationActions,
    navigationIndex: Int
) {
    val destinations = BOTTOM_DESTINATIONS
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(navigationIndex) }

    NavigationBar(
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(90.dp)
    ) {
        destinations.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItemIndex == index,
                onClick = {
                    navigationActions.navigateTo(item.route)
                    selectedItemIndex = index
                },
                icon = {
                    Icon(
                        modifier = Modifier.size(28.dp),
                        painter = painterResource(item.icon),
                        contentDescription = stringResource(item.text),
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.text),
                        style = MyTypography.bodyLarge
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}


package com.example.foodiebuddy.ui.account

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.exifinterface.media.ExifInterface
import coil.compose.rememberAsyncImagePainter
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.jpeg.JpegDirectory
import com.example.foodiebuddy.R
import com.example.foodiebuddy.errors.handleError
import com.example.foodiebuddy.navigation.NavigationActions
import com.example.foodiebuddy.system.checkPermission
import com.example.foodiebuddy.system.imagePermissionVersion
import com.example.foodiebuddy.ui.CustomTextField
import com.example.foodiebuddy.ui.RoundImage
import com.example.foodiebuddy.ui.SecondaryScreen
import com.example.foodiebuddy.ui.theme.MyTypography
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

@Composable
fun EditAccount(
    context: Context,
    navigationActions: NavigationActions,
    navExtraActions: () -> Unit,
    name: MutableState<String>,
    picture: MutableState<Uri>,
    bio: MutableState<String>,
    dataEdited: MutableState<Boolean> ?= null,
    onEditPicture: () -> Unit,
    onSave: () -> Unit
) {
    // getting image and image permissions
    val imageInput = "image/*"
    val getPicture = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {pictureUri ->
            picture.value = pictureUri
            onEditPicture()
        }
    }
    val imagePermission = imagePermissionVersion()
    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getPicture.launch(imageInput)
            }
        }

    SecondaryScreen(
        title = "",
        navigationActions = navigationActions,
        navExtraActions = { navExtraActions() },
        topBarIcons = {},
        content = { paddingValue ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValue)
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { RoundImage(100.dp, picture.value, stringResource(R.string.desc_profilePic)) }
                item {
                    Text(
                        modifier = Modifier.clickable {
                            checkPermission(context, imagePermission, requestPermissionLauncher) {
                                getPicture.launch(imageInput)
                            }
                        },
                        text = if (dataEdited != null) stringResource(R.string.button_modifyProfilePicture) else {stringResource(R.string.button_addProfilePicture)},
                        style = MyTypography.labelMedium
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                }
                item {
                    CustomTextField(
                        value = name.value,
                        onValueChange = {
                            if (dataEdited != null) { dataEdited.value = true }
                            name.value = it
                        },
                        icon = R.drawable.user,
                        placeHolder = stringResource(R.string.field_username),
                        singleLine = true,
                        maxLength = 15
                    )
                }
                item {
                    CustomTextField(
                        value = bio.value,
                        onValueChange = {
                            if (dataEdited != null) { dataEdited.value = true }
                            bio.value = it
                        },
                        icon = R.drawable.pencil,
                        placeHolder = stringResource(R.string.field_bio),
                        singleLine = false,
                        maxLength = 150
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                }
                item {
                    val isEnabled = dataEdited?.value ?: true
                    SaveButton(name.value.isNotEmpty() && isEnabled) { onSave() }
                }
            }

        }
    )
}

@Composable
private fun SaveButton(isEnabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        modifier = Modifier.width(300.dp),
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(stringResource(R.string.button_save), style = MyTypography.bodyMedium)
    }
}

fun deleteAuthentication(context: Context) {
    val user = FirebaseAuth.getInstance().currentUser
    user?.delete()
        ?.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("Login", "Successfully deleted authenticated user")
            } else {
                handleError(context, "Could not delete authenticated user")
            }
        }
}

fun signOut(context: Context) {
    AuthUI.getInstance().signOut(context).addOnCompleteListener{
        if (it.isSuccessful) {
            Log.d("Login", "Successfully signed out")
        } else {
            handleError(context, "Could not delete sign out user")
        }
    }
}

@Composable
fun SetProfilePicture(picture: Uri, onCancel: () -> Unit, onSave: (Uri) -> Unit) {
    val context = LocalContext.current

    val density = LocalDensity.current
    val screenWidth = LocalConfiguration.current.screenWidthDp * density.density
    val screenHeight = LocalConfiguration.current.screenHeightDp * density.density
    val radius = minOf(screenWidth, screenHeight) / 2
    val imageInfo = computeMinScale(LocalContext.current, picture, radius, screenWidth, screenHeight)

    var scale by remember { mutableFloatStateOf(imageInfo.minScale) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var imageWidth by remember { mutableFloatStateOf(imageInfo.width * imageInfo.minScale) }
    var imageHeight by remember { mutableFloatStateOf(imageInfo.height * imageInfo.minScale) }



    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceAtLeast(imageInfo.minScale)

                        imageWidth = imageInfo.width * scale
                        imageHeight = imageInfo.height * scale

                        val maxOffsetX = abs(imageWidth / 2 - radius)
                        val maxOffsetY = abs(imageHeight / 2 - radius)

                        offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                        offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                    }
                }
        ) {
            Image(
                painter = rememberAsyncImagePainter(picture),
                contentDescription = stringResource(R.string.desc_profilePic),
                contentScale = ContentScale.None,
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .fillMaxSize()
            )
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val circleMask = Path().apply {
                    addOval(Rect(center, radius))
                }
                clipPath(circleMask, clipOp = ClipOp.Difference){
                    drawRect(color = Color.Black.copy(alpha = 0.6f), size = size)
                }
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.button_cancel),
                    style = MyTypography.bodySmall,
                    modifier = Modifier.clickable {
                        onCancel()
                    }
                )
                Text(
                    text = stringResource(R.string.button_save),
                    style = MyTypography.bodySmall,
                    modifier = Modifier.clickable {
                        val croppedBitmap = cropImage(context, picture, imageWidth, imageHeight, offsetX, offsetY, radius, imageInfo.orientation)
                        croppedBitmap?.let {
                            val croppedBitmapUri = saveBitmapToFile(context, it)
                            croppedBitmapUri?.let { uri ->
                                onSave(uri)
                            }
                        }
                    }
                )
            }
        }
    }
}

private fun computeMinScale(context: Context, picture: Uri, radius: Float, screenWidth: Float, screenHeight: Float): ImageInfo {
    return try {
        val inputStream = context.contentResolver.openInputStream(picture)

        val metadata = ImageMetadataReader.readMetadata(inputStream)
        val jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory::class.java)
        var imageHeight = jpegDirectory?.getString(JpegDirectory.TAG_IMAGE_HEIGHT)?.toFloat() ?: 0f
        var imageWidth = jpegDirectory?.getString(JpegDirectory.TAG_IMAGE_WIDTH)?.toFloat() ?: 0f

        val exifDirectory = metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java)
            val orientation = exifDirectory?.getString(ExifIFD0Directory.TAG_ORIENTATION)?.toInt()
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            val temp = imageHeight
            imageHeight = imageWidth
            imageWidth = temp
        }
            val diameter = radius * 2

        while (imageWidth > screenWidth || imageHeight > screenHeight) {
            if (imageWidth > screenWidth) {
                val ratio = screenWidth / imageWidth
                imageWidth = screenWidth
                imageHeight *= ratio
            } else  {
                val ratio = screenHeight / imageHeight
                imageHeight = screenHeight
                imageWidth *= ratio
            }
        }

            val widthRatio = diameter / imageWidth
            val heightRatio = diameter / imageHeight
            val minScale = maxOf(widthRatio, heightRatio)
            ImageInfo(minScale, imageWidth, imageHeight, orientation)
    } catch (e: Exception) {
        Log.d("Error", "Failed to calculate initial scale with $e")
        ImageInfo(1.5f, 0f, 0f, ExifInterface.ORIENTATION_NORMAL)
    }
}

data class ImageInfo(val minScale: Float, val width: Float, val height: Float, val orientation: Int?)

private fun cropImage(context: Context, picture: Uri, imageWidth: Float, imageHeight: Float, offsetX: Float, offsetY: Float, radius: Float, orientation: Int?): Bitmap? {
    val inputStream = context.contentResolver.openInputStream(picture)
    val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null

    val correctedBitmap = if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
        val matrix = Matrix()
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            matrix.setRotate(90f)
        } else {
            matrix.setRotate(270f)
        }
        val orientedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
        Bitmap.createScaledBitmap(orientedBitmap, (imageWidth).toInt(), (imageHeight).toInt(), true)
    } else {
        Bitmap.createScaledBitmap(originalBitmap, (imageWidth).toInt(), (imageHeight).toInt(), true)
    }
    val centerX = correctedBitmap.width / 2 - offsetX
    val centerY = correctedBitmap.height / 2 - offsetY

    val cropLeft = (centerX - radius).toInt()
    val cropTop = (centerY - radius).toInt()
    val cropRight = (centerX + radius).toInt()
    val cropBottom = (centerY + radius).toInt()

    val cropWidth = cropRight - cropLeft
    val cropHeight = cropBottom - cropTop
    val diameter = radius * 2

    val croppedBitmap = Bitmap.createBitmap(correctedBitmap, cropLeft, cropTop, cropWidth, cropHeight)
    val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, diameter.toInt(), diameter.toInt(), true)

    return scaledBitmap
}

private fun saveBitmapToFile(context: Context, bitmap: Bitmap): Uri? {
    val file = File(context.cacheDir, "cropped_profile_picture.jpg")
    return try {
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
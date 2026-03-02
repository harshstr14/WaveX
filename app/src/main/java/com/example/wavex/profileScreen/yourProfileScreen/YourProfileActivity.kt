package com.example.wavex.profileScreen.yourProfileScreen

import android.app.Activity
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.example.wavex.R
import com.example.wavex.fonts
import com.example.wavex.homeScreen.viewModel.ProfileViewModel
import com.example.wavex.ui.theme.WaveXTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import java.io.File

class YourProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                darkScrim = 0xFF121212.toInt(),
                scrim = 0xFFF6F6F6.toInt()
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = 0xFFF6F6F6.toInt()
            )
        )

        setContent {
            WaveXTheme {
                Your_Profile_Activity()
            }
        }
    }
}

@Composable
fun Your_Profile_Activity() {
    val viewModel: ProfileViewModel = viewModel()

    val imageUrl by viewModel.profileImageUrl.collectAsStateWithLifecycle()
    val name by viewModel.userName.collectAsState()
    var mail by remember { mutableStateOf("") }
    var phoneNo by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    val uid = FirebaseAuth.getInstance().currentUser?.uid

    val database: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("Users")

    LaunchedEffect(uid) {
        uid?.let {
            database.child(it).get().addOnSuccessListener { snapshot ->
                mail = snapshot.child("mail").getValue(String::class.java) ?: ""
                phoneNo = snapshot.child("phoneNo").getValue(String::class.java) ?: ""
                gender = snapshot.child("gender").getValue(String::class.java) ?: ""
            }
        }
    }

    YourProfileScreen(
        viewModel = viewModel,
        imageUrl = imageUrl,
        savedName = name,
        savedEmail = mail,
        savedPhoneNO = phoneNo,
        savedGender = gender,
        onUpdateClick = { updatedName, updatedPhone, updatedGender, showMessage ->

            uid?.let { userId ->
                val updates = mutableMapOf<String, Any>()

                updatedName.takeIf { it.isNotBlank() }?.let { updates["name"] = it }
                updatedPhone.takeIf { it.isNotBlank() }?.let { updates["phoneNo"] = it }
                updatedGender.takeIf { it.isNotBlank() }?.let { updates["gender"] = it }

                if (updates.isNotEmpty()) {
                    database.child(userId)
                        .updateChildren(updates)
                        .addOnSuccessListener {
                            showMessage("Profile updated successfully")
                        }
                        .addOnFailureListener {
                            showMessage("Failed to update profile")
                        }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YourProfileScreen(
    viewModel: ProfileViewModel,
    imageUrl: String?,
    savedName: String,
    savedEmail: String,
    savedPhoneNO: String,
    savedGender: String,
    onUpdateClick: (String, String, String, (String) -> Unit) -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var name by remember { mutableStateOf("") }
    var phoneNo by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var isPhoneEditable by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var expanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Male", "Female", "Other")

    val isUploading by viewModel.isUploading.collectAsState()

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 270f else 90f,
        animationSpec = tween(durationMillis = 300),
        label = "ArrowRotation"
    )

    LaunchedEffect(savedName) {
        name = savedName
    }
    LaunchedEffect(savedEmail) {
        email = savedEmail
    }
    LaunchedEffect(savedPhoneNO) {
        phoneNo = savedPhoneNO
    }
    LaunchedEffect(savedGender) {
        gender = savedGender
    }

    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ShareScale"
    )

    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val resultUri = UCrop.getOutput(result.data!!)
            resultUri?.let { uri ->
                // Upload to Cloudinary and save URL
                val database = FirebaseDatabase.getInstance().getReference("Users")
                uploadToCloudinary(uri, database,
                    viewModel,
                    onShowMessage = { message ->
                        scope.launch {
                            snackBarHostState.showSnackbar(
                                message = message,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                )
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { sourceUri ->
            val destinationUri = Uri.fromFile(
                File(context.cacheDir, "crop_${System.currentTimeMillis()}.jpg")
            )
            val options = UCrop.Options().apply {
                setCircleDimmedLayer(true)
                setShowCropFrame(false)
                setShowCropGrid(false)
            }
            val intent = UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(512, 512)
                .withOptions(options)
                .getIntent(context)
            cropLauncher.launch(intent)
        }
    }

    val shouldBlur = isUploading

    val animatedBlur by animateFloatAsState(
        targetValue = if (shouldBlur) 25f else 0f,
        label = ""
    )

    var shadowColor by remember { mutableStateOf(Color(0xFFF6F6F6)) }
    var isScrollingDown by remember { mutableStateOf(false) }

    val shadowAlpha by animateFloatAsState(
        targetValue = if (isScrollingDown) 0f else 0.8f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "ShadowAlpha"
    )

    val shadowBlur by animateFloatAsState(
        targetValue = if (isScrollingDown) 0f else 50f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "ShadowBlur"
    )

    val shadowScale by animateFloatAsState(
        targetValue = if (isScrollingDown) 0.8f else 1f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "ShadowScale"
    )

    Scaffold(
        modifier = Modifier.background(colorResource(R.color.background_color)),
        topBar = {
            CenterAlignedTopAppBar(
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    Box(
                        modifier = Modifier.padding(start = 20.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(
                                width = 1.5.dp,
                                color = colorResource(R.color.secondary_text_color).copy(alpha = 0.6f),
                                shape = RoundedCornerShape(20.dp)
                            ).clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                activity?.finish()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_icon),
                            contentDescription = "Back Icon",
                            tint = colorResource(R.color.primary_text_color),
                            modifier = Modifier.size(20.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                        )
                    }
                },
                title = {
                    Text(
                        text = "Your Profile",
                        fontSize = 20.sp,
                        fontFamily = fonts,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.primary_text_color),
                        lineHeight = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.background_color),
                    scrolledContainerColor = colorResource(R.color.background_color)
                )
            )
        },
        snackbarHost = {
            SnackbarHost(
                snackBarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 25.dp)
            ) { data ->
                Snackbar(
                    modifier = Modifier.fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(10.dp),
                            ambientColor = Color(0xFF2C2C2C),
                            spotColor = Color(0xFF2C2C2C)
                        ),
                    containerColor = Color(0xFF2C2C2C),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painter = painterResource(when {
                            data.visuals.message.contains("Profile") -> R.drawable.user_icon
                            else -> {
                                R.drawable.alert_icon
                            }
                        } ), contentDescription = "Icons",
                            tint = colorResource(R.color.theme_color), modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = data.visuals.message,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            fontSize = 13.sp,
                            color = colorResource(R.color.off_white)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colorResource(R.color.background_color))
                .graphicsLayer {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && animatedBlur > 0f) {
                        renderEffect = RenderEffect
                            .createBlurEffect(
                                animatedBlur,
                                animatedBlur,
                                Shader.TileMode.CLAMP
                            )
                            .asComposeRenderEffect()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (profileImageRef, formContainerRef, editProfileImageRef, updateProfileButtonRef) = createRefs()

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .allowHardware(false)
                        .build(),
                    contentDescription = "Profile Image",
                    onSuccess = { result ->
                        val drawable = result.result.drawable
                        val bitmap = (drawable as? BitmapDrawable)?.bitmap ?: return@AsyncImage

                        Palette.from(bitmap).generate { palette ->
                            palette?.dominantSwatch?.rgb?.let { colorInt ->
                                shadowColor = Color(colorInt)
                            }
                        }
                    },
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.constrainAs(profileImageRef) {
                        top.linkTo(parent.top, margin = 15.dp)
                        end.linkTo(parent.end)
                        start.linkTo(parent.start)
                    }.padding(top = 10.dp).size(162.dp)
                        .drawBehind {
                            val glowRadius = (size.minDimension / 2) * shadowScale
                            val safeBlur = shadowBlur.coerceAtLeast(0.1f)

                            drawIntoCanvas { canvas ->
                                val paint = Paint().apply {
                                    color = shadowColor.copy(alpha = shadowAlpha)
                                    asFrameworkPaint().apply {
                                        isAntiAlias = true

                                        maskFilter = if (shadowBlur > 0f) {
                                            android.graphics.BlurMaskFilter(
                                                safeBlur,
                                                android.graphics.BlurMaskFilter.Blur.NORMAL
                                            )
                                        } else {
                                            null
                                        }
                                    }
                                }

                                canvas.drawCircle(
                                    center,
                                    glowRadius,
                                    paint
                                )
                            }
                        }
                        .clip(CircleShape)
                )

                Box(
                    modifier = Modifier.constrainAs(editProfileImageRef) {
                        bottom.linkTo(profileImageRef.bottom)
                        end.linkTo(profileImageRef.end, margin = 8.dp)
                    }.size(36.dp).clip(RoundedCornerShape(20.dp))
                        .background(colorResource(R.color.theme_color))
                        .border(
                            width = 1.5.dp,
                            color = colorResource(R.color.background_color),
                            shape = RoundedCornerShape(20.dp)
                        ).clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            imagePickerLauncher.launch("image/*")
                        }, contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.edit_icon),
                        contentDescription = "Edit Icon",
                        tint = colorResource(R.color.background_color),
                        modifier = Modifier.size(18.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                }

                Column(modifier = Modifier
                    .constrainAs(formContainerRef) {
                        top.linkTo(profileImageRef.bottom, margin = 30.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(updateProfileButtonRef.top, margin = (-16).dp)
                        height = Dimension.fillToConstraints
                    }.verticalScroll(rememberScrollState())
                    .padding(bottom = 25.dp)
                ) {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val (genderLabelRef, genderDropdownRef,
                            nameLabelRef, nameFieldContainerRef, phoneLabelRef, phoneFieldContainerRef,
                            emailLabelRef, emailFieldContainerRef
                        ) = createRefs()

                        Text("Name", modifier = Modifier.constrainAs(nameLabelRef) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start, margin = 28.dp)
                        }, fontSize = 13.sp, lineHeight = 15.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color)
                        )

                        Box(modifier = Modifier.constrainAs(nameFieldContainerRef) {
                            top.linkTo(nameLabelRef.bottom, margin = 10.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }.padding(horizontal = 25.dp).height(52.dp).fillMaxWidth().background(colorResource(R.color.secondary_text_color).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                                val (inputField, placeholderText) = createRefs()

                                if (name.isEmpty()) {
                                    Text(modifier = Modifier.constrainAs(placeholderText) {
                                        top.linkTo(parent.top)
                                        bottom.linkTo(parent.bottom)
                                        start.linkTo(parent.start, margin = 15.dp)
                                        end.linkTo(parent.end, margin = 15.dp)
                                        width = Dimension.fillToConstraints },
                                        text = "Enter Name",
                                        fontFamily = fonts,
                                        fontWeight = FontWeight.Normal,
                                        fontStyle = FontStyle.Normal,
                                        fontSize = 14.sp, lineHeight = 17.sp,
                                        color = colorResource(R.color.secondary_text_color)
                                    )
                                }

                                val selectionColors = TextSelectionColors(
                                    handleColor = Color(0xFF1C1C1C),
                                    backgroundColor = Color(0xFF1C1C1C).copy(alpha = 0.3f)
                                )

                                CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                                    BasicTextField(
                                        value = name,
                                        onValueChange = { name = it },
                                        modifier = Modifier
                                            .constrainAs(inputField) {
                                                top.linkTo(parent.top)
                                                bottom.linkTo(parent.bottom)
                                                start.linkTo(parent.start, margin = 15.dp)
                                                end.linkTo(parent.end, margin = 15.dp)
                                                width = Dimension.fillToConstraints
                                            },
                                        textStyle = TextStyle(
                                            fontFamily = fonts,
                                            fontWeight = FontWeight.SemiBold,
                                            fontStyle = FontStyle.Normal,
                                            fontSize = 14.sp, lineHeight = 17.sp,
                                            color = colorResource(R.color.secondary_text_color)
                                        ),
                                        singleLine = true,
                                        cursorBrush = SolidColor(Color(0xFF1C1C1C))
                                    )
                                }
                            }
                        }

                        Text("Phone Number", modifier = Modifier.constrainAs(phoneLabelRef) {
                            top.linkTo(nameFieldContainerRef.bottom, margin = 20.dp)
                            start.linkTo(parent.start, margin = 28.dp)
                        }, fontSize = 13.sp, lineHeight = 15.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color)
                        )

                        Box(modifier = Modifier.constrainAs(phoneFieldContainerRef) {
                            top.linkTo(phoneLabelRef.bottom, margin = 10.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }.padding(horizontal = 25.dp).height(52.dp).fillMaxWidth().background(colorResource(R.color.secondary_text_color).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                                val (inputField, placeholderText, text) = createRefs()

                                if (phoneNo.isEmpty()) {
                                    Text(modifier = Modifier.constrainAs(placeholderText) {
                                        top.linkTo(parent.top)
                                        bottom.linkTo(parent.bottom)
                                        start.linkTo(parent.start, margin = 15.dp)
                                        end.linkTo(text.start, margin = 15.dp)
                                        width = Dimension.fillToConstraints },
                                        text = "Enter Phone Number",
                                        fontFamily = fonts,
                                        fontWeight = FontWeight.Normal,
                                        fontStyle = FontStyle.Normal,
                                        fontSize = 14.sp, lineHeight = 17.sp,
                                        color = colorResource(R.color.secondary_text_color)
                                    )
                                }

                                val selectionColors = TextSelectionColors(
                                    handleColor = Color(0xFF1C1C1C),
                                    backgroundColor = Color(0xFF1C1C1C).copy(alpha = 0.3f)
                                )

                                CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                                    BasicTextField(
                                        value = phoneNo,
                                        onValueChange = {
                                            if (it.all { char -> char.isDigit() } && it.length <= 10) {
                                                phoneNo = it
                                            }
                                        },
                                        enabled = isPhoneEditable,
                                        modifier = Modifier
                                            .focusRequester(focusRequester)
                                            .constrainAs(inputField) {
                                                top.linkTo(parent.top)
                                                bottom.linkTo(parent.bottom)
                                                start.linkTo(parent.start, margin = 15.dp)
                                                end.linkTo(text.start, margin = 15.dp)
                                                width = Dimension.fillToConstraints
                                            },
                                        textStyle = TextStyle(
                                            fontFamily = fonts,
                                            fontWeight = FontWeight.SemiBold,
                                            fontStyle = FontStyle.Normal,
                                            fontSize = 14.sp, lineHeight = 17.sp,
                                            color = colorResource(R.color.secondary_text_color)
                                        ),
                                        singleLine = true,
                                        cursorBrush = SolidColor(Color(0xFF1C1C1C)),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),

                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                keyboardController?.hide()
                                                isPhoneEditable = false
                                            }
                                        )
                                    )
                                }

                                Text(modifier = Modifier.constrainAs(text) {
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                    end.linkTo(parent.end, margin = 15.dp) }
                                    .clickable {
                                        isPhoneEditable = !isPhoneEditable
                                    },
                                    text = if (isPhoneEditable) "Done" else "Change",
                                    fontFamily = fonts,
                                    fontWeight = FontWeight.Normal,
                                    fontStyle = FontStyle.Normal,
                                    fontSize = 14.sp, lineHeight = 17.sp,
                                    color = colorResource(R.color.theme_color)
                                )
                            }
                        }

                        Text("Email", modifier = Modifier.constrainAs(emailLabelRef) {
                            top.linkTo(phoneFieldContainerRef.bottom, margin = 20.dp)
                            start.linkTo(parent.start, margin = 28.dp)
                        }, fontSize = 13.sp, lineHeight = 15.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color)
                        )

                        Box(modifier = Modifier.constrainAs(emailFieldContainerRef) {
                            top.linkTo(emailLabelRef.bottom, margin = 10.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }.padding(horizontal = 25.dp).height(52.dp).fillMaxWidth().background(colorResource(R.color.secondary_text_color).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                                val (inputField, placeholderText) = createRefs()

                                if (email.isEmpty()) {
                                    Text(modifier = Modifier.constrainAs(placeholderText) {
                                        top.linkTo(parent.top)
                                        bottom.linkTo(parent.bottom)
                                        start.linkTo(parent.start, margin = 15.dp)
                                        end.linkTo(parent.end, margin = 15.dp)
                                        width = Dimension.fillToConstraints },
                                        text = "Enter Email",
                                        fontFamily = fonts,
                                        fontWeight = FontWeight.Normal,
                                        fontStyle = FontStyle.Normal,
                                        fontSize = 14.sp, lineHeight = 17.sp,
                                        color = colorResource(R.color.secondary_text_color)
                                    )
                                }

                                val selectionColors = TextSelectionColors(
                                    handleColor = Color(0xFF1C1C1C),
                                    backgroundColor = Color(0xFF1C1C1C).copy(alpha = 0.3f)
                                )

                                CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                                    BasicTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        enabled = false,
                                        modifier = Modifier
                                            .constrainAs(inputField) {
                                                top.linkTo(parent.top)
                                                bottom.linkTo(parent.bottom)
                                                start.linkTo(parent.start, margin = 15.dp)
                                                end.linkTo(parent.end, margin = 15.dp)
                                                width = Dimension.fillToConstraints
                                            },
                                        textStyle = TextStyle(
                                            fontFamily = fonts,
                                            fontWeight = FontWeight.SemiBold,
                                            fontStyle = FontStyle.Normal,
                                            fontSize = 14.sp, lineHeight = 17.sp,
                                            color = colorResource(R.color.secondary_text_color)
                                        ),
                                        singleLine = true,
                                        cursorBrush = SolidColor(Color(0xFF1C1C1C))
                                    )
                                }
                            }
                        }

                        Text("Gender", modifier = Modifier.constrainAs(genderLabelRef) {
                            top.linkTo(emailFieldContainerRef.bottom, margin = 20.dp)
                            start.linkTo(parent.start, margin = 28.dp)
                        }, fontSize = 13.sp, lineHeight = 15.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color)
                        )

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.constrainAs(genderDropdownRef) {
                                top.linkTo(genderLabelRef.bottom, margin = 10.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }.padding(horizontal = 25.dp)
                                .fillMaxWidth().zIndex(1f)
                        ) {
                            TextField(
                                value = gender.ifEmpty{ "" },
                                onValueChange = {},
                                readOnly = true,
                                placeholder = {
                                    Text(
                                        "Select",
                                        fontSize = 14.sp,
                                        fontFamily = fonts,
                                        fontWeight = FontWeight.Normal,
                                        fontStyle = FontStyle.Normal,
                                        lineHeight = 17.sp,
                                        color = colorResource(R.color.secondary_text_color)
                                    )
                                },
                                textStyle = TextStyle(
                                    fontFamily = fonts,
                                    fontWeight = FontWeight.SemiBold,
                                    fontStyle = FontStyle.Normal,
                                    fontSize = 14.sp, lineHeight = 17.sp,
                                    color = colorResource(R.color.secondary_text_color)
                                ),
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.right_arrow_icon),
                                        contentDescription = null,
                                        tint = colorResource(R.color.theme_color),
                                        modifier = Modifier
                                            .rotate(rotation)
                                            .size(22.dp)
                                    )
                                },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(
                                    focusedContainerColor = colorResource(R.color.secondary_text_color).copy(alpha = 0.2f),
                                    unfocusedContainerColor = colorResource(R.color.secondary_text_color).copy(alpha = 0.2f),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .menuAnchor()
                                    .height(52.dp)
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                containerColor = Color(0xFF3a3a3a),
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                genderOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.SemiBold,
                                                fontStyle = FontStyle.Normal,
                                                fontSize = 14.sp, lineHeight = 17.sp,
                                                color = colorResource(R.color.background_color)
                                            ) },
                                        onClick = {
                                            gender = option
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Button(modifier = Modifier.constrainAs(updateProfileButtonRef) {
                    bottom.linkTo(parent.bottom, margin = 35.dp)
                }.fillMaxWidth().padding(horizontal = 25.dp).height(52.dp).shadow(
                    elevation = 26.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = colorResource(R.color.theme_color).copy(alpha = 0.2f),
                    spotColor = colorResource(R.color.theme_color).copy(alpha = 0.4f)
                ),
                    onClick = {
                        when {
                            phoneNo.isNotEmpty() && phoneNo.length != 10 -> {
                                scope.launch {
                                    snackBarHostState.showSnackbar(
                                        message = "Phone number must be 10 digits",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@Button
                            }

                            name.isBlank() -> {
                                scope.launch {
                                    snackBarHostState.showSnackbar(
                                        message = "Please enter your name",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@Button
                            }
                        }

                        onUpdateClick(name, phoneNo, gender) { message ->
                            scope.launch {
                                snackBarHostState.showSnackbar(
                                    message = message,
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }, colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.theme_color),
                        contentColor = colorResource(R.color.background_color)
                    ) , shape = RoundedCornerShape(26.dp)) {

                    Text("Update", fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Normal, fontSize = 18.sp
                    )
                }
            }
        }

        LaunchedEffect(isPhoneEditable) {
            if (isPhoneEditable) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }

        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp).padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colorResource(R.color.background_color)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Text(
                            text = "Updating Profile Photo",
                            fontSize = 16.sp, lineHeight = 16.sp,
                            fontFamily = fonts, fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color), maxLines = 1,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val composition by rememberLottieComposition(
                            LottieCompositionSpec.RawRes(R.raw.astronaut_illustration)
                        )

                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(160.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 28.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            LinearProgressIndicator(
                                color = colorResource(R.color.theme_color),
                                trackColor = colorResource(R.color.secondary_text_color).copy(alpha = 0.3f),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50)),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun uploadToCloudinary(imageUri: Uri,
                               database: DatabaseReference,
                               profileViewModel: ProfileViewModel,
                               onShowMessage: (String) -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    profileViewModel.setUploading(true)

    MediaManager.get().upload(imageUri)
        .option("folder", "profile_pics")
        .option("public_id", userId)
        .option("overwrite", true)
        .callback(object : UploadCallback {
            override fun onStart(requestId: String?) {
                Log.d("UPLOAD_DEBUG", "Upload started")
                profileViewModel.setUploading(true)
                profileViewModel.updateProgress(0f)
            }

            override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                val progress = bytes.toFloat() / totalBytes.toFloat()
                Log.d("UPLOAD_DEBUG", "Progress: $progress")
                profileViewModel.updateProgress(progress)
            }

            override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                profileViewModel.setUploading(false)

                val secureUrl = resultData?.get("secure_url").toString()
                val version = resultData?.get("version").toString()

                val finalUrl = "$secureUrl?v=$version"

                // Save URL in Firebase Realtime Database
                database.child(userId).child("photoUrl").setValue(finalUrl)
                    .addOnSuccessListener {
                        onShowMessage("Profile photo updated")

                        profileViewModel.refreshProfileImage(userId)
                    }
                    .addOnFailureListener { e ->
                        onShowMessage("Failed to update profile: ${e.message}")
                    }
            }

            override fun onError(requestId: String, p1: com.cloudinary.android.callback.ErrorInfo) {
                profileViewModel.setUploading(false)
                profileViewModel.resetProgress()

                onShowMessage("Upload failed: ${p1.description}")
            }

            override fun onReschedule(requestId: String, p1: com.cloudinary.android.callback.ErrorInfo) {
                // You can leave this empty if you don’t need it
            }
        })
        .dispatch()
}

@Preview(showSystemUi = true)
@Composable
fun YourProfileActivityPreview() {
    val viewModel: ProfileViewModel = viewModel()
    WaveXTheme {
        YourProfileScreen(
            viewModel = viewModel,
            imageUrl = "",
            savedName = "",
            savedEmail = "",
            savedPhoneNO = "",
            savedGender = "",
            onUpdateClick = { _, _, _ , _-> }
        )
    }
}
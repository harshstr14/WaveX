package com.example.wavex.feature.profile.user.presentation

import android.app.Activity
import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.wavex.R
import com.example.wavex.feature.auth.presentation.signup.fonts
import com.example.wavex.feature.library.presentation.pressScale
import com.example.wavex.feature.profile.user.model.ProfileUpdateState
import com.example.wavex.ui.theme.WaveXTheme
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourProfileScreen(
    imageUrl: String?,
    userName: String,
    userEmail: String,
    userPhoneNo: String,
    userGender: String,
    updateState: ProfileUpdateState,
    onUpdateClick: (String, String, String) -> Unit,
    isUploading: Boolean,
    onSetUploading: (Boolean) -> Unit,
    onUpdateProgress: (Float) -> Unit,
    onRefreshUser: () -> Unit,
    onResetProgress: () -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val (backInteraction, backScale) = pressScale()
    val (editInteraction, editScale) = pressScale()

    var name by rememberSaveable { mutableStateOf("") }
    var phoneNo by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf("") }
    var isPhoneEditable by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var expanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Male", "Female", "Other")

    var nameFocused by remember { mutableStateOf(false) }
    var phoneNoFocused by remember { mutableStateOf(false) }
    var emailFocused by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "ArrowRotation"
    )

    LaunchedEffect(userName, userPhoneNo, userEmail, userGender) {
        name = userName
        phoneNo = userPhoneNo
        email = userEmail
        gender = userGender
    }

    LaunchedEffect(updateState) {
        when (updateState) {
            is ProfileUpdateState.Success -> {
                snackBarHostState.showSnackbar(
                    updateState.message
                )
            }

            is ProfileUpdateState.Error -> {
                snackBarHostState.showSnackbar(
                    updateState.message
                )
            }

            else -> Unit
        }
    }

    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val resultUri = UCrop.getOutput(result.data!!)
            resultUri?.let { uri ->
                // Upload to Cloudinary and save URL
                uploadToCloudinary(
                    uri,
                    onRefreshUser = {
                        onRefreshUser()
                    },
                    onResetProgress = {
                        onResetProgress()
                    },
                    onUpdateProgress = { float ->
                        onUpdateProgress(float)
                    },
                    onSetUploading = { boolean ->
                        onSetUploading(boolean)
                    },
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

    val animatedBlur by animateFloatAsState(
        targetValue = if (isUploading) 25f else 0f,
        label = ""
    )

    var shadowColor by remember { mutableStateOf(Color(0xFFF6F6F6)) }
    var startAnimation by remember { mutableStateOf(false) }

    val shadowAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 0.8f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "shadowAlpha"
    )

    val shadowScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.6f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "shadowScale"
    )

    val shadowBlur by animateFloatAsState(
        targetValue = if (startAnimation) 60f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "shadowBlur"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Scaffold(
        modifier = Modifier.background(colorResource(R.color.background_color)).
        nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    Box(
                        modifier = Modifier.padding(start = 20.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(
                                width = 1.dp,
                                color = colorResource(R.color.secondary_text_color).copy(alpha = 0.40f),
                                shape = RoundedCornerShape(20.dp)
                            ).clickable(
                                interactionSource = backInteraction,
                                indication = ripple(
                                    bounded = true,
                                    color = colorResource(R.color.secondary_text_color).copy(alpha = 0.25f)
                                )
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
                                    scaleX = backScale
                                    scaleY = backScale
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
                hostState = snackBarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) { data ->
                Snackbar(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color(0xFF414141),
                    shape = RoundedCornerShape(16.dp)
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
                .background(colorResource(R.color.background_color))
                .padding(paddingValues)
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
                val (formContainerRef, updateProfileButtonRef) = createRefs()

                Column(
                    modifier = Modifier
                        .constrainAs(formContainerRef) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(updateProfileButtonRef.top, margin = (-16).dp)
                            height = Dimension.fillToConstraints
                        }
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 25.dp)
                ) {
                    ConstraintLayout(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val (
                            genderLabelRef, genderDropdownRef, profileImageRef, editProfileImageRef,
                            nameLabelRef, nameFieldContainerRef, phoneLabelRef, phoneFieldContainerRef,
                            emailLabelRef, emailFieldContainerRef
                        ) = createRefs()

                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageUrl)
                                .allowHardware(false)
                                .build(),
                            contentDescription = "Profile Image",
                            onSuccess = { result ->
                                val drawable = result.result.drawable
                                val bitmap =
                                    (drawable as? BitmapDrawable)?.bitmap ?: return@AsyncImage

                                Palette.from(bitmap).generate { palette ->
                                    palette?.dominantSwatch?.rgb?.let { colorInt ->
                                        shadowColor = Color(colorInt)
                                    }
                                }
                            },
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .constrainAs(profileImageRef) {
                                    top.linkTo(parent.top)
                                    end.linkTo(parent.end)
                                    start.linkTo(parent.start)
                                }
                                .padding(top = 25.dp).size(162.dp)
                                .drawBehind {
                                    val glowRadius = (size.minDimension / 2) * shadowScale
                                    val safeBlur = shadowBlur.coerceAtLeast(0.1f)

                                    drawIntoCanvas { canvas ->
                                        val frameworkPaint = Paint().apply {
                                            isAntiAlias = true
                                            color = shadowColor.copy(alpha = shadowAlpha).toArgb()

                                            maskFilter = BlurMaskFilter(
                                                safeBlur,
                                                BlurMaskFilter.Blur.NORMAL
                                            )
                                        }

                                        canvas.nativeCanvas.drawCircle(
                                            center.x,
                                            center.y,
                                            glowRadius,
                                            frameworkPaint
                                        )
                                    }
                                }
                                .clip(CircleShape)
                        )

                        Box(
                            modifier = Modifier
                                .constrainAs(editProfileImageRef) {
                                    bottom.linkTo(profileImageRef.bottom)
                                    end.linkTo(profileImageRef.end, margin = 8.dp)
                                }
                                .size(36.dp).clip(RoundedCornerShape(20.dp))
                                .background(colorResource(R.color.theme_color))
                                .border(
                                    width = 1.5.dp,
                                    color = colorResource(R.color.background_color),
                                    shape = RoundedCornerShape(20.dp)
                                ).clickable(
                                    interactionSource = editInteraction,
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
                                        scaleX = editScale
                                        scaleY = editScale
                                    }
                            )
                        }

                        Text(
                            text = "Name",
                            modifier = Modifier
                                .constrainAs(nameLabelRef) {
                                    top.linkTo(profileImageRef.bottom, margin = 30.dp)
                                    start.linkTo(parent.start, margin = 28.dp)
                                },
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color)
                        )

                        Box(
                            modifier = Modifier
                                .constrainAs(nameFieldContainerRef) {
                                    top.linkTo(nameLabelRef.bottom, margin = 10.dp)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                }
                                .padding(horizontal = 25.dp).height(52.dp)
                                .fillMaxWidth()
                                .border(
                                    width = 1.1.dp,
                                    color = when {
                                        nameFocused -> colorResource(R.color.theme_color)
                                        else -> Color.Transparent
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(
                                    color = Color(0xFFfefefe),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                                val (inputField, placeholderText) = createRefs()

                                if (name.isEmpty()) {
                                    Text(
                                        modifier = Modifier
                                            .constrainAs(placeholderText) {
                                                top.linkTo(parent.top)
                                                bottom.linkTo(parent.bottom)
                                                start.linkTo(parent.start, margin = 15.dp)
                                                end.linkTo(parent.end, margin = 15.dp)
                                                width = Dimension.fillToConstraints
                                            },
                                        text = "Enter Name",
                                        fontFamily = fonts,
                                        fontWeight = FontWeight.Normal,
                                        fontStyle = FontStyle.Normal,
                                        fontSize = 15.sp, lineHeight = 18.sp,
                                        color = colorResource(R.color.secondary_text_color)
                                    )
                                }

                                val selectionColors = TextSelectionColors(
                                    handleColor = colorResource(R.color.primary_text_color).copy(
                                        alpha = 0.88f
                                    ),
                                    backgroundColor = colorResource(R.color.primary_text_color).copy(
                                        alpha = 0.3f
                                    )
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
                                            }
                                            .onFocusChanged {
                                                nameFocused = it.isFocused
                                            },
                                        textStyle = TextStyle(
                                            fontFamily = fonts,
                                            fontWeight = FontWeight.Normal,
                                            fontStyle = FontStyle.Normal,
                                            fontSize = 15.sp, lineHeight = 18.sp,
                                            color = colorResource(R.color.secondary_text_color)
                                        ),
                                        singleLine = true,
                                        cursorBrush = SolidColor(colorResource(R.color.primary_text_color).copy(alpha = 0.88f))
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Phone Number",
                            modifier = Modifier
                                .constrainAs(phoneLabelRef) {
                                    top.linkTo(nameFieldContainerRef.bottom, margin = 15.dp)
                                    start.linkTo(parent.start, margin = 28.dp)
                                },
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color)
                        )

                        Box(
                            modifier = Modifier
                                .constrainAs(phoneFieldContainerRef) {
                                    top.linkTo(phoneLabelRef.bottom, margin = 10.dp)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                }
                                .padding(horizontal = 25.dp)
                                .height(52.dp).fillMaxWidth()
                                .border(
                                    width = 1.1.dp,
                                    color = when {
                                        phoneNoFocused -> colorResource(R.color.theme_color)
                                        else -> Color.Transparent
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(
                                    color = Color(0xFFfefefe),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                                val (inputField, placeholderText, text) = createRefs()

                                if (phoneNo.isEmpty()) {
                                    Text(
                                        modifier = Modifier
                                            .constrainAs(placeholderText) {
                                                top.linkTo(parent.top)
                                                bottom.linkTo(parent.bottom)
                                                start.linkTo(parent.start, margin = 15.dp)
                                                end.linkTo(text.start, margin = 15.dp)
                                                width = Dimension.fillToConstraints
                                            },
                                        text = "Enter Phone Number",
                                        fontFamily = fonts,
                                        fontWeight = FontWeight.Normal,
                                        fontStyle = FontStyle.Normal,
                                        fontSize = 15.sp, lineHeight = 18.sp,
                                        color = colorResource(R.color.secondary_text_color)
                                    )
                                }

                                val selectionColors = TextSelectionColors(
                                    handleColor = colorResource(R.color.primary_text_color).copy(
                                        alpha = 0.88f
                                    ),
                                    backgroundColor = colorResource(R.color.primary_text_color).copy(
                                        alpha = 0.3f
                                    )
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
                                            }
                                            .onFocusChanged {
                                                phoneNoFocused = it.isFocused
                                            },
                                        textStyle = TextStyle(
                                            fontFamily = fonts,
                                            fontWeight = FontWeight.Normal,
                                            fontStyle = FontStyle.Normal,
                                            fontSize = 15.sp, lineHeight = 18.sp,
                                            color = colorResource(R.color.secondary_text_color)
                                        ),
                                        singleLine = true,
                                        cursorBrush = SolidColor(
                                            colorResource(R.color.primary_text_color).copy(
                                                alpha = 0.88f
                                            )
                                        ),
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

                                Text(
                                    modifier = Modifier
                                        .constrainAs(text) {
                                            top.linkTo(parent.top)
                                            bottom.linkTo(parent.bottom)
                                            end.linkTo(parent.end, margin = 15.dp)
                                        }
                                        .clickable {
                                            isPhoneEditable = !isPhoneEditable
                                        },
                                    text = if (isPhoneEditable) "Done" else "Change",
                                    fontFamily = fonts,
                                    fontWeight = FontWeight.Normal,
                                    fontStyle = FontStyle.Normal,
                                    fontSize = 14.sp, lineHeight = 18.sp,
                                    color = colorResource(R.color.theme_color)
                                )
                            }
                        }

                        Text(
                            text = "Email",
                            modifier = Modifier
                                .constrainAs(emailLabelRef) {
                                    top.linkTo(phoneFieldContainerRef.bottom, margin = 15.dp)
                                    start.linkTo(parent.start, margin = 28.dp)
                                },
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color)
                        )

                        Box(
                            modifier = Modifier
                                .constrainAs(emailFieldContainerRef) {
                                    top.linkTo(emailLabelRef.bottom, margin = 10.dp)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                }
                                .padding(horizontal = 25.dp)
                                .height(52.dp).fillMaxWidth()
                                .border(
                                    width = 1.1.dp,
                                    color = when {
                                        emailFocused -> colorResource(R.color.theme_color)
                                        else -> Color.Transparent
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(
                                    color = Color(0xFFfefefe),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                                val (inputField, placeholderText) = createRefs()

                                if (email.isEmpty()) {
                                    Text(
                                        modifier = Modifier
                                            .constrainAs(placeholderText) {
                                                top.linkTo(parent.top)
                                                bottom.linkTo(parent.bottom)
                                                start.linkTo(parent.start, margin = 15.dp)
                                                end.linkTo(parent.end, margin = 15.dp)
                                                width = Dimension.fillToConstraints
                                            },
                                        text = "Enter Email",
                                        fontFamily = fonts,
                                        fontWeight = FontWeight.Normal,
                                        fontStyle = FontStyle.Normal,
                                        fontSize = 15.sp, lineHeight = 18.sp,
                                        color = colorResource(R.color.secondary_text_color)
                                    )
                                }

                                val selectionColors = TextSelectionColors(
                                    handleColor = colorResource(R.color.primary_text_color).copy(
                                        alpha = 0.88f
                                    ),
                                    backgroundColor = colorResource(R.color.primary_text_color).copy(
                                        alpha = 0.3f
                                    )
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
                                            }
                                            .onFocusChanged {
                                                emailFocused = it.isFocused
                                            },
                                        textStyle = TextStyle(
                                            fontFamily = fonts,
                                            fontWeight = FontWeight.Normal,
                                            fontStyle = FontStyle.Normal,
                                            fontSize = 15.sp, lineHeight = 18.sp,
                                            color = colorResource(R.color.secondary_text_color)
                                        ),
                                        singleLine = true,
                                        cursorBrush = SolidColor(
                                            colorResource(R.color.primary_text_color).copy(
                                                alpha = 0.88f
                                            )
                                        )
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Gender",
                            modifier = Modifier
                                .constrainAs(genderLabelRef) {
                                    top.linkTo(emailFieldContainerRef.bottom, margin = 15.dp)
                                    start.linkTo(parent.start, margin = 28.dp)
                                },
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            fontFamily = fonts,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color)
                        )

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier
                                .constrainAs(genderDropdownRef) {
                                    top.linkTo(genderLabelRef.bottom, margin = 10.dp)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                }
                                .padding(horizontal = 25.dp)
                                .fillMaxWidth().zIndex(1f)
                        ) {
                            TextField(
                                value = gender.ifEmpty { "" },
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
                                    fontWeight = FontWeight.Normal,
                                    fontStyle = FontStyle.Normal,
                                    fontSize = 15.sp, lineHeight = 18.sp,
                                    color = colorResource(R.color.secondary_text_color)
                                ),
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.arrow_down_icon),
                                        contentDescription = null,
                                        tint = colorResource(R.color.theme_color),
                                        modifier = Modifier
                                            .rotate(rotation)
                                            .size(24.dp)
                                    )
                                },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(
                                    focusedContainerColor = Color(0xFFfefefe),
                                    unfocusedContainerColor = Color(0xFFfefefe),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(16.dp),
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
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                genderOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                fontFamily = fonts,
                                                fontWeight = FontWeight.SemiBold,
                                                fontStyle = FontStyle.Normal,
                                                fontSize = 14.sp, lineHeight = 16.sp,
                                                color = colorResource(R.color.background_color)
                                            )
                                        },
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

                Box(
                    modifier = Modifier
                        .constrainAs(updateProfileButtonRef) {
                            bottom.linkTo(parent.bottom, margin = 25.dp)
                        }
                        .fillMaxWidth().padding(horizontal = 20.dp).height(54.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(colorResource(R.color.theme_color))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            when {
                                phoneNo.isNotEmpty() && phoneNo.length != 10 -> {
                                    scope.launch {
                                        snackBarHostState.showSnackbar(
                                            message = "Phone number must be 10 digits",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@clickable
                                }

                                name.isBlank() -> {
                                    scope.launch {
                                        snackBarHostState.showSnackbar(
                                            message = "Please enter your name",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@clickable
                                }
                            }

                            onUpdateClick(name, phoneNo, gender)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Update", fontSize = 17.sp,
                        lineHeight = 20.sp, fontFamily = fonts,
                        fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.background_color)
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

@Preview(showSystemUi = true)
@Composable
fun YourProfileScreenPreview() {
    WaveXTheme {
        YourProfileScreen(
            imageUrl = "",
            userName = "",
            userEmail = "",
            userPhoneNo = "",
            userGender = "",
            updateState = ProfileUpdateState.Success(""),
            onUpdateClick = { _, _, _ -> },
            isUploading = false,
            onSetUploading = { _ -> },
            onUpdateProgress = { _ -> },
            onRefreshUser = {},
            onResetProgress = {}
        )
    }
}
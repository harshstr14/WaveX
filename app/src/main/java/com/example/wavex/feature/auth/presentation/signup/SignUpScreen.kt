package com.example.wavex.feature.auth.presentation.signup

import android.app.Activity
import android.content.Intent
import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.wavex.MainScreen
import com.example.wavex.R
import com.example.wavex.feature.auth.model.SignUpUiState
import com.example.wavex.feature.auth.presentation.signin.SignInActivity
import com.example.wavex.feature.auth.presentation.verifyemail.VerifyEmailActivity
import com.example.wavex.ui.theme.WaveXTheme

@Composable
fun SignUpScreen(
    uiState: SignUpUiState,
    onSignUp: (String, String, String) -> Unit,
    onSignInWithGoogle: () -> Unit,
    onClearSnackbar: () -> Unit,
    onContinueWithGuest: () -> Unit,
    onRefreshUser: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(uiState.navigateToVerifyEmail) {
        if (uiState.navigateToVerifyEmail) {
            context.startActivity(
                Intent(context,VerifyEmailActivity::class.java)
                    .apply {
                        putExtra("name",uiState.userName)
                        putExtra("email",uiState.email)
                    }
            )
        }
    }

    LaunchedEffect(uiState.navigateToHome) {
        if (uiState.navigateToHome) {
            context.startActivity(
                Intent(context,MainScreen::class.java)
            ).apply {
                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                onRefreshUser()
            }

            activity?.finish()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackBarHostState.showSnackbar(it)

            onClearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 15.dp)
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
                            data.visuals.message.contains("name") -> R.drawable.user_icon
                            data.visuals.message.contains("email") -> R.drawable.email_icon
                            data.visuals.message.contains("Email") -> R.drawable.email_icon
                            data.visuals.message.contains("password") -> R.drawable.password_icon
                            data.visuals.message.contains("Password") -> R.drawable.password_icon
                            data.visuals.message.contains("Welcome") -> R.drawable.logo2
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
        val context = LocalContext.current

        var name by rememberSaveable { mutableStateOf("") }
        var email by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }

        var nameFocused by rememberSaveable { mutableStateOf(false) }
        var emailFocused by rememberSaveable { mutableStateOf(false) }
        var passwordFocused by rememberSaveable { mutableStateOf(false) }

        var nameError by rememberSaveable { mutableStateOf(false) }
        var emailError by rememberSaveable { mutableStateOf(false) }
        var passwordError by rememberSaveable { mutableStateOf(false) }
        var nameErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
        var emailErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
        var passwordErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }

        var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
        val scrollState = rememberScrollState()

        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize().padding(paddingValues)
                .background(colorResource(R.color.background_color))
        ) {
            val (titleText, subtitleText, formContainer) = createRefs()

            Text(
                text = "Create Account",
                modifier = Modifier
                    .constrainAs(titleText) {
                        top.linkTo(parent.top, margin = 45.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                fontSize = 24.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.primary_text_color), lineHeight = 28.sp
            )

            Text(
                text = "Fill your  information below or register\nwith your social account",
                modifier = Modifier
                    .constrainAs(subtitleText) {
                        top.linkTo(titleText.bottom, margin = 14.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                fontSize = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.secondary_text_color), textAlign = TextAlign.Center, lineHeight = 16.sp
            )

            Column(
                modifier = Modifier
                    .constrainAs(formContainer) {
                        top.linkTo(subtitleText.bottom, margin = 15.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                    }
                    .verticalScroll(scrollState)
            ) {
                ConstraintLayout(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val (
                        nameLabel, emailLabel, passwordLabel, nameInputContainer, emailInputContainer, passwordInputContainer, signUpButton,
                        googleSignInButton, anonymousLogInButton, dividerLeft, dividerRight, dividerText, signInSection
                    ) = createRefs()

                    Text(
                        text = "Name",
                        modifier = Modifier
                            .constrainAs(nameLabel) {
                                top.linkTo(parent.top, margin = 30.dp)
                                start.linkTo(parent.start, margin = 28.dp)
                            },
                        fontSize = 14.sp, lineHeight = 18.sp, fontFamily = fonts,
                        fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.primary_text_color)
                    )

                    Box(
                        modifier = Modifier
                            .constrainAs(nameInputContainer) {
                                top.linkTo(nameLabel.bottom, margin = 10.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                            .padding(horizontal = 25.dp).height(52.dp)
                            .fillMaxWidth()
                            .border(
                                width = 1.1.dp,
                                color = when {
                                    nameError -> Color.Red
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
                        ConstraintLayout(
                            modifier = Modifier.fillMaxSize()
                        ) {
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
                                handleColor = colorResource(R.color.primary_text_color).copy(alpha = 0.88f),
                                backgroundColor = colorResource(R.color.primary_text_color).copy(alpha = 0.3f)
                            )

                            CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                                BasicTextField(
                                    value = name,
                                    onValueChange = {
                                        name = it

                                        if (it.isNotBlank()) {
                                            nameError = false
                                        }
                                    },
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

                    Column(
                        modifier = Modifier
                            .constrainAs(emailLabel) {
                                top.linkTo(nameInputContainer.bottom, margin = 10.dp)
                                start.linkTo(parent.start, margin = 28.dp)
                            }
                    ) {
                        AnimatedVisibility(nameError) {
                            Text(
                                text = nameErrorMessage ?: "",
                                color = Color.Red,
                                fontSize = 12.sp,
                                lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Normal
                            )
                        }

                        Spacer(modifier = Modifier.height(if (nameError) 10.dp else 10.dp))

                        Text(
                            text = "Email",
                            fontSize = 14.sp, lineHeight = 18.sp, fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .constrainAs(emailInputContainer) {
                                top.linkTo(emailLabel.bottom, margin = 10.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                            .padding(horizontal = 25.dp)
                            .height(52.dp).fillMaxWidth()
                            .border(
                                width = 1.1.dp,
                                color = when {
                                    emailError -> Color.Red
                                    emailFocused -> colorResource(R.color.theme_color)
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
                        ConstraintLayout(
                            modifier = Modifier.fillMaxSize()
                        ) {
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
                                handleColor = colorResource(R.color.primary_text_color).copy(alpha = 0.88f),
                                backgroundColor = colorResource(R.color.primary_text_color).copy(alpha = 0.3f)
                            )

                            CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                                BasicTextField(
                                    value = email,
                                    onValueChange = {
                                        email = it

                                        if (it.isNotBlank()) {
                                            emailError = false
                                        }
                                    },
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
                                    cursorBrush = SolidColor(colorResource(R.color.primary_text_color).copy(alpha = 0.88f))
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .constrainAs(passwordLabel) {
                                top.linkTo(emailInputContainer.bottom, margin = 10.dp)
                                start.linkTo(parent.start, margin = 28.dp)
                            }
                    ) {
                        AnimatedVisibility(emailError) {
                            Text(
                                text = emailErrorMessage ?: "",
                                color = Color.Red,
                                fontSize = 12.sp,
                                lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Normal
                            )
                        }

                        Spacer(modifier = Modifier.height(if (emailError) 10.dp else 10.dp))

                        Text(
                            text = "Password",
                            fontSize = 14.sp, lineHeight = 18.sp, fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.primary_text_color)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .constrainAs(passwordInputContainer) {
                                top.linkTo(passwordLabel.bottom, margin = 10.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                            .padding(horizontal = 25.dp).height(52.dp)
                            .border(
                                width = 1.1.dp,
                                color = when {
                                    passwordError -> Color.Red
                                    passwordFocused -> colorResource(R.color.theme_color)
                                    else -> Color.Transparent
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFfefefe),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        ConstraintLayout(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val (inputField, placeholderText, toggleIcon) = createRefs()

                            if (password.isEmpty()) {
                                Text(
                                    modifier = Modifier
                                        .constrainAs(placeholderText) {
                                            top.linkTo(parent.top)
                                            bottom.linkTo(parent.bottom)
                                            start.linkTo(parent.start, margin = 15.dp)
                                            end.linkTo(parent.end, margin = 15.dp)
                                            width = Dimension.fillToConstraints
                                        },
                                    text = "Enter Password",
                                    fontFamily = fonts,
                                    fontWeight = FontWeight.Normal,
                                    fontStyle = FontStyle.Normal,
                                    fontSize = 15.sp, lineHeight = 18.sp,
                                    color = colorResource(R.color.secondary_text_color)
                                )
                            }

                            val selectionColors = TextSelectionColors(
                                handleColor = colorResource(R.color.primary_text_color).copy(alpha = 0.88f),
                                backgroundColor = colorResource(R.color.primary_text_color).copy(alpha = 0.3f)
                            )

                            CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                                BasicTextField(
                                    value = password,
                                    onValueChange = {
                                        password = it

                                        if (it.isNotBlank()) {
                                            passwordError = false
                                        }
                                    },
                                    modifier = Modifier
                                        .constrainAs(inputField) {
                                            top.linkTo(parent.top)
                                            bottom.linkTo(parent.bottom)
                                            start.linkTo(parent.start, margin = 15.dp)
                                            end.linkTo(toggleIcon.start, margin = 15.dp)
                                            width = Dimension.fillToConstraints
                                        }
                                        .onFocusChanged {
                                            passwordFocused = it.isFocused
                                        },
                                    textStyle = TextStyle(
                                        fontFamily = fonts,
                                        fontWeight = FontWeight.Normal,
                                        fontStyle = FontStyle.Normal,
                                        fontSize = 15.sp, lineHeight = 18.sp,
                                        color = colorResource(R.color.secondary_text_color)
                                    ),
                                    singleLine = true,
                                    cursorBrush = SolidColor(colorResource(R.color.primary_text_color).copy(alpha = 0.88f)),
                                    visualTransformation = if (isPasswordVisible)
                                        VisualTransformation.None
                                    else
                                        PasswordVisualTransformation()
                                )
                            }
                            Icon(
                                painter = painterResource(
                                    if (isPasswordVisible)
                                        R.drawable.eye_open_icon
                                    else
                                        R.drawable.eye_closed_icon
                                ),
                                contentDescription = "Toggle password",
                                tint = colorResource(R.color.primary_text_color),
                                modifier = Modifier
                                    .size(20.dp)
                                    .constrainAs(toggleIcon) {
                                        end.linkTo(parent.end, margin = 15.dp)
                                        top.linkTo(parent.top)
                                        bottom.linkTo(parent.bottom)
                                    }
                                    .clickable {
                                        isPasswordVisible = !isPasswordVisible
                                    }
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .constrainAs(signUpButton) {
                                top.linkTo(passwordInputContainer.bottom, margin = 10.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                width = Dimension.fillToConstraints
                            }
                            .padding(horizontal = 25.dp)
                    ) {
                        AnimatedVisibility(passwordError) {
                            Text(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .align(Alignment.Start),
                                text = passwordErrorMessage ?: "",
                                color = Color.Red,
                                fontSize = 12.sp,
                                lineHeight = 14.sp, fontFamily = fonts, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Normal
                            )
                        }

                        Spacer(modifier = Modifier.height(if (passwordError) 25.dp else 25.dp))

                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .shadow(
                                    elevation = 26.dp,
                                    shape = RoundedCornerShape(22.dp),
                                    ambientColor = colorResource(R.color.theme_color).copy(alpha = 0.2f),
                                    spotColor = colorResource(R.color.theme_color).copy(alpha = 0.4f)
                                ),
                            onClick = {
                                keyboardController?.hide()

                                when {
                                    name.isBlank() && email.isBlank() && password.isBlank() -> {
                                        nameError = true
                                        emailError = true
                                        passwordError = true

                                        nameErrorMessage = "Name is required"
                                        emailErrorMessage = "Email is required"
                                        passwordErrorMessage = "Password is required"

                                        return@Button
                                    }

                                    name.isBlank() -> {
                                        nameError = true
                                        nameErrorMessage = "Name is required"

                                        return@Button
                                    }

                                    email.isBlank() -> {
                                        emailError = true
                                        emailErrorMessage = "Email is required"

                                        return@Button
                                    }

                                    password.isBlank() -> {
                                        passwordError = true
                                        passwordErrorMessage = "Password is required"

                                        return@Button
                                    }

                                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                                        emailError = true
                                        emailErrorMessage = "Enter a valid email"

                                        return@Button
                                    }
                                }

                                onSignUp(
                                    name, email, password
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.theme_color),
                                contentColor = colorResource(R.color.background_color)
                            ),
                            shape = RoundedCornerShape(22.dp)) {

                            Text(
                                text = "Sign Up", fontSize = 16.sp, lineHeight = 18.sp,
                                fontFamily = fonts, fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Normal, color = colorResource(R.color.off_white)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .constrainAs(dividerLeft) {
                                top.linkTo(dividerText.top)
                                bottom.linkTo(dividerText.bottom)
                                end.linkTo(dividerText.start, margin = 12.dp)
                            }
                            .width(90.dp).height(2.dp)
                            .drawWithCache {
                                onDrawBehind {
                                    drawLine(
                                        color = Color(0xFF797979).copy(alpha = 0.4f),
                                        start = Offset(size.width / 2, 0f),
                                        end = Offset(size.width / 2, size.height),
                                        strokeWidth = size.width
                                    )
                                }
                            }
                    )

                    Text(
                        text = "Or sign up with",
                        modifier = Modifier
                            .constrainAs(dividerText) {
                                top.linkTo(signUpButton.bottom, margin = 18.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            },
                        fontSize = 13.sp, fontFamily = fonts, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.secondary_text_color), textAlign = TextAlign.Center, lineHeight = 15.sp
                    )

                    Box(
                        modifier = Modifier
                            .constrainAs(dividerRight) {
                                top.linkTo(dividerText.top)
                                bottom.linkTo(dividerText.bottom)
                                start.linkTo(dividerText.end, margin = 12.dp)
                            }
                            .width(90.dp).height(2.dp)
                            .drawWithCache {
                                onDrawBehind {
                                    drawLine(
                                        color = Color(0xFF797979).copy(alpha = 0.4f),
                                        start = Offset(size.width / 2, 0f),
                                        end = Offset(size.width / 2, size.height),
                                        strokeWidth = size.width
                                    )
                                }
                            }
                    )

                    Box(
                        modifier = Modifier
                            .constrainAs(googleSignInButton) {
                                top.linkTo(dividerText.bottom, margin = 18.dp)
                            }
                            .padding(horizontal = 25.dp).height(54.dp)
                            .fillMaxWidth()
                            .shadow(
                                elevation = 26.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = Color(0xFF2C2C2C).copy(alpha = 0.2f),
                                spotColor = Color(0xFF2C2C2C).copy(alpha = 0.4f)
                            )
                            .clickable {
                                onSignInWithGoogle()

                                keyboardController?.hide()
                            }
                            .background(Color(0xFF2C2C2C),
                                shape = RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row {
                            Icon(
                                painter = painterResource(R.drawable.google_icon),
                                contentDescription = "Google Icon",
                                tint = Color.Unspecified
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Sign in with google", fontSize = 15.sp,
                                fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal, color = colorResource(R.color.off_white)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .constrainAs(anonymousLogInButton) {
                                top.linkTo(googleSignInButton.bottom, margin = 18.dp)
                            }
                            .padding(horizontal = 25.dp).height(54.dp)
                            .fillMaxWidth()
                            .shadow(
                                elevation = 26.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = Color(0xFF2C2C2C).copy(alpha = 0.2f),
                                spotColor = Color(0xFF2C2C2C).copy(alpha = 0.4f)
                            )
                            .clickable {
                                onContinueWithGuest()

                                keyboardController?.hide()
                            }
                            .background(Color(0xFF2C2C2C),
                                shape = RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    )
                    {
                        Row {
                            Icon(
                                painter = painterResource(R.drawable.guest_user_icon),
                                contentDescription = "Google Icon",
                                tint = colorResource(R.color.theme_color),
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Continue as Guest", fontSize = 15.sp,
                                fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal, color = colorResource(R.color.off_white)
                            )
                        }
                    }

                    Row (
                        modifier = Modifier
                            .constrainAs(signInSection) {
                                top.linkTo(anonymousLogInButton.bottom, margin = 32.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                            .padding(bottom = 45.dp)
                    ) {
                        Text(
                            text = "Already have an account?",
                            fontSize = 12.sp, fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.secondary_text_color), textAlign = TextAlign.Center, lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.width(3.dp))

                        Text(
                            text = "Sign In",
                            fontSize = 12.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.theme_color), textAlign = TextAlign.Center, lineHeight = 15.sp, modifier = Modifier
                                .clickable {
                                    val intent = Intent(context, SignInActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    }
                                    context.startActivity(intent)
                                }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun SignUpScreenPreview() {
    WaveXTheme {
        SignUpScreen(
            uiState = SignUpUiState(),
            onSignUp = { _, _, _ -> },
            onSignInWithGoogle = {},
            onClearSnackbar = {},
            onRefreshUser = {},
            onContinueWithGuest = {}
        )
    }
}
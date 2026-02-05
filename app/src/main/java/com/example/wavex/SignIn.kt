package com.example.wavex

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
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
import com.example.wavex.googleAuthentication.GoogleSignInManager
import com.example.wavex.ui.theme.WaveXTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.launch

private lateinit var auth: FirebaseAuth

class SignIn : ComponentActivity() {
    private lateinit var googleSignInManager: GoogleSignInManager

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            googleSignInManager.handleSignInResult(
                it,
                onSuccess = { auth ->
                    Toast.makeText(this,"Welcome ${auth.currentUser?.displayName}",Toast.LENGTH_SHORT).show()
                    startActivity(
                        Intent(this, MainScreen::class.java).apply {
                            flags =Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                },
                onError = { message ->
                    Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
                }
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleSignInManager = GoogleSignInManager(this)
        auth = FirebaseAuth.getInstance()

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = 0xFFF6F6F6.toInt()
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = 0xFFF6F6F6.toInt()
            )
        )

        setContent {
            WaveXTheme {
                SignInScreen(onGoogleSignIn = {
                    googleSignInManager.signIn(launcher)
                })
            }
        }
    }
}

@Composable
fun SignInScreen(onGoogleSignIn: () -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 15.dp).shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(10.dp),
                            ambientColor = Color(0xFF1C1C1C),
                            spotColor = Color(0xFF1C1C1C)
                        ),
                    containerColor = Color(0xFF1C1C1C),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painter = painterResource(when {
                            data.visuals.message.contains("Email") -> R.drawable.email_icon
                            data.visuals.message.contains("email") -> R.drawable.email_icon
                            data.visuals.message.contains("Welcome") -> R.drawable.logo2
                            data.visuals.message.contains("password") -> R.drawable.password_icon
                            else -> {
                                R.drawable.alert_icon
                            }
                        } ), contentDescription = "Icons",
                            tint = Color(0xFF34A853), modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = data.visuals.message,
                            fontFamily = fonts,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal,
                            fontSize = 13.sp,
                            color = Color(0xFFF6F6F6)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        val context = LocalContext.current
        val activity = remember(context) { context as? Activity }

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        var isPasswordVisible by remember { mutableStateOf(false) }
        val scrollState = rememberScrollState()

        ConstraintLayout(modifier = Modifier.fillMaxSize().padding(paddingValues).background(colorResource(R.color.background_color))) {
            val (titleText,subtitleText,formContainer) = createRefs()

            Text("Sign In", modifier = Modifier.constrainAs(titleText) {
                top.linkTo(parent.top, margin = 45.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }, fontSize = 22.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.primary_text_color), lineHeight = 26.sp
            )

            Text("Hii! Welcome back, you`ve been missed", modifier = Modifier.constrainAs(subtitleText) {
                top.linkTo(titleText.bottom, margin = 14.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }, fontSize = 12.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = colorResource(R.color.secondary_text_color), textAlign = TextAlign.Center, lineHeight = 14.sp
            )

            Column(modifier = Modifier.constrainAs(formContainer) {
                top.linkTo(subtitleText.bottom, margin = 15.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
                height = Dimension.fillToConstraints
            }.verticalScroll(scrollState)) {
                ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                    val (forgotPasswordText,emailLabel,passwordLabel,emailInputContainer,passwordInputContainer,
                        signInButton,googleSignInButton,dividerLeft,dividerRight,dividerText,signUpSection) = createRefs()

                    Text("Email", modifier = Modifier.constrainAs(emailLabel) {
                        top.linkTo(parent.top, margin = 30.dp)
                        start.linkTo(parent.start, margin = 28.dp)
                    }, fontSize = 12.sp, lineHeight = 15.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.primary_text_color)
                    )

                    Box(modifier = Modifier.constrainAs(emailInputContainer) {
                        top.linkTo(emailLabel.bottom, margin = 10.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }.padding(horizontal = 25.dp).height(52.dp).fillMaxWidth().background(colorResource(R.color.secondary_text_color).copy(alpha = 0.4f),
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

                    Text("Password", modifier = Modifier.constrainAs(passwordLabel) {
                        top.linkTo(emailInputContainer.bottom, margin = 20.dp)
                        start.linkTo(parent.start, margin = 28.dp)
                    }, fontSize = 12.sp, lineHeight = 15.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.primary_text_color)
                    )

                    Box(modifier = Modifier.constrainAs(passwordInputContainer) {
                        top.linkTo(passwordLabel.bottom, margin = 10.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }.padding(horizontal = 25.dp).height(52.dp).fillMaxWidth().background(colorResource(R.color.secondary_text_color).copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                            val (inputField, placeholderText, toggleIcon) = createRefs()

                            if (password.isEmpty()) {
                                Text(modifier = Modifier.constrainAs(placeholderText) {
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                    start.linkTo(parent.start, margin = 15.dp)
                                    end.linkTo(parent.end, margin = 15.dp)
                                    width = Dimension.fillToConstraints },
                                    text = "Enter Password",
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
                                    value = password,
                                    onValueChange = { password = it },
                                    modifier = Modifier
                                        .constrainAs(inputField) {
                                            top.linkTo(parent.top)
                                            bottom.linkTo(parent.bottom)
                                            start.linkTo(parent.start, margin = 15.dp)
                                            end.linkTo(toggleIcon.start, margin = 15.dp)
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

                    Text("Forgot Password?", modifier = Modifier.constrainAs(forgotPasswordText) {
                        top.linkTo(passwordInputContainer.bottom, margin = 18.dp)
                        end.linkTo(parent.end, margin = 28.dp)
                    }.clickable {
                        val intent = Intent(context, ForgotPassword::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        context.startActivity(intent)
                    }, fontSize = 12.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.theme_color), lineHeight = 15.sp
                    )

                    Button(modifier = Modifier.constrainAs(signInButton) {
                        top.linkTo(forgotPasswordText.bottom, margin = 35.dp)
                    }.fillMaxWidth().padding(horizontal = 25.dp).height(52.dp).shadow(
                        elevation = 26.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = colorResource(R.color.theme_color).copy(alpha = 0.2f),
                        spotColor = colorResource(R.color.theme_color).copy(alpha = 0.4f)
                    ),
                        onClick = {
                            keyboardController?.hide()

                            when {
                                email.isBlank() && password.isBlank() -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "All fields are required",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }

                                email.isBlank() -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Please enter your email",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }

                                password.isBlank() -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Please enter your password",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }

                                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Enter a valid email",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }
                            }

                            auth.signInWithEmailAndPassword(email.trim(),password.trim()).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser!!

                                    user.reload().addOnCompleteListener {
                                        val refreshedUser = auth.currentUser

                                        if (refreshedUser?.isEmailVerified == true) {
                                            context.startActivity(
                                                Intent(context, MainScreen::class.java)
                                            )
                                            activity?.finish()
                                        } else {
                                            refreshedUser?.sendEmailVerification()?.addOnCompleteListener {
                                                val intent = Intent(context, VerifyEmail::class.java).apply {
                                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                                    putExtra("name", auth.currentUser?.displayName?.trim())
                                                    putExtra("email", email.trim())
                                                }
                                                context.startActivity(intent)
                                            }?.addOnFailureListener {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Failed to send verification email",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    try {
                                        throw task.exception!!
                                    } catch (e: FirebaseAuthInvalidUserException) {
                                        // Email not registered
                                        Log.e("Auth", "Email : ${e.message}")
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "This email is not registered",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                                        // Wrong password
                                        Log.e("Auth", "Password : ${e.message}")
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Incorrect password",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } catch (e: Exception) {
                                        // Other errors
                                        Log.e("Auth", "Other : ${e.message}")
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Login failed: ${e.message}",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            }
                        }, colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.theme_color),
                            contentColor = colorResource(R.color.background_color)
                        ) , shape = RoundedCornerShape(26.dp)) {

                        Text("Sign In", fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Normal, fontSize = 18.sp
                        )
                    }

                    Box(modifier = Modifier.constrainAs(dividerLeft) {
                        top.linkTo(dividerText.top)
                        bottom.linkTo(dividerText.bottom)
                        end.linkTo(dividerText.start, margin = 12.dp)
                    }.width(90.dp).height(2.dp).drawWithCache {
                        onDrawBehind {
                            drawLine(
                                color = Color(0xFF797979).copy(alpha = 0.4f),
                                start = Offset(size.width / 2, 0f),
                                end = Offset(size.width / 2, size.height),
                                strokeWidth = size.width
                            )
                        } }
                    )

                    Text("Or sign in with", modifier = Modifier.constrainAs(dividerText) {
                        top.linkTo(signInButton.bottom, margin = 18.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }, fontSize = 12.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                        color = colorResource(R.color.secondary_text_color), textAlign = TextAlign.Center, lineHeight = 15.sp
                    )

                    Box(modifier = Modifier.constrainAs(dividerRight) {
                        top.linkTo(dividerText.top)
                        bottom.linkTo(dividerText.bottom)
                        start.linkTo(dividerText.end, margin = 12.dp)
                    }.width(90.dp).height(2.dp).drawWithCache {
                        onDrawBehind {
                            drawLine(
                                color = Color(0xFF797979).copy(alpha = 0.4f),
                                start = Offset(size.width / 2, 0f),
                                end = Offset(size.width / 2, size.height),
                                strokeWidth = size.width
                            )
                        } }
                    )

                    Box(modifier = Modifier.constrainAs(googleSignInButton) {
                        top.linkTo(dividerText.bottom, margin = 18.dp)
                    }.padding(horizontal = 25.dp).height(52.dp).fillMaxWidth().shadow(
                        elevation = 26.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = Color(0xFF1C1C1C).copy(alpha = 0.2f),
                        spotColor = Color(0xFF1C1C1C).copy(alpha = 0.4f)
                    ).clickable { onGoogleSignIn()
                        keyboardController?.hide()
                    }.background(Color(0xFF1C1C1C),
                        shape = RoundedCornerShape(26.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row {
                            Icon(painter = painterResource(R.drawable.google_icon), contentDescription = "Google Icon", tint = Color.Unspecified)

                            Spacer(modifier = Modifier.width(5.dp))

                            Text("Sign in with google", fontSize = 14.sp, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                                color = colorResource(R.color.background_color)
                            )
                        }
                    }

                    Row (modifier = Modifier.constrainAs(signUpSection) {
                        top.linkTo(googleSignInButton.bottom, margin = 32.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }.padding(bottom = 45.dp)) {
                        Text("Don`t have an account?" , fontSize = 12.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.secondary_text_color), textAlign = TextAlign.Center, lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.width(3.dp))

                        Text("Sign Up", fontSize = 12.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                            color = colorResource(R.color.theme_color), textAlign = TextAlign.Center, lineHeight = 15.sp, modifier = Modifier
                                .clickable {
                                    val intent = Intent(context, SignUp::class.java).apply {
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
fun SignInScreenPreview() {
    WaveXTheme {
        SignInScreen(onGoogleSignIn = { })
    }
}
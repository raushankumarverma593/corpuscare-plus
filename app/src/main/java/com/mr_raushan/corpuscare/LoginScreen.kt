package com.mr_raushan.corpuscare

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

enum class AuthState {
    LOGIN, SIGNUP, ADMIN, FACILITY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: (String, String, String?) -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    
    var animationStarted by remember { mutableStateOf(false) }
    var authState by remember { mutableStateOf(AuthState.LOGIN) }

    // Detect keyboard visibility
    val isKeyboardOpen = WindowInsets.ime.getBottom(androidx.compose.ui.platform.LocalDensity.current) > 0

    // Input fields state
    var mobileNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Sign Up fields state
    var fullName by remember { mutableStateOf("") }
    var signUpMobile by remember { mutableStateOf("") }
    var signUpPassword by remember { mutableStateOf("") }
    var signUpPasswordVisible by remember { mutableStateOf(false) }

    // Admin fields state
    var adminId by remember { mutableStateOf("") }
    var adminPass by remember { mutableStateOf("") }

    // Facility fields state
    var facilityId by remember { mutableStateOf("") }
    var facilityPass by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        delay(300)
        animationStarted = true
    }

    // Logo size animation: 140dp -> 90dp -> 55dp if keyboard is open
    val logoSize by animateDpAsState(
        targetValue = if (!animationStarted) 140.dp else if (isKeyboardOpen) 55.dp else 90.dp,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "LogoSize"
    )

    // Vertical Bias: 0f (Center) -> -0.85f (Top) -> -1.0f (Absolute top) if keyboard open
    val verticalBias by animateFloatAsState(
        targetValue = if (!animationStarted) 0f else if (isKeyboardOpen) -1.0f else -0.85f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "VerticalBias"
    )

    // Alpha for content and branding
    val contentAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 400),
        label = "ContentAlpha"
    )

    val formOffsetY by animateDpAsState(
        targetValue = if (animationStarted) 0.dp else 100.dp,
        animationSpec = tween(durationMillis = 1000, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "FormOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
            .statusBarsPadding()
            .imePadding()
    ) {
        // --- 1 & 2. SMOOTH LOGO AND BRANDING ANIMATION ---
        Column(
            modifier = Modifier
                .align(BiasAlignment(0f, verticalBias))
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.mipmap.app_logo),
                contentDescription = "CorpusCare Logo",
                modifier = Modifier
                    .size(logoSize)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Branding (Fade In)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer(alpha = contentAlpha)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Corpus",
                        fontSize = if (isKeyboardOpen) 22.sp else 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0052D4)
                    )
                    Text(
                        text = "Care",
                        fontSize = if (isKeyboardOpen) 22.sp else 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF006766)
                    )
                    Spacer(modifier = Modifier.width(if (isKeyboardOpen) 6.dp else 12.dp))
                    Text(
                        text = "+",
                        fontSize = if (isKeyboardOpen) 24.sp else 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00BCD4)
                    )
                }
                if (!isKeyboardOpen) {
                    Text(
                        text = "Complete Care for Every Body.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // --- 3 & 4. FLEXIBLE LOGIN / SIGNUP / ADMIN FORMS ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .graphicsLayer(
                    alpha = contentAlpha,
                    translationY = formOffsetY.value
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .background(
                        Color.White,
                        RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = if (isKeyboardOpen) 24.dp else 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = authState,
                    transitionSpec = {
                        fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                    },
                    label = "AuthFormTransition"
                ) { targetState ->
                    when (targetState) {
                        AuthState.LOGIN -> LoginForm(
                            mobile = mobileNumber,
                            onMobileChange = { mobileNumber = it },
                            pass = password,
                            onPassChange = { password = it },
                            visible = passwordVisible,
                            onVisibleToggle = { passwordVisible = !passwordVisible },
                            onLogin = {
                                if (mobileNumber.isNotBlank() && password.isNotBlank()) {
                                    val email = "$mobileNumber@corpuscare.com"
                                    auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                // Show success toast immediately
                                                Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                                                
                                                val uid = auth.currentUser?.uid
                                                if (uid != null) {
                                                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                    db.collection("users").document(uid).get()
                                                        .addOnSuccessListener { doc ->
                                                            val name = doc.getString("name") ?: "User"
                                                            onLoginSuccess(name, mobileNumber, null)
                                                        }
                                                        .addOnFailureListener {
                                                            // Fallback to avoid getting stuck
                                                            onLoginSuccess("User", mobileNumber, null)
                                                        }
                                                } else {
                                                    onLoginSuccess("User", mobileNumber, null)
                                                }
                                            } else {
                                                val exception = task.exception
                                                var finalMessage = "Invalid mobile number or password."
                                                
                                                if (exception is com.google.firebase.auth.FirebaseAuthException) {
                                                    finalMessage = when (exception.errorCode) {
                                                        "ERROR_USER_NOT_FOUND" -> "User not found. Please sign up first."
                                                        "ERROR_WRONG_PASSWORD" -> "Invalid mobile number or password."
                                                        "INVALID_LOGIN_CREDENTIALS" -> "Invalid mobile number or password."
                                                        else -> exception.localizedMessage ?: "Login failed."
                                                    }
                                                }
                                                
                                                // General check for the message shown in the screenshot
                                                if (exception?.message?.contains("incorrect", ignoreCase = true) == true) {
                                                    finalMessage = "Invalid mobile number or password."
                                                }

                                                Toast.makeText(context, finalMessage, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(context, "Please enter all details", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        AuthState.SIGNUP -> SignUpForm(
                            name = fullName,
                            onNameChange = { fullName = it },
                            mobile = signUpMobile,
                            onMobileChange = { signUpMobile = it },
                            pass = signUpPassword,
                            onPassChange = { signUpPassword = it },
                            visible = signUpPasswordVisible,
                            onVisibleToggle = { signUpPasswordVisible = !signUpPasswordVisible },
                            onSignUp = {
                                if (signUpMobile.isNotBlank() && signUpPassword.isNotBlank()) {
                                    val email = "$signUpMobile@corpuscare.com"
                                    auth.createUserWithEmailAndPassword(email, signUpPassword)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val uid = task.result?.user?.uid
                                                if (uid != null) {
                                                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                    val userData = hashMapOf(
                                                        "name" to fullName,
                                                        "phone" to signUpMobile,
                                                        "age" to "",
                                                        "bloodGroup" to "",
                                                        "email" to "",
                                                        "addresses" to emptyList<Map<String, Any>>()
                                                    )
                                                    // Move success toast here to ensure it shows immediately
                                                    Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT).show()
                                                    
                                                    db.collection("users").document(uid).set(userData)
                                                        .addOnCompleteListener {
                                                            // After data is saved (or even if it fails, we move to login)
                                                            authState = AuthState.LOGIN
                                                        }
                                                }
                                            } else {
                                                Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        AuthState.ADMIN -> AdminForm(
                            id = adminId,
                            onIdChange = { adminId = it },
                            pass = adminPass,
                            onPassChange = { adminPass = it },
                            onLogin = {
                                val trimmedId = adminId.trim()
                                val trimmedPass = adminPass.trim()
                                
                                if (trimmedId.isNotBlank() && trimmedPass.isNotBlank()) {
                                    // RESTORED EMERGENCY BYPASS
                                    if (trimmedId == "admin01" && trimmedPass == "123456") {
                                        Toast.makeText(context, "Admin Authorized (Master Access)", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess("Mr. Raushan", "Admin", "admin01")
                                        return@AdminForm
                                    }

                                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    db.collection("admins").document(trimmedId).get()
                                        .addOnSuccessListener { doc ->
                                            if (doc.exists() && doc.getString("passcode") == trimmedPass) {
                                                Toast.makeText(context, "Admin Authorized", Toast.LENGTH_SHORT).show()
                                                onLoginSuccess(doc.getString("name") ?: "Admin Staff", "Admin", trimmedId)
                                            } else {
                                                Toast.makeText(context, "Invalid Admin Credentials", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .addOnFailureListener {
                                            // Fallback for Master Access even on network error
                                            if (trimmedId == "admin01" && trimmedPass == "123456") {
                                                onLoginSuccess("Mr. Raushan", "Admin", "admin01")
                                            } else {
                                                Toast.makeText(context, "Auth Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(context, "Please enter Admin ID and Passcode", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        AuthState.FACILITY -> FacilityForm(
                            id = facilityId,
                            onIdChange = { facilityId = it },
                            pass = facilityPass,
                            onPassChange = { facilityPass = it },
                            onLogin = {
                                val fId = facilityId.trim()
                                val fPass = facilityPass.trim()
                                
                                if (fId.isNotBlank() && fPass.isNotBlank()) {
                                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    // Check hospitals first
                                    db.collection("hospitals").document(fId).get()
                                        .addOnSuccessListener { doc ->
                                            if (doc.exists() && doc.getString("contact") == fPass) {
                                                Toast.makeText(context, "Hospital Authorized", Toast.LENGTH_SHORT).show()
                                                onLoginSuccess(doc.getString("name") ?: "Hospital", "Facility", fId)
                                            } else {
                                                // Check clinics
                                                db.collection("clinics").document(fId).get()
                                                    .addOnSuccessListener { cDoc ->
                                                        if (cDoc.exists() && cDoc.getString("contact") == fPass) {
                                                            Toast.makeText(context, "Clinic Authorized", Toast.LENGTH_SHORT).show()
                                                            onLoginSuccess(cDoc.getString("name") ?: "Clinic", "Facility", fId)
                                                        } else {
                                                            Toast.makeText(context, "Invalid Facility ID or Passcode", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                    .addOnFailureListener {
                                                        Toast.makeText(context, "Auth Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Auth Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Toast.makeText(context, "Please enter ID and Passcode", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Navigation Links
                AuthFooter(
                    currentState = authState,
                    onSwitch = { authState = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginForm(
    mobile: String, onMobileChange: (String) -> Unit,
    pass: String, onPassChange: (String) -> Unit,
    visible: Boolean, onVisibleToggle: () -> Unit,
    onLogin: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Welcome Back!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1E))
        Text("Login to your health portal", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = mobile, onValueChange = onMobileChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Mobile Number") },
            leadingIcon = { Icon(Icons.Default.Smartphone, null, tint = Color(0xFF006766)) },
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF006766))
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = pass, onValueChange = onPassChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF006766)) },
            trailingIcon = {
                IconButton(onClick = onVisibleToggle) {
                    Icon(if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                }
            },
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF006766))
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766))
        ) {
            Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpForm(
    name: String, onNameChange: (String) -> Unit,
    mobile: String, onMobileChange: (String) -> Unit,
    pass: String, onPassChange: (String) -> Unit,
    visible: Boolean, onVisibleToggle: () -> Unit,
    onSignUp: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Create Account", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1E))
        Text("Start your wellness journey", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = name, onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Full Name") },
            leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFF006766)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF006766))
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = mobile, onValueChange = onMobileChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Mobile Number") },
            leadingIcon = { Icon(Icons.Default.Smartphone, null, tint = Color(0xFF006766)) },
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF006766))
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = pass, onValueChange = onPassChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF006766)) },
            trailingIcon = {
                IconButton(onClick = onVisibleToggle) {
                    Icon(if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                }
            },
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF006766))
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSignUp,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766))
        ) {
            Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminForm(
    id: String, onIdChange: (String) -> Unit,
    pass: String, onPassChange: (String) -> Unit,
    onLogin: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Admin Access", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1E))
        Text("Secure portal for staff", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = id, onValueChange = onIdChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Admin ID") },
            leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null, tint = Color(0xFF1A1C1E)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF1A1C1E))
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = pass, onValueChange = onPassChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Access Code") },
            leadingIcon = { Icon(Icons.Default.VpnKey, null, tint = Color(0xFF1A1C1E)) },
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF1A1C1E))
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1C1E))
        ) {
            Text("Authorize", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacilityForm(
    id: String, onIdChange: (String) -> Unit,
    pass: String, onPassChange: (String) -> Unit,
    onLogin: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Facility Login", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1E))
        Text("Hospital & Clinic Portal", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = id, onValueChange = onIdChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Hospital/Clinic ID") },
            leadingIcon = { Icon(Icons.Default.LocalHospital, null, tint = Color(0xFF006766)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF006766))
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = pass, onValueChange = onPassChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Contact Number (Passcode)") },
            leadingIcon = { Icon(Icons.Default.VpnKey, null, tint = Color(0xFF006766)) },
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF006766))
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006766))
        ) {
            Text("Login Portal", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AuthFooter(currentState: AuthState, onSwitch: (AuthState) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            val text = if (currentState == AuthState.SIGNUP) "Already have an account? " else "Don't have an account? "
            val action = if (currentState == AuthState.SIGNUP) "Log In" else "Sign Up"
            val target = if (currentState == AuthState.SIGNUP) AuthState.LOGIN else AuthState.SIGNUP
            
            Text(text, color = Color.Gray, fontSize = 14.sp)
            Text(
                action,
                color = Color(0xFF006766),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onSwitch(target) }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (currentState != AuthState.ADMIN && currentState != AuthState.FACILITY) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Admin Login",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onSwitch(AuthState.ADMIN) }
                )
                Text(
                    "Facility Login",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onSwitch(AuthState.FACILITY) }
                )
            }
        } else {
            Text(
                "Back to User Login",
                color = Color.LightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onSwitch(AuthState.LOGIN) }
            )
        }
    }
}

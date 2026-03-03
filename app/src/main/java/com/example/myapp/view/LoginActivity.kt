//package com.example.myapp.view
//
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.HorizontalDivider
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextFieldDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.input.VisualTransformation
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.myapp.ui.theme.Blue
//import com.example.myapp.ui.theme.Purple80
//import com.example.myapp.ui.theme.White
////import com.google.firebase.auth.FirebaseAuth
//
//class LoginActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            LoginBody()
//        }
//    }
//}
//
//@Composable
//fun LoginBody() {
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var visibility by remember { mutableStateOf(false) }
//
//    val context = LocalContext.current
//    val activity = context as Activity
//
//    Scaffold { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .background(White),
//                horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Spacer(modifier = Modifier.height(40.dp))
//            Image(
//                painter = painterResource(id = com.example.myapp.R.drawable.logo2),
//                contentDescription = "Finance Manager Logo",
//                modifier = Modifier.size(80.dp)
//            )
//            Spacer(modifier = Modifier.height(20.dp))
//            Text(
//                "SIGN IN",
//                modifier = Modifier.fillMaxWidth(),
//                style = TextStyle(
//                    textAlign = TextAlign.Center,
//                    fontSize = 32.sp,
//                    color = Blue,
//                    fontWeight = FontWeight.Bold
//                )
//            )
//
//            Text(
//                "Manage your finances securely. Login to access your personal finance dashboard.",
//                style = TextStyle(
//                    textAlign = TextAlign.Center,
//                    color = Color.Gray.copy(0.8f),
//                    fontSize = 14.sp
//                ),
//                modifier = Modifier.padding(vertical = 20.dp, horizontal = 15.dp)
//            )
//            Spacer(modifier = Modifier.height(10.dp))
//            OutlinedTextField(
//                value = email,
//                onValueChange = { data ->
//                    email = data
//                },
//                label = { Text("Email") },
//                placeholder = {
//                    Text("abc@gmail.com")
//                },
//                colors = TextFieldDefaults.colors(
//                    unfocusedContainerColor = Purple80,
//                    focusedContainerColor = Purple80,
//                    focusedIndicatorColor = Blue,
//                    unfocusedIndicatorColor = Color.Transparent
//                ),
//                shape = RoundedCornerShape(12.dp),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 15.dp)
//            )
//            Spacer(modifier = Modifier.height(20.dp))
//
//            OutlinedTextField(
//                value = password,
//                onValueChange = { data ->
//                    password = data
//                },
//                label = { Text("Password") },
//                placeholder = {
//                    Text("*******")
//                },
//
//                colors = TextFieldDefaults.colors(
//                    unfocusedContainerColor = Purple80,
//                    focusedContainerColor = Purple80,
//                    focusedIndicatorColor = Blue,
//                    unfocusedIndicatorColor = Color.Transparent
//                ),
//                shape = RoundedCornerShape(12.dp),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 15.dp),
//                visualTransformation = if (visibility) VisualTransformation.None else PasswordVisualTransformation(),
//                trailingIcon = {
//                    IconButton(onClick = {
//                        visibility = !visibility
//                    }) {
//                        Icon(
//                            painter = if (visibility)
//                                painterResource(com.example.myapp.R.drawable.baseline_visibility_off_24)
//                            else painterResource(
//                                com.example.myapp.R.drawable.baseline_visibility_24
//                            ),
//
//
//                            contentDescription = null
//                        )
//                    }
//                }
//            )
//            Spacer(modifier = Modifier.height(30.dp))
//            Button(onClick = {
//                if (email.isEmpty() || password.isEmpty()) {
//                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
//                } else {
////                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
////                        .addOnCompleteListener { task ->
////                            if (task.isSuccessful) {
////                                Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
////                                // Navigate to main activity or dashboard
////                            } else {
////                                Toast.makeText(context, task.exception?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
////                            }
////                        }
//                }
//            },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 15.dp)
//            ) {
//                Text("Sign In")
//            }
//            Spacer(modifier = Modifier.height(20.dp))
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 15.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                HorizontalDivider(
//                    modifier = Modifier.weight(1f)
//                )
//                Text("OR", modifier = Modifier.padding(horizontal = 15.dp))
//                HorizontalDivider(
//                    modifier = Modifier.weight(1f)
//                )
//            }
//            Spacer(modifier = Modifier.height(20.dp))
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 15.dp)
//            ) {
//
//                Card(
//                    modifier = Modifier
//                        .height(60.dp)
//                        .weight(1f)
//                ) {
//                    Row(
//                        modifier = Modifier.fillMaxSize(),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.Center
//                    ) {
//                        Image(
//                            painter = painterResource(com.example.myapp.R.drawable.gmail),
//                            contentDescription = null,
//                            modifier = Modifier.size(30.dp)
//                        )
//                        Spacer(modifier = Modifier.width(15.dp))
//                        Text("Google")
//                    }
//                }
//
//            }
//            Spacer(modifier = Modifier.height(20.dp))
//            Text("Don't have an account? Sign up", modifier = Modifier.clickable {
//                val intent = Intent(
//                    context,
//                    Registration::class.java
//                )
//
//                context.startActivity(intent)
//
//            })
//
//            Spacer(modifier = Modifier.height(20.dp))
//            Text(
//                "Forgot Password?",
//                color = Blue,
//                modifier = Modifier
//                    .clickable {
//                        val intent = Intent(context, ForgetPassword::class.java)
//                        context.startActivity(intent)
//                    }
//            )
//
//
//        }
//    }
//}
//
//@Preview
//@Composable
//fun PreviewLogin() {
//    LoginBody()
//}
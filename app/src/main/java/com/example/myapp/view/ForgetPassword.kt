//package com.example.myapp.view
//
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.material3.HorizontalDivider
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.sp
//import com.example.myapp.ui.theme.Blue
//import com.example.myapp.ui.theme.Purple80
//import com.example.myapp.ui.theme.White
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Button
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
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import com.example.myapp.ui.theme.Blue
//import com.example.myapp.ui.theme.White
////import com.google.firebase.auth.FirebaseAuth
//
//
//class ForgetPassword : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            forgetPassword()
//        }
//    }
//}
//
//@Composable
//fun forgetPassword() {
//    var email by remember { mutableStateOf("") }
//
//    val context = LocalContext.current
//
//    Scaffold { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .background(White),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Spacer(modifier = Modifier.height(40.dp))
//            Image(
//                painter = painterResource(id = com.example.myapp.R.drawable.logo2),
//                contentDescription = "Finance Manager Logo",
//                modifier = Modifier.size(80.dp)
//            )
//            Spacer(modifier = Modifier.height(20.dp))
//            Text(
//                "Reset Password",
//                modifier = Modifier.fillMaxWidth(),
//                style = TextStyle(
//                    textAlign = TextAlign.Center,
//                    fontSize = 28.sp,
//                    color = Blue,
//                    fontWeight = FontWeight.Bold
//                )
//            )
//
//            Text(
//                "Enter your email and we will send you a reset password link.",
//                style = TextStyle(
//                    textAlign = TextAlign.Center,
//                    color = Color.Gray.copy(0.8f),
//                    fontSize = 14.sp
//                ),
//                modifier = Modifier.padding(vertical = 20.dp, horizontal = 15.dp)
//            )
//
//            Spacer(modifier = Modifier.height(10.dp))
//            OutlinedTextField(
//                value = email,
//                onValueChange = { email = it },
//                label = { Text("Email") },
//                placeholder = { Text("abc@gmail.com") },
//                shape = RoundedCornerShape(12.dp),
//                colors = TextFieldDefaults.colors(
//                    unfocusedContainerColor = Purple80,
//                    focusedContainerColor = Purple80,
//                    focusedIndicatorColor = Blue,
//                    unfocusedIndicatorColor = Color.Transparent
//                ),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 15.dp)
//            )
//
//            Spacer(modifier = Modifier.height(30.dp))
//            Button(
//                onClick = {
//                    if (email.isEmpty()) {
//                        Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
//                    } else {
////                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
////                            .addOnCompleteListener { task ->
////                                if (task.isSuccessful) {
////                                    Toast.makeText(context, "Reset email sent successfully", Toast.LENGTH_SHORT).show()
////                                } else {
////                                    Toast.makeText(context, task.exception?.message ?: "Error", Toast.LENGTH_SHORT).show()
////                                }
////                            }
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 15.dp)
//            ) {
//                Text("Send Reset Link")
//            }
//
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
//
//            Spacer(modifier = Modifier.height(20.dp))
//            Button(
//                onClick = {
//                    (context as? ComponentActivity)?.finish()
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 15.dp)
//            ) {
//                Text("Back to Login")
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun forgetPasswordPreview() {
//    forgetPassword()
//}
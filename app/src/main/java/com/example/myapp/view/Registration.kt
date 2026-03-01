package com.example.myapp.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myApp.viewmodel.UserViewModel
import com.example.myapp.R
import com.example.myapp.model.UserModel
import com.example.myapp.repository.UserRepoImpl

import com.example.myapp.ui.theme.Blue
import com.example.myapp.ui.theme.Purple80
import com.example.myapp.ui.theme.White
import java.util.Calendar

class Registration : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegistrationBody()
        }
    }
}

@Composable
fun RegistrationBody() {
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(false) }
    var checkbox by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    var selectedDate by remember { mutableStateOf("") }

    val datepicker = remember {
        DatePickerDialog(context, { _, y, m, d ->
            selectedDate = "$d/${m + 1}/$y"
        }, year, month, day)
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Image(
                painter = painterResource(id = com.example.myapp.R.drawable.logo2),
                contentDescription = "Finance Manager Logo",
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Sign Up",
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp,
                    color = Blue,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                "Create your account to start managing your finances securely.",
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    color = Color.Gray.copy(0.8f),
                    fontSize = 14.sp
                ),
                modifier = Modifier.padding(vertical = 20.dp, horizontal = 15.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                placeholder = { Text("abc@gmail.com") },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Purple80,
                    focusedContainerColor = Purple80,
                    focusedIndicatorColor = Blue,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
                .clickable { datepicker.show() }
            ) {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { selectedDate = it },
                    label = { Text("Date of Birth") },
                    placeholder = { Text("dd/mm/yyyy") },
                    enabled = false,
                    colors = TextFieldDefaults.colors(
                        disabledContainerColor = Purple80,
                        disabledIndicatorColor = Color.Transparent,
                        unfocusedContainerColor = Purple80,
                        focusedContainerColor = Purple80,
                        focusedIndicatorColor = Blue,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
//            OutlinedTextField(
//                value = selectedDate,
//                onValueChange = { selectedDate = it },
//                label = { Text("Date of Birth") },
//                placeholder = { Text("dd/mm/yyyy") },
//                enabled = false,
//                colors = TextFieldDefaults.colors(
//                    disabledContainerColor = Purple80,
//                    disabledIndicatorColor = Color.Transparent,
//                    unfocusedContainerColor = Purple80,
//                    focusedContainerColor = Purple80,
//                    focusedIndicatorColor = Blue,
//                    unfocusedIndicatorColor = Color.Transparent
//                ),
//                shape = RoundedCornerShape(12.dp),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable { datepicker.show() }
//                    .padding(horizontal = 15.dp)
//            )

            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                placeholder = { Text("*******") },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Purple80,
                    focusedContainerColor = Purple80,
                    focusedIndicatorColor = Blue,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                visualTransformation = if (visibility) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { visibility = !visibility }) {
                        Icon(
                            painter = if (visibility)
                                painterResource(R.drawable.baseline_visibility_off_24)
                            else painterResource(R.drawable.baseline_visibility_24),
                            contentDescription = null
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checkbox,
                    onCheckedChange = { checkbox = it },
                    colors = CheckboxDefaults.colors(
                        checkmarkColor = White,
                        checkedColor = Color.Green
                    )
                )
                Text("I agree to terms & conditions")
            }

            Spacer(modifier = Modifier.height(30.dp))
            Button(onClick = {
                if (!checkbox) {
                    Toast.makeText(
                        context,
                        "Please accept terms & conditions",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    userViewModel.register(email,password){
                            success,message,userId->
                        if(success){
                            val model = UserModel(
                                id = userId,
                                dob = selectedDate,
                                email = email,
                            )
                            userViewModel.addUserToDatabase(userId,model){
                                    success,message ->
                                if(success){
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                                else{
                                    Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }else{
                             Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }) {
                Text("Register")
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text("OR", modifier = Modifier.padding(horizontal = 15.dp))
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Already have an account? Sign in", modifier = Modifier.clickable {
                (context as? ComponentActivity)?.finish()
            })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationPreview() {
    RegistrationBody()
}
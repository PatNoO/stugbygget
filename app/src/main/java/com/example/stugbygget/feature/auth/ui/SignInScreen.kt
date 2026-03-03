package com.example.stugbygget.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SignInScreen(
    email: String,
    password: String,
    isLoading: Boolean,
    errorMessage: String?,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSignInClick: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "StugBygget", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Sign in with email and password to access project data",
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChanged,
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChanged,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Button(
            onClick = { onSignInClick(email, password) },
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(2.dp))
            } else {
                Text("Sign In")
            }
        }
    }
}

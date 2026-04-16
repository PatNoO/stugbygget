package com.example.stugbygget.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import com.example.stugbygget.ui.components.SommarTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.stugbygget.R

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
            text = stringResource(R.string.auth_subtitle),
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        SommarTextField(
            value = email,
            onValueChange = onEmailChanged,
            label = { Text(stringResource(R.string.auth_field_email)) },
            singleLine = true,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SommarTextField(
            value = password,
            onValueChange = onPasswordChanged,
            label = { Text(stringResource(R.string.auth_field_password)) },
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
                Text(stringResource(R.string.auth_button_sign_in))
            }
        }
    }
}

package com.example.sonicflow.presentation.signin

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.sonicflow.R

@Composable
fun SignInScreen(
    onNavigateToSignUp: () -> Unit,
    onSignInSuccess: () -> Unit,
){
    var email by remember{mutableStateOf("")}
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = stringResource(R.string.sign_in_title),
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // TODO: Vérifier les credentials
                onSignInSuccess()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.sign_in_button))
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToSignUp) {
            Text(stringResource(R.string.no_account))
        }
    }
}
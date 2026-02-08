package com.example.sonicflow.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.annotation.StringRes
import com.example.sonicflow.R
import androidx.navigation.NavController
import com.example.sonicflow.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    navController: NavController,
    onDismiss: () -> Unit
){

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 32.dp,
                    top = 0.dp
                ))
        ) {

            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            SectionTitle(R.string.settings_account)

            SettingsItem(
                icon = Icons.Outlined.Login,
                titleRes = R.string.settings_sign_in,
                subtitleRes = R.string.settings_sign_in_desc,
                onClick = {
                    onDismiss()
                    navController.navigate(Screen.SignIn.route)
                }
            )

            SettingsItem(
                icon = Icons.Outlined.PersonAdd,
                titleRes = R.string.settings_sign_up,
                subtitleRes = R.string.settings_sign_up_desc,
                onClick = {
                    onDismiss()
                    navController.navigate(Screen.SignUp.route)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle(R.string.settings_preferences)

            SettingsItem(
                icon = Icons.Outlined.Notifications,
                titleRes = R.string.settings_notifications,
                subtitleRes = R.string.settings_notifications_desc,
                onClick = {}
            )

            SettingsItem(
                icon = Icons.Outlined.Palette,
                titleRes = R.string.settings_theme,
                subtitleRes = R.string.settings_theme_desc,
                onClick = {}
            )

            SettingsItem(
                icon = Icons.Outlined.MusicNote,
                titleRes = R.string.settings_audio_quality,
                subtitleRes = R.string.settings_audio_quality_desc,
                onClick = {}
            )

            SettingsItem(
                icon = Icons.Outlined.Storage,
                titleRes = R.string.settings_storage,
                subtitleRes = R.string.settings_storage_desc,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle(R.string.settings_other)

            SettingsItem(
                icon = Icons.Outlined.Info,
                titleRes = R.string.settings_about,
                subtitle = stringResource(R.string.settings_version, "1.0.0"),
                onClick = {}
            )

            SettingsItem(
                icon = Icons.Outlined.Help,
                titleRes = R.string.settings_help,
                subtitleRes = R.string.settings_help_desc,
                onClick = {}
            )

            SettingsItem(
                icon = Icons.Outlined.Policy,
                titleRes = R.string.settings_privacy,
                onClick = {}
            )
        }
    }
}

@Composable
private fun SectionTitle(@StringRes titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int? = null,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )

                val subtitleText = subtitle ?: subtitleRes?.let { stringResource(it) }

                if (subtitleText != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

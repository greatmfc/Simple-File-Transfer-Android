package com.sft.sft_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainPage()
        }
    }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                "Simple File Transfer Android",
            )
        },
        actions = {
            IconButton(
                onClick = { expanded = !expanded } //do something
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_about)) },
                    onClick = {
                        showDialog = true
                    }
                )
                if (showDialog) {
                    CreateAlertDialog(
                        onConfirmation = {
                            showDialog = false
                            expanded = false
                        },
                        dialogTitle = stringResource(R.string.menu_about),
                        dialogText = "Simple File Transfer Android\n" +
                                "Developed by greatmfc.",
                    )
                }

            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Receive button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            FilledTonalButton(
                modifier = Modifier
                    .width(155.dp)
                    .height(60.dp),
                onClick = {
                    val intent = Intent(context, ReceiveActivity::class.java)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    colorResource(
                        com.google.android.material.R.color.material_dynamic_primary70
                    )
                )
            ) {
                Text(stringResource(R.string.rbtn_str), fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(150.dp))

        // Transfer button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            FilledTonalButton(
                modifier = Modifier
                    .width(155.dp)
                    .height(60.dp),
                onClick = {
                    val intent = Intent(context, TransferActivity::class.java)
                    context.startActivity(intent)
                },
                colors =
                ButtonDefaults.buttonColors(
                    colorResource(
                        android.R.color.holo_green_light
                    )
                ),
            ) {
                Text(stringResource(R.string.tbtn_str), fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun CreateAlertDialog(
    onDismissRequest: () -> Unit = {},
    onConfirmation: () -> Unit = {},
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector = Icons.Default.Build,
    enableDismiss: Boolean = false,
    enableConfirm: Boolean = true,
) {
    AlertDialog(
        icon = { Icon(icon, contentDescription = "Example Icon") },
        onDismissRequest = {
            onDismissRequest()
        },
        title = {
            Text(dialogTitle)
        },
        text = {
            Text(
                text = dialogText,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                },
                enabled = enableDismiss,
            ) {
                Text(stringResource(R.string.dismiss_str))
            }

        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                },
                enabled = enableConfirm
            ) {
                Text(stringResource(R.string.confirm_str))
            }
        },
    )

}

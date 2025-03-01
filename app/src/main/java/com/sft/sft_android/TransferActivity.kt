package com.sft.sft_android

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket


class TransferActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TransferPage()
        }
    }

}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferPage(
) {
    var ipAddress by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }
    var isValidIP by remember { mutableStateOf(true) }
    var isValidPort by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var connectionStatus by remember { mutableIntStateOf(R.string.conn_status_str_no) }
    var tcpSocket by remember { mutableStateOf<Socket?>(null) }
    val udpSocket = DatagramSocket(null)
    var isError by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var ableToConnect by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var hostName by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    var fileSize by remember { mutableStateOf<Long?>(null) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            selectedFileUri = uri
            uri?.let {
                // 获取文件名和文件大小
                fileName = getFileNameFromUri(context, it)
                fileSize = getFileSizeFromUri(context, it)
            }
        }
    )

    TopAppBar(
        title = {
            Text(
                stringResource(R.string.tbtn_str),
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    (context as? Activity)?.finish()
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        },
    )

    //Texts
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(150.dp))

        Text(
            text = stringResource(connectionStatus),
            modifier = Modifier.fillMaxWidth(),
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = stringResource(R.string.select_file_str) + fileName,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

    }

    //Middle column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        //IP and Port
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = ipAddress,
                onValueChange = { newValue ->
                    if (newValue.length <= 22 && newValue.matches(Regex("^[0-9.]*$"))) {
                        ipAddress = newValue
                        isValidIP =
                            newValue.matches(Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"))
                    }
                },
                label = { Text(stringResource(R.string.ip_str)) },
                isError = !isValidIP,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(0.7f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 端口号输入框
            OutlinedTextField(
                value = port,
                onValueChange = { newValue ->
                    if (newValue.length <= 5 && newValue.matches(Regex("^[0-9]*$"))) {
                        port = newValue
                        isValidPort = newValue.isNotEmpty() && newValue.toIntOrNull() in 0..65535
                    }
                },
                label = { Text(stringResource(R.string.port_str)) },
                isError = !isValidPort,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(0.3f)
            )

            ableToConnect = isValidIP && isValidPort && ipAddress.isNotEmpty() && port.isNotEmpty()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Connect button
        FilledTonalButton(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    loading = true
                    try {
                        // 创建 Socket 连接
                        tcpSocket = Socket(ipAddress, port.toInt())
                        ipAddress = ""
                        port = ""
                        connectionStatus = R.string.conn_status_str_done
                    } catch (e: Exception) {
                        isError = true
                        errorMessage = e.toString()
                    }
                    loading = false
                    // 在协程中执行网络连接
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = ableToConnect,
            colors =
            ButtonDefaults.buttonColors(
                colorResource(android.R.color.holo_orange_light)
            ),
        ) {
            Text(
                stringResource(R.string.connbtn_str),
            )
            if (isError) {
                CreateAlertDialog(
                    dialogTitle = stringResource(R.string.error_str),
                    dialogText = errorMessage,
                    icon = Icons.Default.Clear,
                    onConfirmation = { isError = false }
                )
            }
            if (isSuccess) {
                CreateAlertDialog(
                    dialogTitle = stringResource(R.string.success_str),
                    dialogText = fileName + stringResource(R.string.success_transfer_msg_str),
                    icon = Icons.Default.Done,
                    onConfirmation = { isSuccess = false }
                )
            }
            LaunchedEffect(selectedFileUri, tcpSocket) {
                if (selectedFileUri != null && tcpSocket != null) {
                    try {
                        CoroutineScope(Dispatchers.IO).launch {
                            val socketOutput = tcpSocket!!.getOutputStream()
                            val socketInput = tcpSocket!!.getInputStream()
                            val contentResolver = context.contentResolver
                            val fileInput = contentResolver.openInputStream(selectedFileUri!!)
                            val request = "sft1.0/FIL/$fileName/$fileSize\r\n"

                            socketOutput.write(request.toByteArray())
                            loading = true
                            socketInput.read()
                            fileInput?.use { input ->
                                socketOutput.use { output ->
                                    input.copyTo(output)
                                }
                            }
                            tcpSocket!!.close()
                            tcpSocket = null
                            connectionStatus = R.string.conn_status_str_no
                            loading = false
                            isSuccess = true
                        }
                    } catch (e: Exception) {
                        connectionStatus = R.string.conn_status_str_no
                        loading = false
                        isError = true
                        errorMessage = e.toString()
                    }
                }
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        // Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }

    //Bottom column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        //Auto search button
        FilledTonalButton(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        loading = true
                        udpSocket.broadcast = true
                        udpSocket.reuseAddress = true
                        udpSocket.bind(InetSocketAddress(7897))
                        val discoverHeader =
                            "sft1.0/DIS/${Build.BRAND}${Build.MODEL}/${
                                htons(7897)
                            }\r\n".toByteArray()
                        val broadcastAddress = InetAddress.getByName("255.255.255.255")
                        var packet =
                            DatagramPacket(
                                discoverHeader,
                                discoverHeader.size,
                                broadcastAddress,
                                7897
                            )
                        udpSocket.send(packet)
                        val buf = ByteArray(64)
                        packet = DatagramPacket(buf, buf.size)
                        udpSocket.receive(packet)
                        buf.fill(0)
                        udpSocket.receive(packet)
                        val header = buf.toString(Charsets.UTF_8)
                        val respondedHeader = header.split("/")
                        port =
                            respondedHeader[3].substringBefore("\r\n")
                        port = ntohs(port.toInt()).toString()
                        ipAddress = packet.address.toString().removePrefix("/")
                        hostName = respondedHeader[2]
                        showDialog = true
                    } catch (e: Exception) {
                        isError = true
                        errorMessage = e.toString()
                    }
                    loading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading,
            colors =
            ButtonDefaults.buttonColors(
                colorResource(android.R.color.holo_blue_light)
            ),
        ) {
            Text(
                stringResource(R.string.autosearch_str),
            )

            if (showDialog) {
                CreateAlertDialog(
                    onDismissRequest = {
                        ipAddress = ""
                        port = ""
                        showDialog = !showDialog
                    },
                    onConfirmation = {
                        ableToConnect = true
                        showDialog = !showDialog
                    },
                    enableDismiss = true,
                    dialogTitle = stringResource(R.string.available_str),
                    dialogText = "Host: $hostName IP: $ipAddress Port:$port",
                    icon = Icons.Default.Notifications
                )
            }
        }

        Spacer(modifier = Modifier.height(50.dp))

        ExtendedFloatingActionButton(
            onClick = {
                filePickerLauncher.launch("*/*")
            },
            icon = { Icon(Icons.Filled.Add, "Extended floating action button.") },
            text = { Text(stringResource(R.string.add_FAB_str)) },
        )

        Spacer(modifier = Modifier.height(150.dp))
    }
}

/**
 * 从 Uri 获取文件名
 */
@SuppressLint("Range")
private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        if (it.moveToFirst()) {
            it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        } else {
            null
        }
    }
}

/**
 * 从 Uri 获取文件大小
 */
@SuppressLint("Range")
private fun getFileSizeFromUri(context: Context, uri: Uri): Long? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        if (it.moveToFirst()) {
            it.getLong(it.getColumnIndex(OpenableColumns.SIZE))
        } else {
            null
        }
    }
}

fun htons(value: Int): Int {
    return ((value and 0xFF) shl 8) or ((value and 0xFF00) ushr 8)
}

fun ntohs(value: Int): Int {
    return htons(value) // ntohs 和 htons 是相同的操作
}
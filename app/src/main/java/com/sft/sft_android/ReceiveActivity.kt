package com.sft.sft_android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.ServerSocket
import java.net.Socket

private var incomingSocket: Socket? = null
private val tcpPortNumber = 9007
private val udpPortNumber = 7897
private val tcpSocket = ServerSocket(tcpPortNumber)
private val udpSocket = DatagramSocket(udpPortNumber)

class ReceiveActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            ReceivePage()
        }
    }

}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivePage() {
    val context = LocalContext.current
    val tcpIPAddress = getWifiIpAddress(context)
    val maxArraySize = 200000000 //300MB
    var isLoading by remember { mutableStateOf(false) }
    var isConnection by remember { mutableStateOf(false) }
    var startToReceive by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var startToListen by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var fileName by remember { mutableStateOf("") }
    var currentProgress by remember { mutableStateOf(0f) }

    TopAppBar(
        title = {
            Text(
                stringResource(R.string.rbtn_str),
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
            text = "${stringResource(R.string.ip_str)}: $tcpIPAddress",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "${stringResource(R.string.port_str)}: $tcpPortNumber",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

    }

    // Start receive button
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            FilledTonalButton(
                onClick = {
                    startToListen = true
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            incomingSocket = tcpSocket.accept()
                            isConnection = true
                        } catch (e: Exception) {
                            isError = true
                            errorMessage = e.toString()
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        val buffer = ByteArray(128)
                        val receivedPacket = DatagramPacket(buffer, 127)
                        udpSocket.receive(receivedPacket)
                        val respondHeader =
                            "sft1.0/RES/${Build.BRAND}${Build.MODEL}/${htons(tcpPortNumber)}\r\n"
                        receivedPacket.data = respondHeader.toByteArray()
                        udpSocket.send(receivedPacket)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !startToListen
            ) {
                Text(
                    stringResource(R.string.start_receive_str),
                )

                if (isConnection) {
                    CreateAlertDialog(
                        onDismissRequest = {
                            incomingSocket!!.close()
                            isConnection = !isConnection
                        },
                        onConfirmation = {
                            startToReceive = true
                            isConnection = !isConnection
                        },
                        dialogTitle = stringResource(R.string.incoming_str),
                        dialogText = incomingSocket!!.remoteSocketAddress.toString()
                            .removePrefix("/"),
                        icon = Icons.Default.Warning,
                        enableDismiss = true,
                    )
                }
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
                        dialogText = fileName + stringResource(R.string.success_receive_msg_str),
                        icon = Icons.Default.Done,
                        onConfirmation = { isSuccess = false }
                    )
                }

                LaunchedEffect(startToReceive) {
                    if (startToReceive) {
                        CoroutineScope(Dispatchers.IO).launch {
                            isLoading = true
                            try {
                                val tcpInputStream = incomingSocket!!.getInputStream()
                                val requestBuffer = ByteArray(128)
                                tcpInputStream.read(requestBuffer)
                                val requestMessage =
                                    requestBuffer.toString(Charsets.UTF_8).split("/")
                                fileName = requestMessage[2]
                                val fileSize = requestMessage[3].substringBefore("\r\n").toInt()
                                incomingSocket!!.getOutputStream().write(49) //'1'


                                val parent =
                                    Environment.getExternalStorageDirectory().path.toString()
                                val child = "$parent/sft"
                                File(child).mkdirs()
                                val fileInstance = File("$child/$fileName")
                                fileInstance.createNewFile()
                                val outputFileWriter = FileOutputStream(fileInstance)
                                var bytesReceived = 0
                                var bytesLeft = fileSize
                                var ret = 0

                                if (fileSize < maxArraySize) {
                                    val bufferForFile = ByteArray(fileSize)
                                    while (bytesLeft > 0) {
                                        ret = tcpInputStream.read(
                                            bufferForFile,
                                            bytesReceived,
                                            bytesLeft
                                        )
                                        bytesReceived += ret
                                        bytesLeft -= ret
                                        currentProgress = bytesReceived.toFloat() / fileSize
                                    }
                                    outputFileWriter.write(bufferForFile)
                                } else {
                                    val bufferForFile = ByteArray(maxArraySize)
                                    var bytesWritten: Long = 0
                                    while (true) {
                                        var currentReturn = 0
                                        while (ret < maxArraySize) {
                                            currentReturn =
                                                tcpInputStream.read(
                                                    bufferForFile,
                                                    ret,
                                                    maxArraySize - ret
                                                )
                                            if (currentReturn <= 0) break
                                            ret += currentReturn
                                            if (ret + bytesWritten >= fileSize) break
                                        }
                                        if (currentReturn <= 0) break
                                        outputFileWriter.write(bufferForFile, 0, ret)
                                        bytesWritten += ret
                                        if (bytesWritten >= fileSize) break
                                        currentProgress = bytesWritten.toFloat() / fileSize
                                        bufferForFile.fill(0)
                                        ret = 0
                                    }
                                }
                                outputFileWriter.close()
                                isSuccess = true
                            } catch (e: Exception) {
                                isError = true
                                errorMessage = e.toString()
                            }
                            incomingSocket!!.close()
                            startToReceive = false
                            isLoading = false
                            startToListen = false
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            if (isLoading) {
                LinearProgressIndicator(
                    progress = { currentProgress },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

fun getWifiIpAddress(context: Context): String? {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork
    val linkProperties = connectivityManager.getLinkProperties(activeNetwork)

    // 遍历 LinkProperties 中的地址，筛选 IPv4 地址
    linkProperties?.linkAddresses?.forEach { linkAddress ->
        val address = linkAddress.address
        if (address is Inet4Address) { // 检查是否为 IPv4 地址
            return address.hostAddress
        }
    }

    return null // 未找到 IPv4 地址
}
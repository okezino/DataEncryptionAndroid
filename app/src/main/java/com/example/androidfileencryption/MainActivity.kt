package com.example.androidfileencryption

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidfileencryption.ui.theme.AndroidFileEncryptionTheme
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cryptoManager = KeyManager()
        setContent {
            AndroidFileEncryptionTheme {
                // A surface container using the 'background' color from the theme

                var messageToEncrypt by remember {
                    mutableStateOf("")
                }

                var messageToDecrypt by remember {
                    mutableStateOf("")
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {

                    TextField(value = messageToEncrypt,
                        onValueChange = {messageToEncrypt = it},
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {Text(text = "Encrypt string")})

                    Spacer(modifier = Modifier.width(8.dp))

                    Row{
                        Button(onClick = {
                            val byte = messageToEncrypt.encodeToByteArray()
                            val file = File(filesDir, "secret.txt")
                            if(!file.exists()){
                                file.createNewFile()
                            }

                            val fos = FileOutputStream(file)
                            messageToDecrypt = cryptoManager.encrypt(byte, fos).decodeToString()
                        }, modifier = Modifier.padding(10.dp)) {
                            Text(text = "Encrypt")
                        }

                        Button(onClick = {

                            val file = File(filesDir, "secret.txt")
                            messageToEncrypt = cryptoManager.decrypt(
                                FileInputStream(file)
                            ).decodeToString()

                         }, modifier = Modifier.padding(10.dp)) {
                            Text(text = "Decrypt")
                        }
                    }

                    Text(text = messageToDecrypt)

                }

            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AndroidFileEncryptionTheme {
        Greeting("Android")
    }
}
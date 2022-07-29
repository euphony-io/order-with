package com.euphonyio.orderwith

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import co.euphony.rx.AcousticSensor
import co.euphony.rx.EuRxManager
import com.euphonyio.orderwith.data.DBUtil
import com.euphonyio.orderwith.ui.theme.OrderWithTheme
import kotlinx.coroutines.launch

class StoreActivity : ComponentActivity() {

    private val rxManager: EuRxManager by lazy {
        EuRxManager()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rxManager.listen()
        rxManager.acousticSensor = AcousticSensor {
            Toast.makeText(applicationContext, "수신완료", Toast.LENGTH_LONG).show()
        }

        setContent {
            OrderWithTheme {
                // showMenuDialog()
            }
        }
    }
}

// 메뉴 주문 다이얼로그 버튼 이걸로 사용하시면 됩니다!
@Composable
fun showMenuDialog() {
    val visible = remember { mutableStateOf(false) }
    if (visible.value) {
        DialogContent(
            onDismissRequest = { visible.value = false }
        )
    }
    Surface {
        Button(onClick = { visible.value = true }) {
            Text(text = "test")
        }
    }
}

@Composable
fun DialogContent(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
) {
    var textName by remember { mutableStateOf("") }
    var textDescription by remember { mutableStateOf("") }
    var textCost by remember { mutableStateOf("") }
    val util = DBUtil(context = LocalContext.current)
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = properties
    ) {
        Surface(
            color = Color.White
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TextField(
                    value = textName,
                    onValueChange = { textName = it },
                    label = { Text("Name") },
                    modifier = Modifier
                        .padding(all = 10.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White
                    )
                )
                TextField(
                    value = textDescription,
                    onValueChange = { textDescription = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .padding(all = 10.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White
                    )

                )
                TextField(
                    value = textCost,
                    onValueChange = { textCost = it },
                    label = { Text("cost") },
                    modifier = Modifier
                        .padding(all = 10.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row {
                    Button(
                        modifier = Modifier
                            .size(100.dp, 50.dp),
                        onClick = {
                            coroutineScope.launch {
                                val menu = util.getAllMenu()
                                Log.d("menu", menu.toString())
                                onDismissRequest()
                            }

                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                    ) {
                        Text("CANCEL")
                    }
                    Spacer(modifier = Modifier.size(20.dp))
                    Button(
                        modifier = Modifier
                            .size(100.dp, 50.dp),
                        onClick = {
                            coroutineScope.launch {
                                util.addMenu(
                                    name = textName,
                                    description = textDescription,
                                    cost = textCost.toInt()
                                )
                                onDismissRequest()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                    ) {
                        Text("ADD")
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview3() {
    OrderWithTheme {
        showMenuDialog()
    }
}
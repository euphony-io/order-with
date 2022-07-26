package com.euphonyio.orderwith

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.euphonyio.orderwith.ui.theme.OrderWithTheme
//import euphony.lib.receiver.AcousticSensor
//import euphony.lib.receiver.EuRxManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Main()
        }
    }
}

@Composable
fun Main() {

    val context = LocalContext.current

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
            ){
        Text(
            modifier = Modifier
                .padding(bottom = 30.dp),
            textAlign = TextAlign.Center,
            text = "Order-With",
            style = MaterialTheme.typography.h3
        )
        Row {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(painterResource(R.drawable.customer),"content description")
                Button(
                    modifier = Modifier.padding(
                    top = 16.dp,
                    start = 30.dp,
                    end = 30.dp,
                    ),
                    onClick = {
                        goCustomer(context = context)
                        //mContext.startActivity(Intent(mContext, CustomerActivity::class.java))
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                ) {
                    Text("Customer")
                }
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(painterResource(R.drawable.shops),"content description")
                Button(
                    modifier = Modifier.padding(
                    top = 16.dp,
                    start = 30.dp,
                    end = 30.dp,
                    ),
                    onClick = {
                              goStore(context = context)
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                ) {
                    Text("Store")
                }
            }
        }
    }
}

fun goStore(context: Context) {
    context.startActivity(Intent(context, StoreActivity::class.java))
}

fun goCustomer(context: Context) {
    context.startActivity(Intent(context, CustomerActivity::class.java))
}
//
//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    OrderWithTheme {
//        Main()
//    }
//}
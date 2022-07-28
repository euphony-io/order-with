package com.euphonyio.orderwith

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.euphonyio.orderwith.ui.theme.OrderWithTheme
import com.euphonyio.orderwith.ui.theme.Shapes
import com.euphonyio.orderwith.ui.theme.Typography
import java.util.*


class CustomerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Main UI
            CustomerView()
        }

//        val mRxManager: EuRxManager = EuRxManager()
//        mRxManager.setAcousticSensor {  }
//
//        mRxManager.listen()


        // request Menu String by Euphony
        requestMenu()


    }

    private fun requestMenu() {
        // TODO: Euphony TxManager 사용
    }
}

@Composable
fun CustomerView() {

    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    Column(Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.padding(8.dp))
        // show menu list
        MenuList(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.padding(5.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
//                .weight(1f),
            horizontalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.padding(6.dp))
            // Cancel Button
            Button(
                modifier = Modifier.background(color = Color.White),
                onClick = { onBackPressedDispatcher?.onBackPressed() },
                shape = RoundedCornerShape(3.dp),
            ) {
                Text(text = "Cancel",
                    style = Typography.body1
                )
            }
            Spacer(modifier = Modifier.padding(8.dp))

            // Order Button
            CheckOrderButton()
            Spacer(modifier = Modifier.padding(6.dp))
        }
        Spacer(modifier = Modifier.padding(8.dp))
    }
}


@Composable
fun MenuList(modifier: Modifier) {
    val scrollState = rememberLazyListState()
    LazyColumn(
        modifier = modifier,
        state = scrollState,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // TODO: mockData
        items(3) {
            // List item
            MenuListItem()
        }

        // TODO: 컨텐츠 넣기

    }
}

@Composable
fun MenuListItem() {

    var count by remember { mutableStateOf(0) }

    Surface {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.padding(5.dp))
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2f)
            ) {
                // TODO: name
                Text(
                    text = "FoodName",
                    style = Typography.h5
                )
                Spacer(modifier = Modifier.padding(3.dp))
                // TODO: description 설정
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.LightGray,
                            shape = Shapes.small
                        )
                ) {
                    Text(
                        text = "description\n\n",
                        modifier = Modifier.padding(start = 2.dp),
                        style = Typography.body1,
                        color = Color.LightGray,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.padding(3.dp))
            }
            Spacer(modifier = Modifier.padding(3.dp))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.padding(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.padding(5.dp))
                    count = MenuCountingButton(count)
                    // decrease count button
//                    Icon(
//                        modifier = Modifier
//                            .clickable { }
//                            .background(
//                                color = Color.LightGray,
//                                shape = RoundedCornerShape(
//                                    topStart = 8.dp,
//                                    bottomStart = 8.dp
//                                )
//                            )
//                            .padding(horizontal = 6.dp, vertical = 8.dp),
//                        painter = painterResource(id = R.drawable.ic_minus_count),
//                        contentDescription = null
//                    )
//                    Box(contentAlignment = Alignment.Center) {
//                        Text(
//                            text = count.toString(),
//                            modifier = Modifier
//                                .padding(0.dp)
//                                .width(30.dp)
//                                .height(30.dp),
//                            fontSize = Typography.h6.fontSize,
//                            textAlign = TextAlign.Center,
//                        )
//                    }
//                    // increase count button
//                    ClickableText(
//                        text = AnnotatedString("+"),
//                        modifier = Modifier
//                            .background(
//                                color = Color.LightGray,
//                                shape = RoundedCornerShape(
//                                    topEnd = 8.dp,
//                                    bottomEnd = 8.dp
//                                )
//                            )
//                            .padding(horizontal = 9.dp, vertical = 5.dp),
//                        style = Typography.body1,
//                        onClick = {}
//                    )
                }
                Spacer(modifier = Modifier.padding(5.dp))
                // TODO: Cost 설정
//                val cost =
                val totalCost: String =
                    String.format("%s %s", Currency.getInstance(Locale.KOREA).symbol, "2000")
                Text(
                    text = totalCost,
                    modifier = Modifier.fillMaxWidth(),
                    style = Typography.h6,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.padding(3.dp))
            }
            Spacer(modifier = Modifier.padding(5.dp))
        }
    }
}

@SuppressLint("ComposableNaming")
@Composable
fun MenuCountingButton(count: Int): Int {
    // decrease count button
    Icon(
        modifier = Modifier
            .clickable { }
            .background(
                color = Color.LightGray,
                shape = RoundedCornerShape(
                    topStart = 8.dp,
                    bottomStart = 8.dp
                )
            )
            .padding(horizontal = 6.dp, vertical = 8.dp),
        painter = painterResource(id = R.drawable.ic_minus_count),
        contentDescription = null
    )
    // current count text
    Box(contentAlignment = Alignment.Center) {
        Text(
            text = count.toString(),
            modifier = Modifier
                .padding(0.dp)
                .width(30.dp)
                .height(30.dp),
            fontSize = Typography.h6.fontSize,
            textAlign = TextAlign.Center,
        )
    }
    // increase count button
    ClickableText(
        text = AnnotatedString("+"),
        modifier = Modifier
            .background(
                color = Color.LightGray,
                shape = RoundedCornerShape(
                    topEnd = 8.dp,
                    bottomEnd = 8.dp
                )
            )
            .padding(horizontal = 9.dp, vertical = 5.dp),
        style = Typography.body1,
        onClick = {}
    )

    return count
}

@Composable
fun CheckOrderButton() {
    val showDialog = remember { mutableStateOf(false) }
    if (showDialog.value) {
        CheckOrderDialog(
            showDialog = showDialog.value,
            onDismiss = { showDialog.value = false }
        )
    }
    Surface {
        Button(
            modifier = Modifier.background(color = Color.White),
            onClick = { showDialog.value = true },
            shape = RoundedCornerShape(3.dp),
        ) {
            Text(text = "Send Order",
                style = Typography.body1
            )
        }
    }

}

@Composable
fun CheckOrderDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
) {
    if (showDialog) {

        val showCost = Currency.getInstance(Locale.KOREA).symbol + ""

        AlertDialog(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            backgroundColor = Color.White,
            onDismissRequest = onDismiss,
            text = {
                Text(text = "total Price: $showCost" +
                        "Complete the order to press \"OK\"")
            },
            buttons = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // close the dialog button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(color = Color.White),
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            style = Typography.body1
                        )
                    }

                    // send the order button
                    Button(
                        onClick = { /*TODO*/ },
                        modifier = Modifier
                            .padding(8.dp)
                            .background(color = Color.White),
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Text(
                            text = "OK",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
//    CustomerView()
    MenuList(modifier = Modifier)
}
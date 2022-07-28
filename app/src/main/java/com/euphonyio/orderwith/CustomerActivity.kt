package com.euphonyio.orderwith

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.euphony.rx.AcousticSensor
import co.euphony.rx.EuRxManager
import co.euphony.tx.EuTxManager
import com.euphonyio.orderwith.model.MenuItem
import com.euphonyio.orderwith.ui.theme.Shapes
import com.euphonyio.orderwith.ui.theme.Typography
import com.euphonyio.orderwith.viewModel.CustomerViewModel
import java.util.*
import kotlin.collections.ArrayList


class CustomerActivity : ComponentActivity() {

//    private val _isListening = MutableLiveData(false)
//    val isListening get() = _isListening
//
//    private val _listenResult = MutableLiveData("")
//    val listenResult get() = _listenResult
//
    private val txManager: EuTxManager by lazy {
        EuTxManager(applicationContext)
    }
//    private val rxManager: EuRxManager by lazy {
//        EuRxManager()
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Main UI
            txManager.callEuPI(18500.0, EuTxManager.EuPIDuration.LENGTH_LONG)

            CustomerView()
        }
    }
}

@Composable
fun CustomerView(
    viewModel: CustomerViewModel = CustomerViewModel()
) {
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

//    val isListening by viewModel.isListening

    Column(Modifier
        .padding(8.dp)
        .fillMaxSize()) {
        // show menu list
        MenuList(
            menuList = viewModel.menuListResponse,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f))

        Spacer(modifier = Modifier.padding(5.dp))

        BottomBar(onBackPressedDispatcher)
    }
}

@Composable
fun MenuList(
    menuList: List<MenuItem>,
    modifier: Modifier = Modifier
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        // TODO: mockData
        itemsIndexed(items = MenuItem.getMockMenuItem()) { index, item ->
            MenuItemView(menuItem = item)
        }

        // TODO: 컨텐츠 넣기

    }
}

@Composable
fun BottomBar(onBackPressedDispatcher: OnBackPressedDispatcher?) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        // Cancel Button
        Button(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .background(color = Color.White),
            onClick = { onBackPressedDispatcher?.onBackPressed() },
            shape = RoundedCornerShape(3.dp),
        ) {
            Text(text = "Cancel",
                style = Typography.body1
            )
        }
        // Order Button
        CheckOrderButton(modifier = Modifier.padding(6.dp))
    }
}

@Composable
fun MenuItemView(
    menuItem: MenuItem,
    modifier: Modifier = Modifier
) {
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
                    text = menuItem.name ?: "FoodName",
                    style = Typography.h5
                )
                Spacer(modifier = Modifier.padding(3.dp))
                // TODO: description 설정
                Surface(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = menuItem.description ?: "description\n\n",
                        modifier = Modifier.padding(start = 2.dp),
                        style = Typography.body1,
                        color = Color.Gray,
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
                    menuItem.count = MenuCountingButton(menuItem.count)
                }
                Spacer(modifier = Modifier.padding(5.dp))
                // TODO: Cost 설정
                val cost = menuItem.cost
                val totalCost: String =
                    String.format("%s %d", Currency.getInstance(Locale.KOREA).symbol, cost * menuItem.count)
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

    var itemCount by remember { mutableStateOf(count) }

    // decrease count button
    Icon(
        modifier = Modifier
            .clickable { itemCount-- }
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
        onClick = { itemCount++ }
    )

    return itemCount
}

@Composable
fun CheckOrderButton(modifier: Modifier = Modifier) {
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
            onClick = {
                showDialog.value = true

            },
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
//    MenuList(modifier = Modifier)
}
package com.euphonyio.orderwith

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.lifecycle.Observer
import co.euphony.rx.AcousticSensor
import co.euphony.rx.EuRxManager
import co.euphony.tx.EuTxManager
import com.euphonyio.orderwith.model.OrderItem
import com.euphonyio.orderwith.ui.theme.Orange
import com.euphonyio.orderwith.ui.theme.Typography
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext


class CustomerActivity : ComponentActivity(), CoroutineScope {
    val TAG: String = "로그"

    companion object {
        private const val MENU_REQUEST = "requestMenu"
    }

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val _isSpeaking = MutableLiveData(false)
    val isSpeaking get() = _isSpeaking

    private val _isListening = MutableLiveData(false)
    val isListening get() = _isListening

    private val _listenResult = MutableLiveData("")
    val listenResult get() = _listenResult

    var menuListResponse: List<OrderItem> by mutableStateOf(listOf())

    private val txManager: EuTxManager by lazy {
        EuTxManager(applicationContext)
    }

    private val rxManager: EuRxManager by lazy {
        EuRxManager()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Main UI
            CustomerView()
        }
    }

    override fun onResume() {
        super.onResume()
        setContent {
            CustomerView()
        }
    }

    override fun onPause() {
        super.onPause()
        stopEuphony()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopEuphony()
    }

    private fun requestMenu() {
        if (isSpeaking.value == false) {
            _listenResult.postValue("")
            _isSpeaking.postValue(true)

            txManager.setCode(MENU_REQUEST)
            txManager.play(-1)
        } else {
            _isSpeaking.postValue(false)
            txManager.stop()
        }
    }

    private fun listen() {
        // TODO: RxManager listen
        if (isListening.value == true) {
            _isListening.postValue(false)
            rxManager.finish()
        } else {
            launch(coroutineContext) {
                try {
                    // work in I/O thread
                    withContext(Dispatchers.IO) {
                        rxManager.listen()
                        _isListening.postValue(true)
                        rxManager.acousticSensor = AcousticSensor {
                            _listenResult.postValue(it)
                            //                    getMenuList()
                            _isListening.postValue(false)
                            _isSpeaking.postValue(false)
                            Log.d(TAG, "CustomerViewModel - listen() called :: 수신완료")
                            txManager.stop()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getMenuList() {
        try {
            val menuList = parseMenuItem(listenResult.value.toString())
            menuListResponse = menuList
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 데이터 파싱해서 리스트로 저장
    // create menu item list
    private fun parseMenuItem(receivedData: String): ArrayList<OrderItem> {
        val orderItemList = arrayListOf<OrderItem>()

        val receivedList = receivedData.split("&")
        for (item in receivedList) {
            // if receivedData is not a menu
            if (receivedList.first().equals("#").not()) break

            // if item is not the first item
            else if (item.equals("#").not()) {
                val tmp = item.split("_")
                orderItemList.add(
                    OrderItem(
                        id = tmp[0].toInt(),
                        name = tmp[1],
                        description = tmp[2],
                        cost = tmp[3].toInt()
                    )
                )
            }
        }
        return orderItemList
    }

    private fun transmitOrder() {
        if (isSpeaking.value == false) {
            isSpeaking.postValue(true)

            val order = makeDataToString()
            txManager.setCode(order)
            txManager.play(-1)
        } else {
            isSpeaking.postValue(false)
            txManager.stop()
        }
    }

    private fun makeDataToString(): String {
        var stringData = "#&"
        for (item in menuListResponse) {
            if (item.count != 0) {
                stringData += item.id.toString() + "_"
                stringData += item.name + "_"
                stringData += item.count.toString() + "_"
                stringData += "&"
            }
        }
        return stringData
    }

    private fun stopEuphony() {
        txManager.stop()
        rxManager.finish()
    }


    @Composable
    fun CustomerView() {

        val onBackPressedDispatcher =
            LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

        val observer = Observer<Boolean> {}
        isListening.observeForever(observer)

        requestMenu()

        listen()

        Scaffold(
            modifier = Modifier.padding(10.dp),
            bottomBar = {
                BottomBar(onBackPressedDispatcher = onBackPressedDispatcher)
            }
        ) {
            // show menu list
            MenuList(modifier = Modifier.fillMaxSize())
        }
    }

    @Composable
    fun BottomBar(
        onBackPressedDispatcher: OnBackPressedDispatcher?,
        modifier: Modifier = Modifier,
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            // Cancel Button
            Button(
                modifier = Modifier
                    .padding(horizontal = 12.dp),
                onClick = { onBackPressedDispatcher?.onBackPressed() },
                shape = RoundedCornerShape(3.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
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
    fun MenuList(modifier: Modifier = Modifier) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {

            // TODO: dummy Data 삭제
//            itemsIndexed(items = OrderItem.getMockMenuItem()) { index, item ->
//                MenuItemView(index = index, orderItem = item)
//            }

            // TODO: 실제 사용코드
            itemsIndexed(items = menuListResponse) { index, item ->
                MenuItemView(index = index, orderItem = item)
            }
        }
    }

    @Composable
    fun MenuItemView(
        index: Int,
        orderItem: OrderItem,
        modifier: Modifier = Modifier,
    ) {

        var count by remember { mutableStateOf(orderItem.count) }

        Surface {
            Row(
                modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 13.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(2f)
                ) {
                    // name
                    Text(
                        text = orderItem.name ?: "FoodName",
                        style = Typography.h5
                    )
                    // description
                    Surface(
                        modifier = Modifier
                            .padding(vertical = 3.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = orderItem.description ?: "description\n\n",
                            modifier = Modifier.padding(start = 2.dp),
                            style = Typography.body1,
                            color = Color.Gray,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .padding(start = 3.dp, end = 5.dp, top = 10.dp, bottom = 5.dp)
                        .fillMaxSize()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    count = MenuCountingButton(count)
                    orderItem.count = count
                    // itemCost
                    val cost = orderItem.cost
                    val itemTotalCost: String =
                        String.format("%s %d",
                            Currency.getInstance(Locale.KOREA).symbol,
                            cost * count)
                    Text(
                        text = itemTotalCost,
                        modifier = Modifier
                            .padding(top = 10.dp, bottom = 3.dp)
                            .fillMaxWidth(),
                        style = Typography.h6,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }

    @SuppressLint("ComposableNaming")
    @Composable
    fun MenuCountingButton(count: Int): Int {

        var itemCount by remember { mutableStateOf(count) }

        Row() {
            // decrease count button
            Icon(
                modifier = Modifier
                    .clickable { if (0 < count) itemCount-- }
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
                onClick = { if (count < 10) itemCount++ }
            )
        }

        return itemCount
    }

    @Composable
    fun CheckOrderButton(modifier: Modifier = Modifier) {
        val openDialog = remember { mutableStateOf(false) }
        if (openDialog.value) {
            CheckOrderDialog(
                openDialog = openDialog.value,
                onDismiss = { openDialog.value = false }
            )
        }
        Surface {
            Button(
                onClick = {
                    openDialog.value = true
                },
                shape = RoundedCornerShape(3.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Orange)
            ) {
                Text(text = "Send Order",
                    style = Typography.body1
                )
            }
        }
    }

    @Composable
    fun CheckOrderDialog(
        openDialog: Boolean,
        onDismiss: () -> Unit,
    ) {
        if (openDialog) {

            var totalCost = 0
            menuListResponse.forEach {
                totalCost += it.cost * it.count
            }

            val showCost = Currency.getInstance(Locale.KOREA).symbol + " " + totalCost

            AlertDialog(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                backgroundColor = Color.White,
                onDismissRequest = onDismiss,
                text = {
                    Text(text = "total Price: ${showCost}\n" +
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
                                .padding(8.dp),
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                        ) {
                            Text(
                                text = "Cancel",
                                style = Typography.body1
                            )
                        }

                        // send the order button
                        Button(
                            onClick = {
                                launch(coroutineContext) {
                                    transmitOrder()
                                    Toast.makeText(applicationContext,
                                        "Order Success",
                                        Toast.LENGTH_LONG).show()
                                }
                                onDismiss
                            },

                            modifier = Modifier.padding(8.dp),
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Orange)
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
}

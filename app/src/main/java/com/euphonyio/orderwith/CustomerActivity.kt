package com.euphonyio.orderwith

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.util.Log
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
import co.euphony.util.EuOption
import com.euphonyio.orderwith.model.OrderItem
import com.euphonyio.orderwith.ui.theme.Typography
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext


class CustomerActivity : ComponentActivity(), CoroutineScope {
    val TAG: String = "로그"

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

//    val RECORDER_SAMPLERATE = 8000
//    val RECORDER_CHANNELS = AudioFormat.CHANNEL_CONFIGURATION_STEREO
//    val RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
//
//    val bufferSizeInBytes = AudioTrack.getMinBufferSize(
//        RECORDER_SAMPLERATE,
//        RECORDER_CHANNELS,
//        RECORDER_AUDIO_ENCODING
//    )
//
//    val audioTrack = AudioTrack(
//        AudioManager.STREAM_MUSIC,
//        RECORDER_SAMPLERATE,
//        RECORDER_CHANNELS,
//        RECORDER_AUDIO_ENCODING,
//        bufferSizeInBytes
//    )

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
        txManager.setMode(EuOption.ModeType.EUPI)
        // transmit frequency
        if (isSpeaking.value == false) {
            _listenResult.postValue("")
            _isSpeaking.postValue(true)

            txManager.callEuPI(18500.0, EuTxManager.EuPIDuration.LENGTH_FOREVER)
//            txManager.setCode("Hello")
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
//            rxManager.listen()
//            _isListening.postValue(true)
//            rxManager.acousticSensor = AcousticSensor {
//                _listenResult.postValue(it)
//                Log.d(TAG, "CustomerViewModel - listen() called :: 수신완료")
//                _isListening.postValue(false)
//            }
//            txManager.stop()
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
        receivedData.split("&").forEach {
            val tmp = it.split("_")
            orderItemList.add(
                OrderItem(
                    id = tmp[0].toInt(),
                    name = tmp[1],
                    description = tmp[2],
                    cost = tmp[3].toInt()
                )
            )
        }
        return orderItemList
    }

    private fun transmitOrder() {
        if (isSpeaking.value == false) {
            isSpeaking.postValue(true)

            val order = makeDataToString()
            txManager.setMode(EuOption.ModeType.DEFAULT)
            txManager.setCode(order)
            txManager.play(-1)
        } else {
            isSpeaking.postValue(false)
            txManager.stop()
        }
    }

    private fun makeDataToString(): String {
        var stringData = ""
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
    fun MenuList(modifier: Modifier = Modifier) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {

            // TODO: dummy Data 삭제
            itemsIndexed(items = OrderItem.getMockMenuItem()) { index, item ->
                MenuItemView(index = index, orderItem = item)
            }

            // TODO: 실제 사용코드
//            itemsIndexed(items = menuListResponse) { index, item ->
//                MenuItemView(index = index, orderItem = item)
//            }
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
                    .padding(10.dp)
                    .fillMaxWidth()
            ) {
//                Spacer(modifier = Modifier.padding(5.dp))
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
                    Spacer(modifier = Modifier.padding(3.dp))
                    // description
                    Surface(
                        modifier = Modifier.fillMaxWidth()
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
                        // TODO: count값을 LiveData로 만들어서 해야할듯?
//                        menuListResponse[index].count = count
                        orderItem.count = count
                    }
                    Spacer(modifier = Modifier.padding(5.dp))
                    // itemCost
                    val cost = orderItem.cost
                    val itemTotalCost: String =
                        String.format("%s %d",
                            Currency.getInstance(Locale.KOREA).symbol,
                            cost * count)
                    Text(
                        text = itemTotalCost,
                        modifier = Modifier.fillMaxWidth(),
                        style = Typography.h6,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.padding(3.dp))
                }
//                Spacer(modifier = Modifier.padding(5.dp))
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
                            onClick = {
                                transmitOrder()

                            },
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
}

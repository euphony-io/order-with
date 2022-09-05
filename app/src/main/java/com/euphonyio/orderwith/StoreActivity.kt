package com.euphonyio.orderwith

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.MutableLiveData
import co.euphony.rx.AcousticSensor
import co.euphony.rx.EuPICallDetector
import co.euphony.rx.EuRxManager
import co.euphony.tx.EuTxManager
import co.euphony.util.EuOption
import com.euphonyio.orderwith.data.DBUtil
import com.euphonyio.orderwith.data.dto.Menu
import com.euphonyio.orderwith.data.dto.Order
import com.euphonyio.orderwith.data.dto.OrderMenuItem
import com.euphonyio.orderwith.ui.theme.Orange
import com.euphonyio.orderwith.ui.theme.OrderWithTheme
import kotlinx.coroutines.*
import java.sql.Types.NULL

class StoreActivity : ComponentActivity() {
    companion object {
        // 메뉴는 EuPI로 대체하기 때문에 비활성화
        private const val MENU_REQUEST = "requestMenu"
        private const val ORDER_REQUEST = "#&"
    }

    private val TAG = "[StoreActivity]"
    private lateinit var dbUtil: DBUtil
    private lateinit var mTxManager: EuTxManager
    private lateinit var mRxManager: EuRxManager
    private lateinit var mEuPIRxManager: EuRxManager

    private var flag = MutableLiveData("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbUtil = DBUtil(this)
        mTxManager = EuTxManager(this)
        mRxManager = EuRxManager()

        mEuPIRxManager = EuRxManager(EuOption.ModeType.EUPI)

        var allMenu: List<Menu>
        runBlocking {
            allMenu = dbUtil.getAllMenu()
        }
        Log.e(TAG, "Finish menu check")

        setContent {
            InitView(dbUtil)
        }

        if (!allMenu.isNullOrEmpty()) {
            mRxManager.listen()

            var orderContent = ""
            mRxManager.acousticSensor = AcousticSensor { letters ->
                if (letters == MENU_REQUEST) {
                    flag.value = MENU_REQUEST
                } else {
                    flag.value = letters.substring(0..1)
                    orderContent = letters.substring(2)
                }
            }

            flag.observe(this) { flag ->
                var speakOn = false

                when (flag) {
                    // setCode()방식.
                    // EuPI 방식으로 대체
//                    MENU_REQUEST -> {
//                        Log.i(TAG, "Receive Menu Request.")
//                        if (speakOn) {
//                            mTxManager.stop()
//                        }
//                        mRxManager.finish()
//                        Toast.makeText(
//                            this@StoreActivity,
//                            "Menu requested",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        /* TODO: send menu content
//                        speakOn = true */
//                    }
                    ORDER_REQUEST -> {
                        Log.i(TAG, "Receive Order Request.")

                        mRxManager.finish()
                        Toast.makeText(
                            this@StoreActivity,
                            "Order requested",
                            Toast.LENGTH_SHORT
                        ).show()
                        //TODO: save order data
                        // receiveOrder(orderContent, dbUtil)

                    }
                    else -> {
                        Log.i(TAG, "Receive Wrong Data.")
                        //nothing received
                    }
                }
            }
        } else {
            showErrorToast("Store has no menu. Add menu and Try again")
        }

        // EuPI 방식. RequestCodeEnum.MENU_REQUEST 주파수를 들었을때 작업 수행
//        /**
//         * 해당 주파수의 음파가 인식될 때 계속 호출되는 API
//         * */
//        mEuPIRxManager.setOnWaveKeyPressed(RequestCodeEnum.MENU_REQUEST.code.toInt()) {
//            Toast.makeText(this, "menu request detected", Toast.LENGTH_SHORT).show()
//        }
//        /**
//         * 해당 주파수의 음파가 발생하고 꺼졌을 때 최초 1회 인식하는 API
//         * */
//        mEuPIRxManager.setOnWaveKeyUp(RequestCodeEnum.MENU_REQUEST.code.toInt()) {
//            Toast.makeText(this, "menu request detected", Toast.LENGTH_SHORT).show()
//        }
        /**
         * 해당 주파수의 음파가 발생했을 때 최초 1회 인식하는 API
         * */
        mEuPIRxManager.setOnWaveKeyDown(RequestCodeEnum.MENU_REQUEST.code.toInt()) {
            Toast.makeText(this, "menu request detected", Toast.LENGTH_SHORT).show()
        }

        if (mEuPIRxManager.listen()) {
            Log.d(TAG, "StoreActivity - mEuPIRxManager listen return : true")
        } else {
            Log.d(TAG, "StoreActivity - mEuPIRxManager listen return : false")
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

    private fun stopEuphony() {
        mTxManager.stop()
        mRxManager.finish()
        mEuPIRxManager.finish()
    }

    private fun receiveOrder(letters: String, dbUtil: DBUtil) {

        val orders = (letters.split("&"))
        CoroutineScope(Dispatchers.IO).launch {
            for (order in orders) {
                val splitOrder = order.split("_")
                if (splitOrder.size != 3) {
                    showErrorToast("Order size is ${splitOrder.size}")
                    continue
                }

                val orderName = splitOrder[1]
                val menuId = splitOrder[0]
                val count = splitOrder[2]
                val orderId = dbUtil.addOrder(orderName)

                if (orderId == null) {
                    showErrorToast("OrderId is NULL")
                    continue
                }

                try {
                    val orderMenuId =
                        dbUtil.addOrderMenu(orderId, menuId.toInt(), count.toInt())
                    if (orderMenuId == null) {
                        dbUtil.deleteOrder(orderId)
                        showErrorToast("OrderMenuId is NULL")
                    } else {
                        Toast.makeText(
                            this@StoreActivity,
                            this@StoreActivity.resources.getString(R.string.store_addorder_success),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    showErrorToast("addOrderMenu :: ${e.message}")
                }
            }
        }
    }

//    private fun sendMenu(allMenu: List<Menu>, mTxManager: EuTxManager) {
//        var menuData = ""
//        for (menu in allMenu) {
//            val menuElement =
//                menu.id.toString() + "_" + menu.name + "_" + menu.description + "_" + menu.cost.toString() + "&"
//            menuData += menuElement
//        }
//
//        mTxManager.code = menuData
//        mTxManager.play(-1)
//        Log.e("Tttt", "send start")
//    }

    fun showErrorToast(logMsg: String) {
        Log.i(TAG, logMsg)
        Toast.makeText(
            this@StoreActivity,
            this@StoreActivity.resources.getString(R.string.common_error_message),
            Toast.LENGTH_SHORT
        ).show()
    }

}


@Composable
fun InitView(dbUtil: DBUtil) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar()
        OrderList(dbUtil)
    }
}

@Composable
fun TopBar() {
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(modifier = Modifier.size(50.dp), onClick = { goMain(context = context) }) {
            Icon(
                Icons.Outlined.ArrowBack,
                "back to main"
            )
        }
        Text(text = stringResource(id = R.string.title_orderlist), fontSize = 30.sp)
        IconButton(
            modifier = Modifier.size(50.dp),
            onClick = { openDialog.value = !openDialog.value }) {
            Icon(
                Icons.Outlined.AddCircle,
                "go to add"
            )
            if (openDialog.value) {
                AddMenuDialog(onDismissRequest = {openDialog.value=false})
            }

        }
    }

}

@Composable
fun OrderList(dbUtil: DBUtil) {
    val scrollState = rememberScrollState()
    var allOrder: List<Order>
    val orderMenuList = mutableMapOf<Int, List<OrderMenuItem>>()

    Column(
        modifier = Modifier
            .padding(30.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        runBlocking {
            allOrder = dbUtil.getAllOrder()
            for (order in allOrder) {
                orderMenuList[order.id] = dbUtil.getAllWithMenuByOrderId(order.id)
            }

//        CoroutineScope(Dispatchers.Main).launch{
//
//            CoroutineScope(Dispatchers.Default).async{
//                val allOrder = dbUtil.getAllOrder()
//                for (order in allOrder){
//                    orderMenuList[order.id] = dbUtil.getAllWithMenuByOrderId(order.id)
//                }
//            }.await()
//
        }
        for (order in allOrder) {
            OrderCard(orderName = order.name, orderMenuList = orderMenuList[order.id])
            Spacer(modifier = Modifier.size(15.dp))
        }
    }
}


@Composable
fun OrderCard(orderName: String, orderMenuList: List<OrderMenuItem>?) {
    val isChecked = remember { mutableStateOf(false) }
    val isClicked = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .border(width = 2.dp, color = Color.Black, shape = RectangleShape)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isChecked.value, onCheckedChange = { isChecked.value = it })
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            onClick = { isClicked.value = !isClicked.value },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            if (isClicked.value) {
                if (!orderMenuList.isNullOrEmpty()) {
                    ShowDialog(orderName, orderMenuList, isClicked = isClicked)
                }
            }
            Text(text = orderName, fontSize = 25.sp)
        }
    }
}

@Composable
fun ShowDialog(
    orderName: String,
    orderMenuList: List<OrderMenuItem>?,
    isClicked: MutableState<Boolean>
) {
    AlertDialog(onDismissRequest = { isClicked.value = false },
        title = { Text(text = orderName) },
        text = {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .wrapContentHeight()
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                if (orderMenuList != null) {
                    for (orderMenu in orderMenuList) {
                        val menuName = orderMenu.menuName
                        val count = orderMenu.count.toString()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(text = menuName)
                            Text(text = count)
                        }
                    }
                }
            }
        },
        buttons = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center)
            {
                TextButton(onClick = {
                    isClicked.value = false
                }) {
                    Text(
                        text = stringResource(id = R.string.common_close),
                        color = Orange
                    )
                }
            }
        }
    )
}

private fun goMain(context: Context) {
    context.startActivity(Intent(context, MainActivity::class.java))
}

@Composable
fun AddMenuDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties()
) {
    var nameText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var costText by remember { mutableStateOf("") }
    val util = DBUtil(LocalContext.current)
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismissRequest,
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
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Name") },
                    modifier = Modifier
                        .padding(all = 10.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White
                    )
                )
                TextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .padding(all = 10.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White
                    )

                )
                TextField(
                    value = costText,
                    onValueChange = { costText = it },
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
                        onClick = {onDismissRequest
                                  Log.i("[StoreActivity]", "Cancel Add menu")
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
                                val menuId = util.addMenu(
                                    name = nameText,
                                    description = descriptionText,
                                    cost = costText.toInt()
                                )
                                Log.i("[StoreActivity]", "Menu:: Id: " +menuId.toString()+", name: "+nameText+", cost: "+costText+", description: "+descriptionText)
                                Log.i("[StoreActivity]", "Finish Add menu")
                                onDismissRequest()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Orange)
                    ) {
                        Text("ADD")
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

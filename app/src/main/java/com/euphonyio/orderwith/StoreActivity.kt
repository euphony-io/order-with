package com.euphonyio.orderwith

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Space
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import co.euphony.rx.AcousticSensor
import co.euphony.rx.EuRxManager
import co.euphony.tx.EuTxManager
import com.euphonyio.orderwith.data.DBUtil
import com.euphonyio.orderwith.data.dto.Menu
import com.euphonyio.orderwith.data.dto.Order
import com.euphonyio.orderwith.data.dto.OrderMenuItem
import com.euphonyio.orderwith.ui.theme.OrderWithTheme
import kotlinx.coroutines.*
import kotlin.reflect.typeOf

class StoreActivity : ComponentActivity() {
    private lateinit var dbUtil: DBUtil
    private val mTxManager = EuTxManager(this)
    private val mRxManager = EuRxManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbUtil = DBUtil(this)

        var allMenu: List<Menu>
        runBlocking {
            allMenu = dbUtil.getAllMenu()
        }

        setContent {
            OrderWithTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    InitView(dbUtil)
                }
            } //# Activate Listener when there is menudata & Start listen
            var listenOn = setListener(allMenu)

            while (listenOn) {
                var speakOn = false
                var key = 0

                if (mRxManager.listen(RequestCodeEnum.MENU_REQUEST.code)) {
                    key = RequestCodeEnum.MENU_REQUEST.code
                }

                if (mRxManager.listen(RequestCodeEnum.ORDER_REQUEST.code)) {
                    key = RequestCodeEnum.ORDER_REQUEST.code
                }

                // # when receive 18500hw (request for menu) : send menudata
                if (key == RequestCodeEnum.MENU_REQUEST.code) {
                    if (speakOn) {
                        mTxManager.stop()
                    }
                    mRxManager.finish()
                    listenOn = false
                    sendMenu(allMenu, mTxManager)
                    speakOn = true
                } else if (key == RequestCodeEnum.ORDER_REQUEST.code) {
                    // # when receive 20500hw (request for order) : save order
                    if (speakOn) {
                        mTxManager.stop()
                    }
                    receiveOrder(mRxManager, dbUtil)
                    //TODO: refresh page
                } else {
                    //Nothing to receive
                }
                if (!listenOn) {
                    listenOn = true
                }
            }
        }
    }


    private fun receiveOrder(mRxManager: EuRxManager, dbUtil: DBUtil) {
        fun showErrorToast(logMsg: String) {
            Log.i("[StoreActivity]", logMsg)
            Toast.makeText(
                this@StoreActivity,
                this@StoreActivity.resources.getString(R.string.common_error_message),
                Toast.LENGTH_SHORT
            ).show()
        }

        //menuId_orderName_count&menuId_orderName....
        mRxManager.acousticSensor = AcousticSensor { letters ->
            val newOrder = (letters.split("&"))
            CoroutineScope(Dispatchers.IO).launch {
                for (menus in newOrder) {
                    val saveOrder = menus.split("_")
                    if (saveOrder.size != 3) {
                        showErrorToast("Order size is ${saveOrder.size}")
                        continue
                    }

                    val orderName = saveOrder[1]
                    val menuId = saveOrder[0]
                    val count = saveOrder[2]
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
    }
}

//페이지 접속 시 한 번만 작동
fun setListener(allMenu: List<Menu>): Boolean {
    return allMenu.isNotEmpty()
}

fun sendMenu(allMenu: List<Menu>, mTxManager: EuTxManager) {
    var menuData = ""
    for (menu in allMenu) {
        val menuElement =
            menu.id.toString() + "_" + menu.name + "_" + menu.description + "_" + menu.cost.toString() + "&"
        menuData += menuElement
    }

    mTxManager.code = menuData
    mTxManager.play(-1)
}


fun goMain(context: Context) {
    context.startActivity(Intent(context, MainActivity::class.java))
}

fun goAddMenu(context: Context) {
    context.startActivity(Intent(context, StoreActivity::class.java))
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        //TODO: 버튼아이콘크기수정
        IconButton(modifier = Modifier.size(50.dp), onClick = { goMain(context = context) }) {
            Icon(
                Icons.Outlined.ArrowBack,
                "back to main"
            )
        }
        Text(text = stringResource(id = R.string.title_orderlist), fontSize = 30.sp)
        IconButton(
            modifier = Modifier.size(50.dp),
            onClick = { goAddMenu(context = context) }) {
            Icon(
                Icons.Outlined.AddCircle,
                "go to add"
            )
        }
    }

}

@Composable
fun OrderList(dbUtil: DBUtil) {
    val coroutine = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    var allOrder = mutableListOf<Order>()
    var orderMenuList = mutableMapOf<Int, List<OrderMenuItem>>()

    LazyColumn(
        modifier = Modifier.padding(30.dp),
        state = scrollState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //TODO: coroutine 데이터 불러오기 해결
//        coroutine.launch {
//            allOrder = dbUtil.getAllOrder()
//            for (order in allOrder) {
//                val id = order.id
//                orderMenuList[id] = dbUtil.getAllWithMenuByOrderId(id)
//            }
//        }

        itemsIndexed(allOrder) { index, item ->
            OrderCard(orderName = item.name, orderMenuList = orderMenuList[item.id])

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
        }

        Text(text = orderName, fontSize = 25.sp)
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
                }) { Text(text = stringResource(id = R.string.common_close)) }
            }
        }
    )
}

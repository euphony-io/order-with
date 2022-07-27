package com.euphonyio.orderwith

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.euphony.rx.AcousticSensor
import co.euphony.rx.EuRxManager
import co.euphony.tx.EuTxManager
import com.euphonyio.orderwith.data.DBUtil
import com.euphonyio.orderwith.data.dto.Menu
import com.euphonyio.orderwith.data.dto.OrderMenuItem

class StoreOrderActivity : ComponentActivity() {
    private val dbUtil = DBUtil(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val allMenu = dbUtil.getAllMenu()
        val mTxManager = EuTxManager(this)
        val mRxManager = EuRxManager()

        setContent {
            //  # Activate Listener when there is menudata & Start listen
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
                    receiveOrder(mRxManager)
                } else {
                    //Nothing to receive
                }

                if (!listenOn) {
                    listenOn = true
                }
            }

            InitView(dbUtil = dbUtil)
        }
    }
}

@Composable
fun InitView(dbUtil: DBUtil) {
    val allOrder = dbUtil.getAllOrder()
    Column() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { /*goMain(context = this)*/ }) {
                (Icons.Outlined.ArrowBack)
            }
            Text(text = stringResource(id = R.string.title_orderlist), fontSize = 30.sp)
            Button(onClick = { /*goAddmenu(context = this)*/ }) {
                (Icons.Outlined.AddCircle)
            }
        }
        LazyColumn(
            modifier = Modifier.padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (order in allOrder) {
                val orderMenuList = dbUtil.getAllWithMenuByOrderId(order.id)
                item {
                    OrderCard(orderName = order.name, orderMenuList = orderMenuList)
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
            "item1[id:" + menu.id.toString() + ",name:" + menu.name + ",description:" + menu.description + ",cost:" + menu.cost.toString() + "]&"
        menuData += menuElement
    }

    mTxManager.code = menuData
    mTxManager.play(-1)
}

fun receiveOrder(mRxManager: EuRxManager) {
    mRxManager.acousticSensor = AcousticSensor { }
    // get order data (name, menuid, count)
    // parse data & add Ordermenu & Order in db
}

@Composable
fun OrderCard(orderName: String, orderMenuList: List<OrderMenuItem>) {
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
                ShowDialog(orderName, orderMenuList, isClicked = isClicked)
            }
            Text(text = orderName, fontSize = 25.sp)
        }
    }
    Spacer(modifier = Modifier.padding(8.dp))
}


@Composable
fun ShowDialog(
    orderName: String,
    orderMenuList: List<OrderMenuItem>,
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


fun goMain(context: Context) {
    context.startActivity(Intent(context, MainActivity::class.java))
}

fun goAddMenu(context: Context) {
    context.startActivity(Intent(context, StoreActivity::class.java))
}








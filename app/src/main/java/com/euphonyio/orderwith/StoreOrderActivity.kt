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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.euphony.rx.AcousticSensor
import co.euphony.rx.EuRxManager
import co.euphony.tx.EuTxManager
import com.euphonyio.orderwith.data.DBUtil
import com.euphonyio.orderwith.data.dto.Menu
import com.euphonyio.orderwith.data.dto.OrderMenuItem
import com.euphonyio.orderwith.data.dto.Order
import org.jetbrains.annotations.TestOnly
import java.sql.Types.NULL



class StoreOrderActivity : ComponentActivity() {
    private val dbUtil = DBUtil(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val allMenu=dbUtil.getAllMenu()
            val allOrder = dbUtil.getAllOrder()
            val mTxManager = EuTxManager(this)
            val mRxManager = EuRxManager()


            //  # Activate Listener when there is menudata & Start listen
            var listenOn = setListener(allMenu)

            while(listenOn){

                var speakOn = false
                var key=0

                if (mRxManager.listen(18500)==true){
                    key == 18500
                }
                if (mRxManager.listen(20500)==true){
                    key == 20500
                }

                // # when receive 18500hw (request for menu) : send menudata
                if (key == 18500) {
                    if (!speakOn){
                        mRxManager.finish()
                        listenOn=false
                        sendMenu(allMenu, mTxManager)
                        speakOn=true
                    }
                    else{
                        mTxManager.stop()
                        mRxManager.finish()
                        listenOn=false
                        sendMenu(allMenu, mTxManager)
                        speakOn = true
                    }
                }

                // # when receive 20500hw (request for order) : save order
                else if (key ==20500) {
                    if (!speakOn){
                        receiveOrder(mRxManager)
                    }
                    else{
                        mTxManager.stop()
                        receiveOrder(mRxManager)
                    }
                }
                else{
                    //Nothing to receive
                }

                if (!listenOn){
                    listenOn=true
                }

            }




            Row(modifier = Modifier.padding(20.dp)) {

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
                        Text(text = "Order List", fontSize = 30.sp)
                        Button(onClick = { /*goAddmenu(context = this)*/ }) {
                            (Icons.Outlined.AddCircle)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.padding(30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (order in allOrder) {
                            val ordermenulist = dbUtil.getAllWithMenuByOrderId(order.id)
                            item {
                                Ordercard(ordername = order.name, ordermenulist = ordermenulist)
                            }
                        }

                    }

                }
            }
        }
    }
}


fun setListener(allMenu: List<Menu>): Boolean {
    //페이지 접속 시 한 번만 작동


    var isMenuEmpty = allMenu.isEmpty()

    if (!isMenuEmpty) {
        return true
    }

    return false
}

fun sendMenu( allMenu: List<Menu>, mTxManager: EuTxManager){


        //1. 한 번에 보내기
        var menuData = ""
        for (menu in allMenu) {

            val menuElement =
                "item1[id:" + menu.id.toString() + ",name:" + menu.name + ",description:" + menu.description + ",cost:" + menu.cost.toString() + "]"
            menuData += menuElement
        }

        mTxManager.setCode(menuData)
        mTxManager.play(-1)

    }

fun receiveOrder(mRxManager: EuRxManager){

    mRxManager.acousticSensor= AcousticSensor {  }
    // get order data (name, menuid, count)
    // parse data & add Ordermenu & Order in db
}


//    @Preview(showBackground = true, showSystemUi = true)
//    @Composable
//    fun DefaultPreview() {
//
//        var orderlist = ArrayList<Order>()
//        for (i in 1..10) {
//            orderlist.add(Order(i, "order$i", 200))
//        }
//        val ordermenuitemlist = ArrayList<OrderMenuItem>()
//        for (i in 1..4) {
//            ordermenuitemlist.add(OrderMenuItem(i, 1, "menu$i", i))
//        }
//
//        Column() {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(20.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Button(onClick = { /*goMain(context = this)*/ }) {
//                    (Icons.Outlined.ArrowBack)
//                }
//                Text(text = "Order List", fontSize = 30.sp)
//                Button(onClick = { /*goAddmenu(context = this)*/ }) {
//                    (Icons.Outlined.AddCircle)
//                    }
//                }
//            LazyColumn(
//                modifier = Modifier.padding(10.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                items(orderlist.size) {
//                    Ordercard(orderlist[it].name, ordermenuitemlist)
//                }
//            }
//        }
//    }


    @Composable
    fun Ordercard(ordername: String, ordermenulist: List<OrderMenuItem>) {

        var ischecked = remember { mutableStateOf(false) }
        var isClicked = remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .border(width = 2.dp, color = Color.Black, shape = RectangleShape)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(checked = ischecked.value, onCheckedChange = { ischecked.value = it })
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
                    make_dialog(ordermenulist, isClicked = isClicked)
                }
                Text(text = ordername, fontSize = 25.sp)
            }
        }
        Spacer(modifier = Modifier.padding(8.dp))
    }


    @Composable
    fun make_dialog(ordermenulist: List<OrderMenuItem>, isClicked: MutableState<Boolean>) {


        AlertDialog(onDismissRequest = { isClicked.value = false },
            title = { Text(text = "order content") },
            text = {

                Column(
                    modifier = Modifier
                        //.border(width = 1.dp, color = Color.Black, shape = RectangleShape)
                        .padding(10.dp)
                        .wrapContentHeight()
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly,
                ) {
                    for (ordermenu in ordermenulist) {

                        val menuname = ordermenu.menuName
                        val count = ordermenu.count.toString()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {

                            Text(text = menuname)
                            Text(text = count)

                        }
                    }
                }
            },
            buttons = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center)
                {
                    TextButton(onClick = { isClicked.value = false }) { Text(text = "close") }
                }
            }
        )

    }


    fun goMain(context: Context) {
        context.startActivity(Intent(context, MainActivity::class.java))
    }

    fun goAddmenu(context: Context) {
        context.startActivity(Intent(context, StoreActivity::class.java))
    }








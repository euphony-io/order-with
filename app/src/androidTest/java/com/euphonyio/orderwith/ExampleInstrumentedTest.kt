package com.euphonyio.orderwith

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.euphony.tx.EuTxManager
import com.euphonyio.orderwith.data.DBUtil
import com.euphonyio.orderwith.data.dto.Menu
import com.euphonyio.orderwith.data.dto.Order
import com.euphonyio.orderwith.data.dto.OrderMenu
import com.euphonyio.orderwith.data.dto.OrderMenuItem

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest(context: Context) {
    private lateinit var dbUtil: DBUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbUtil = DBUtil(this)

        var allMenu = listOf<Menu>()
        var allOrder = listOf<Order>()
        var allOrderMenuItem = listOf<OrderMenuItem>()

        runBlocking {
            delay(50000)
            testdata(dbUtil)

            allMenu = dbUtil.getAllMenu()
            allOrder = dbUtil.getAllOrder()
            allOrderMenuItem = dbUtil.getAllWithMenu()
        }

        Log.e("tttt", "get menu  : "+ allMenu.toString())
        Log.e("tttt", "get order  : "+allOrder.toString())
        Log.e("tttt", "get ordermenu  : "+allOrderMenuItem.toString())

        setContent {
            OrderWithTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    InitView(dbUtil)
                    Log.e("tttt", "viewmain")

                }
            }
        }
    }
}


@Composable
fun TestInitView(allOrder: List<Order>, orderMenuList: List<OrderMenuItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar()
        Log.e("tttt", "build top bar")
        TestOrderList(allOrder, orderMenuList)
        Log.e("tttt", "build  show list")

    }
}

@Composable
fun TestOrderList(allOrder: List<Order>, orderMenuList: List<OrderMenuItem>) {
    val scrollState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier.padding(30.dp),
        state = scrollState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (order in allOrder) {
            item {
                OrderCard(orderName = order.name, orderMenuList = orderMenuList)
            }
        }
    }
}

//db에 데이터 세팅용
fun testdata(dbUtil: DBUtil) {
    runBlocking() {
        for (i in 1..10) {
            dbUtil.addOrder("order$")
            for (j in 1..4) {
                dbUtil.addMenu("menu$j", "desc$j", j)
                dbUtil.addOrderMenu(i, j, j * j)
            }

        }
    }
    Log.e("tttt", "setdata")
}





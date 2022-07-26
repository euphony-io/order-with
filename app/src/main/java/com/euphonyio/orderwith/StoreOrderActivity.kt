package com.euphonyio.orderwith

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.euphonyio.orderwith.data.DBUtil
import com.euphonyio.orderwith.data.dto.OrderMenuItem
import com.euphonyio.orderwith.data.dto.Order


class StoreOrderActivity : ComponentActivity() {
    private val dbUtil = DBUtil(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{

            createIconButton(Icons.Outlined.ArrowBack)
            createIconButton(Icons.Outlined.AddCircle)


            var orderlist = dbUtil.getAllOrder()

            for (order in orderlist)
            {
                val ordermenulist = dbUtil.getAllWithMenuByOrderId(order.id)
                LazyColumn(modifier = Modifier.padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally){
                    item{
                        Ordercard(ordername = order.name, ordermenulist = ordermenulist)
                    }
                }

            }

        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {


    var orderlist = ArrayList<Order>()
    for (i in 1..10){
        orderlist.add(Order(i, "order$i", 200))
    }
    val ordermenuitemlist = ArrayList<OrderMenuItem>()
    for (i in 1..10){
        ordermenuitemlist.add(OrderMenuItem(i, i, "menu$i", i*i))
    }

    LazyColumn(modifier = Modifier.padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally){

        items (orderlist.size){
            Ordercard(orderlist[it].name, ordermenuitemlist)
        }
    }
}



@Composable
fun Ordercard(ordername: String, ordermenulist: List<OrderMenuItem>) {

    var ischecked =remember{mutableStateOf(false)}
    var isClicked =remember{mutableStateOf(false)}

    Row(modifier = Modifier
        .border(width = 2.dp, color = Color.Black, shape =RectangleShape)
        .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically){

        Checkbox(checked = ischecked.value, onCheckedChange ={ischecked.value =it })
        Button(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            , onClick ={isClicked.value = !isClicked.value}
            , colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black)
        ){
            if(isClicked.value){
                make_dialog(ordermenulist , isClicked = isClicked)
            }
            Text(text = ordername, fontSize = 25.sp)
        }
    }
    Spacer(modifier = Modifier.padding(8.dp))
}


@Composable
fun make_dialog(ordermenulist: List<OrderMenuItem>, isClicked: MutableState<Boolean>){


    AlertDialog(onDismissRequest ={isClicked.value = false},
        title ={Text(text = "order content")},
        text ={

            Column(modifier = Modifier
                //.border(width = 1.dp, color = Color.Black, shape = RectangleShape)
                .padding(10.dp)
                .wrapContentHeight()
                .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
            ){
                for (ordermenu in ordermenulist) {

                    val menuname =ordermenu.menuName
                    val count = ordermenu.count.toString()

                    Row(modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly){

                        Text(text = menuname)
                        Text(text = count)

                    }
                }
            }
        },
        buttons ={
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center)
            {TextButton(onClick ={isClicked.value=false}){Text(text = "close")}
            }
        }
    )

}

@Composable
fun createIconButton(icon: ImageVector){

    Button(onClick ={/*TODO*/}){
        Icon(icon, "contentdescription")
    }
}







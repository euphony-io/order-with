package com.euphonyio.orderwith

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.euphonyio.orderwith.ui.theme.OrderWithTheme
import com.euphonyio.orderwith.ui.theme.Shapes
import com.euphonyio.orderwith.ui.theme.Typography
import com.skydoves.landscapist.glide.GlideImage

class CustomerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OrderWithTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background) {
                    // Main UI
                    CustomerView()
                }
            }
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

    val context = LocalContext.current
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    Column (Modifier.fillMaxSize()){
        Spacer(modifier = Modifier.padding(8.dp))
        // show menu list
        MenuList(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.padding(5.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
//                .weight(1f),
            horizontalArrangement = Arrangement.Center
        ){
            Spacer(modifier = Modifier.padding(6.dp))
            // Cancel Button
            // TODO: BackPressed 구현
            Button(
                modifier = Modifier.background(color = Color.LightGray),
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

//@Composable
//fun BackPressHandler(
//    backPressedDispatcher: OnBackPressedDispatcher? =
//        LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher,
//    onBackPressed: () -> Unit
//) {
//    val currentOnBackPressed by rememberUpdatedState(newValue = onBackPressed)
//
//    val backCallback = remember {
//        object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                currentOnBackPressed
//            }
//        }
//    }
//
//    DisposableEffect(key1 = backPressedDispatcher) {
//        backPressedDispatcher?.addCallback(backCallback)
//
//        onDispose {
//            backCallback.remove()
//        }
//    }
//}

@Composable
fun MenuList(modifier: Modifier) {
    val scrollState = rememberLazyListState()
    LazyColumn(
        modifier = modifier,
        state = scrollState,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // TODO: mockData
        items(15) {
            // List item
            MenuListItem()
        }

        // TODO: 컨텐츠 넣기

    }
}

@Composable
fun MenuListItem() {
    Surface {
//        ConstraintLayout
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.padding(5.dp))
            Column(modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth()
            ) {
                // TODO: name
                Text(
                    text = "FoodName",
                    textAlign = TextAlign.Center,
                    style = Typography.body1
                )
                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = Shapes.medium,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                ) {
                    GlideImage(
                        // TODO: 받아온 이미지로 설정
                        imageModel = ImageBitmap.imageResource(R.drawable.ic_no_image),
                        placeHolder = ImageBitmap.imageResource(R.drawable.ic_no_image),
                        error = ImageBitmap.imageResource(R.drawable.ic_no_image)
                    )
                }
            }
            Spacer(modifier = Modifier.padding(3.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                // TODO: description 설정
                Surface(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = Color.LightGray,
                            shape = Shapes.small
                        )
                ) {
                    Text(
                        text = "description\n\n\n",
                        style = Typography.body1,
                        color = Color.LightGray,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // TODO: Cost 설정
                    Text(
                        text = "Cost",
                        style = Typography.body1
                    )
                    Spacer(modifier = Modifier.padding(5.dp))
                    // decrease count button
                    Button(
                        modifier = Modifier
                            .wrapContentSize()
                            .background(color = Color.Gray),
                        onClick = { /*TODO*/ }
                    ) {
                        GlideImage(
                            imageModel = ImageBitmap.imageResource(R.drawable.ic_arrow_count_down),
                        )
                    }
                    // increase count button
                    Button(
                        modifier = Modifier
                            .wrapContentSize()
                            .background(color = Color.LightGray),
                        onClick = { /*TODO*/ }
                    ) {
                        GlideImage(
                            imageModel = ImageBitmap.imageResource(R.drawable.ic_arrow_count_up)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.padding(5.dp))
        }
    }
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
            modifier = Modifier.background(color = Color.Blue),
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
        AlertDialog(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            backgroundColor = Color.White,
            onDismissRequest = onDismiss,
            text = {
                Text(text = "total Price: \u00A4\n" +
                    "Complete the order to press \"OK\"") },
            buttons = {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ){
                    // close the dialog button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(color = Color.LightGray),
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
                            .background(color = Color.Blue),
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
    OrderWithTheme {
//        CustomerView()
        MenuList(modifier = Modifier)
    }
}
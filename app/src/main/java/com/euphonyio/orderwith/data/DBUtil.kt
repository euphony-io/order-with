package com.euphonyio.orderwith.data

import android.content.Context
import androidx.room.Room
import com.euphonyio.orderwith.data.dto.Menu
import com.euphonyio.orderwith.data.dto.Order
import com.euphonyio.orderwith.data.dto.OrderMenu
import com.euphonyio.orderwith.data.dto.OrderMenuItem
import java.util.*

class DBUtil(context: Context) {

    private var db: AppDatabase

    init {
        db = AppDatabase.getInstance(context)
    }

    //  < Menu >
    // 1-1. Get all menu (id, name, description, cost)
    suspend fun getAllMenu(): List<Menu> {
        return db.menuDao().getAll()
    }

    // 1-2. Get menu by id
    suspend fun getMenuById(id: Int): Menu {
        return db.menuDao().findById(id)
    }

    // 1-3. Get menu by name
    suspend fun getMenuByName(name: String): Menu {
        return db.menuDao().findByName(name)
    }


    // 2. add Menu
    suspend fun addMenu(name: String, description: String, cost: Int) {
        val dao = db.menuDao()
        val id = (dao.getLastId()?: 0) + 1
        val menu = Menu(id, name, description, cost)
        dao.insertMenu(menu)
    }

    // 3-1. delete Menu
    suspend fun deleteMenu(id: Int) : Boolean{
        var isSuccess = false
        var menu: Menu? = db.menuDao().findById(id)
        if (menu != null) {
            db.menuDao().delete(menu)
            isSuccess = true
        }
        return isSuccess
    }

    // 3-2. delete all Menu
    suspend fun deleteAllMenu() {
        db.menuDao().deleteAll()
    }


    // <Order>
    // 1. Get all order (id, name, created_at)
    suspend fun getAllOrder(): List<Order> {
        return db.orderDao().getAll()
    }

    // 1-2. Get order by id
    suspend fun getOrderById(id: Int): Order {
        return db.orderDao().findById(id)
    }

    // 1-3. GEt order by name
    suspend fun getOrderByName(name: String): Order {
        return db.orderDao().findByName(name)
    }


    // 2. add Order
    suspend fun addOrder(name: String) {
        val dao = db.orderDao()
        val id = dao.getLastId() + 1
        val order = Order(id, name, Date().time)
        dao.insertAll(order)
    }

    // 3-1. delete Order
    suspend fun deleteOrder(id: Int): Boolean {
        var isSuccess = false
        var order: Order? = db.orderDao().findById(id)
        if (order != null) {
            db.orderDao().delete(order)
            isSuccess = true
        }
        return isSuccess
    }


    // 3-2. delete All Order
    suspend fun deleteAllOrder() {
        db.orderDao().deleteAll()
    }


    //  < OrderMenu >
    // This Table links Order and Menu
    // 1. Get all OrderMenu
    suspend fun getAllOrderMenu(): List<OrderMenu> {
        return db.orderMenuDao().getAll()
    }


    // 1-2. Get All OrderMenu with Menu info
    suspend fun getAllWithMenu(): List<OrderMenuItem> {
        return db.orderMenuDao().getAllWithMenu()
    }


    // 1-3. Get OrderMenu with Menu info by orderId
    suspend fun getAllWithMenuByOrderId(OrderId: Int): List<OrderMenuItem> {
        return db.orderMenuDao().getAllByOrderId(OrderId)
    }


    //2. add OrderMenu
    suspend fun addOrderMenu(orderId: Int, menuId: Int, count: Int) {
        val dao = db.orderMenuDao()
        val id = dao.getLastId() + 1
        val orderMenu = OrderMenu(id, orderId, menuId, count)
        dao.insertAll(orderMenu)
    }


}
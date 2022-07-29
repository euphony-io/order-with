package com.euphonyio.orderwith.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.euphonyio.orderwith.data.dto.OrderMenuItem
import com.euphonyio.orderwith.data.dto.OrderMenu

/**
 *  메뉴-주문 연결 Dao
 *  @author phj0407
 */
@Dao
interface OrderMenuDao {
    @Query("SELECT * FROM OrderMenu")
    suspend fun getAll(): List<OrderMenu>

    @Query("SELECT om.id as id, om.order_id as orderId, menu.name as menuName, om.count as count from OrderMenu om Left join menu on om.menu_id = menu.id")
    suspend fun getAllWithMenu(): List<OrderMenuItem>

    @Query("SELECT om.id as id, om.order_id as orderId, menu.name as menuName, om.count as count from OrderMenu om Left join menu on om.menu_id = menu.id " +
            "WHERE om.order_id = (:orderId)")
    suspend fun getAllByOrderId(orderId: Int): List<OrderMenuItem>

    @Query("SELECT id FROM OrderMenu order by id desc limit 1")
    suspend fun getLastId() : Int?

    @Insert
    suspend fun insertAll(vararg orderMenus: OrderMenu)

    @Delete
    suspend fun delete(orderMenus: OrderMenu)
}

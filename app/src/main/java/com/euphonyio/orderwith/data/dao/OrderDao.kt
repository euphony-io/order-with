package com.euphonyio.orderwith.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.euphonyio.orderwith.data.dto.Order

/**
 *  주문 Dao
 *  @author hyejin
 */
@Dao
interface OrderDao {
    @Query("SELECT * FROM 'Order'")
    fun getAll(): List<Order>

    @Query("SELECT * FROM 'Order' WHERE id IN (:orderIds)")
    fun loadAllByIds(orderIds: IntArray): List<Order>

    @Query("SELECT * FROM 'Order' WHERE id = (:orderId)")
    fun findById(orderId: Int): Order

    @Query("SELECT * FROM 'Order' WHERE name = (:orderName)")
    fun findByName(orderName: String): Order

    @Query("SELECT * FROM 'Order' WHERE created_at = (:created_at) ")
    fun findByCreated_at(created_at: Long): Order

    @Query("SELECT id FROM 'Order' order by id desc limit 1")
    fun getLastId() : Int

    @Insert
    fun insertAll(vararg order: Order)

    @Delete
    fun delete(order: Order)

    @Query("DELETE FROM 'Order'")
    fun deleteAll()
}
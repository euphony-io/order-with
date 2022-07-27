package com.euphonyio.orderwith.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.euphonyio.orderwith.data.dto.Order

/**
 *  주문 Dao
 *  @author phj0407
 */
@Dao
interface OrderDao {
    @Query("SELECT * FROM 'Order'")
    suspend fun getAll(): List<Order>

    @Query("SELECT * FROM 'Order' WHERE id IN (:orderIds)")
    suspend fun loadAllByIds(orderIds: IntArray): List<Order>

    @Query("SELECT * FROM 'Order' WHERE id = (:orderId)")
    suspend fun findById(orderId: Int): Order

    @Query("SELECT * FROM 'Order' WHERE name = (:orderName)")
    suspend fun findByName(orderName: String): Order

    @Query("SELECT * FROM 'Order' WHERE created_at = (:created_at) ")
    suspend fun findByCreated_at(created_at: Long): Order

    @Query("SELECT id FROM 'Order' order by id desc limit 1")
    suspend fun getLastId() : Int

    @Insert
    suspend fun insertAll(vararg order: Order)

    @Delete
    suspend fun delete(order: Order)

    @Query("DELETE FROM 'Order'")
    suspend fun deleteAll()
}
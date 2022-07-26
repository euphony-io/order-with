package com.euphonyio.orderwith.data.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

/**
 *  메뉴-주문 연결 Dto
 *  @see Menu
 *  @see Order
 *  @author phj0407
 */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Order::class,
            parentColumns = ["id"],
            childColumns = ["order_id"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = Menu::class,
            parentColumns = ["id"],
            childColumns = ["menu_id"],
            onDelete = CASCADE
        )
    ]
)

data class OrderMenu (
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "order_id") val order_id: Int,
    @ColumnInfo(name = "menu_id") val menu_id: Int,
    @ColumnInfo(name = "count") val count: Int
)
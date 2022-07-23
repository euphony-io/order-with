package com.euphonyio.orderwith.data.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *  주문 Dto
 *  @author hyejin
 */
@Entity
data class Order(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "created_at") val created_at: Long
)
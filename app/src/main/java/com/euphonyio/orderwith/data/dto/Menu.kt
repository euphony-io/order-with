package com.euphonyio.orderwith.data.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *  메뉴 Dto
 *  @author hyejin
 */
@Entity
data class Menu(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "cost") val cost: Int?
)



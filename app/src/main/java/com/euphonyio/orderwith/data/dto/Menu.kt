package com.euphonyio.orderwith.data.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.net.URL

/**
 *  메뉴 Dto
 *  @author phj0407
 */
@Entity
data class Menu(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "cost") val cost: Int?
)



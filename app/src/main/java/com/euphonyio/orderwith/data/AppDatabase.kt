package com.euphonyio.orderwith.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.euphonyio.orderwith.data.dao.MenuDao
import com.euphonyio.orderwith.data.dao.OrderDao
import com.euphonyio.orderwith.data.dao.OrderMenuDao
import com.euphonyio.orderwith.data.dto.Menu
import com.euphonyio.orderwith.data.dto.OrderMenu

@Database(entities = [ Order::class, Menu::class, OrderMenu::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun menuDao(): MenuDao
    abstract fun orderMenuDao(): OrderMenuDao
}
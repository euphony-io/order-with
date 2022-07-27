package com.euphonyio.orderwith.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.euphonyio.orderwith.data.dao.MenuDao
import com.euphonyio.orderwith.data.dao.OrderDao
import com.euphonyio.orderwith.data.dao.OrderMenuDao
import com.euphonyio.orderwith.data.dto.Menu
import com.euphonyio.orderwith.data.dto.Order
import com.euphonyio.orderwith.data.dto.OrderMenu
import java.lang.Exception

@Database(entities = [ Order::class, Menu::class, OrderMenu::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        private var instance: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                synchronized(AppDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "OrderWith.db"
                    ).build()
                }
            }
            return instance?: throw Exception("Instance Not Created.")
        }
    }

    abstract fun orderDao(): OrderDao
    abstract fun menuDao(): MenuDao
    abstract fun orderMenuDao(): OrderMenuDao
}

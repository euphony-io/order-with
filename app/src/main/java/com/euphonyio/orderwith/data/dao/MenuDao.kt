package com.euphonyio.orderwith.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.euphonyio.orderwith.data.dto.Menu

/**
 *  메뉴 Dao
 *  @author phj0407
 */
@Dao
interface MenuDao {
    @Query("SELECT * FROM Menu")
    suspend fun getAll(): List<Menu>

    @Query("SELECT * FROM Menu WHERE id IN (:menuIds)")
    suspend fun loadAllByIds(menuIds: IntArray): List<Menu>

    @Query("SELECT * FROM Menu WHERE name = (:menuName)")
    suspend fun findByName(menuName: String): Menu

    @Query("SELECT * FROM Menu WHERE id = (:menuId)")
    suspend fun findById(menuId: Int): Menu

    @Query("SELECT id FROM Menu order by id desc limit 1")
    suspend fun getLastId(): Int?

    @Insert
    suspend fun insertAll(vararg menus: Menu) : List<Long>

    @Insert
    suspend fun insertMenu(menu: Menu) : Long

    @Delete
    suspend fun delete(menu: Menu)

    @Query("DELETE FROM Menu")
    suspend fun deleteAll()
}

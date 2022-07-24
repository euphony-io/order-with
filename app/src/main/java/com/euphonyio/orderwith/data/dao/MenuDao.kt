package com.euphonyio.orderwith.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.euphonyio.orderwith.data.dto.Menu

/**
 *  메뉴 Dao
 *  @author hyejin
 */
@Dao
interface MenuDao {
    @Query("SELECT * FROM Menu")
    fun getAll(): List<Menu>

    @Query("SELECT * FROM Menu WHERE id IN (:menuIds)")
    fun loadAllByIds(menuIds: IntArray): List<Menu>

    @Query("SELECT * FROM Menu WHERE name = (:menuName)")
    fun findByName(menuName: String): Menu

    @Query("SELECT * FROM Menu WHERE id = (:menuId)")
    fun findById(menuId: Int): Menu

    @Query("SELECT id FROM Menu order by id desc limit 1")
    fun getLastId(): Int

    @Insert
    fun insertAll(vararg menus: Menu) : List<Long>

    @Insert
    fun insertMenu(menu: Menu) : Long

    @Delete
    fun delete(menus: Menu)

    @Query("DELETE FROM Menu")
    fun deleteAll()
}
package com.euphonyio.orderwith.model

/**
 * OrderWith
 * Created by SeonJK
 * Date: 2022-07-28
 * Time: 오후 2:00
 * */
data class MenuItem(
    val id: Int,
    val name: String,
    val description: String,
    val cost: Int,
    var count: Int = 0
) {
    companion object {
        fun getMockMenuItem() = listOf<MenuItem>(
            MenuItem(
                id = 12,
                name = "Pasta",
                description = "It's a tomato pasta",
                cost = 12000
            ),
            MenuItem(
                id = 8,
                name = "Risotto",
                description = "bacon creme risotto",
                cost = 13500
            )
        )
    }
}

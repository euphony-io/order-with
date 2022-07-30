package com.euphonyio.orderwith.model

/**
 * OrderWith
 * Created by SeonJK
 * Date: 2022-07-28
 * Time: 오후 2:00
 * */
data class OrderItem(
    val id: Int,
    val name: String,
    val description: String,
    val cost: Int,
    var count: Int = 0
) {
    companion object {
        fun getMockMenuItem() = listOf<OrderItem>(
            OrderItem(
                id = 12,
                name = "Pasta",
                description = "It's a tomato pasta",
                cost = 12000
            ),
            OrderItem(
                id = 8,
                name = "Risotto",
                description = "bacon creme risotto",
                cost = 13500
            )
        )
    }
}

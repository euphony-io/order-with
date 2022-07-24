package com.euphonyio.orderwith.data.dto

/**
 *  주문에 해당되는 메뉴 정보
 *  @author phj0407
 */
data class OrderMenuItem(var id : Int, var orderId: Int, var menuName : String, var count : Int)
package com.application.svcafe

data class CartItem(
    val drink: DrinksData,
    var quantity: Int,
    val price: Double
){
constructor() : this(DrinksData(), 0, 0.0)
}
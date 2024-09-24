package com.application.svcafe

data class DrinksData(
    val idDrink: String,
    val strDrink: String,
    val strDrinkThumb: String,
    val strCategory: String,
    var description: String,
    var price: Double
) {

    constructor() : this("", "", "", "", "",0.0)
}

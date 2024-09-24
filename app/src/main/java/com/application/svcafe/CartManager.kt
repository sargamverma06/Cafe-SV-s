package com.application.svcafe

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object CartManager {
    private val cartItems = mutableListOf<CartItem>()
    private val database = FirebaseDatabase.getInstance().getReference("menuItems")

    fun addItem(drink: DrinksData, price: Double) {
        val existingItem = cartItems.find { it.drink.idDrink == drink.idDrink }
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            cartItems.add(CartItem(drink, 1, price))
        }
        updateFirebaseCart()
    }

    fun getCartItems(): List<CartItem> {
        return cartItems
    }

    fun clearCart() {
        cartItems.clear()
        updateFirebaseCart()
    }

    private fun updateFirebaseCart() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val cartRef = FirebaseDatabase.getInstance().getReference("cart").child(userId)
            cartRef.setValue(cartItems)
        }
    }
}

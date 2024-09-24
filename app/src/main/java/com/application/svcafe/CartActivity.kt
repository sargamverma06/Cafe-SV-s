package com.application.svcafe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var totalTextView: TextView
    private lateinit var deliveryChargesTextView: TextView
    private lateinit var taxTextView: TextView
    private lateinit var grandTotalTextView: TextView
    private lateinit var placeOrderButton: Button
    private lateinit var cartAdapter: CartAdapter
    private lateinit var cartItemList: MutableList<CartItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        recyclerView = findViewById(R.id.recyclerViewCart)
        totalTextView = findViewById(R.id.totalTextView)
        deliveryChargesTextView = findViewById(R.id.deliveryChargesTextView)
        taxTextView = findViewById(R.id.taxTextView)
        grandTotalTextView = findViewById(R.id.grandTotalTextView)
        placeOrderButton = findViewById(R.id.placeOrderButton)

        recyclerView.layoutManager = LinearLayoutManager(this)


        cartItemList = mutableListOf()


        fetchCartItems()

        placeOrderButton.setOnClickListener {
            placeOrder()
        }
    }
    // fetch and store data of cart
    private fun fetchCartItems() {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val cartRef = database.getReference("cart").child(userId)

            cartRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    cartItemList.clear()
                    for (snapshot in dataSnapshot.children) {
                        val item = snapshot.getValue(CartItem::class.java)
                        if (item != null) {
                            cartItemList.add(item)
                        }
                    }
                    cartAdapter = CartAdapter(this@CartActivity, cartItemList)
                    recyclerView.adapter = cartAdapter
                    calculateTotal()
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }
    }
    // calculate the total
    private fun calculateTotal() {
        val total = cartItemList.sumOf { it.price * it.quantity }
        val deliveryCharges = 5.00 // Fixed delivery charge
        val tax = total * 0.1 // 10% tax
        val grandTotal = total + deliveryCharges + tax

        totalTextView.text = "Total: $%.2f".format(total)
        deliveryChargesTextView.text = "Delivery Charges: $%.2f".format(deliveryCharges)
        taxTextView.text = "Tax: $%.2f".format(tax)
        grandTotalTextView.text = "Grand Total: $%.2f".format(grandTotal)
    }
    // clear data when order is placed
    private fun placeOrder() {
            Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()

            // Navigate back to MainActivity
            val intent = Intent(this@CartActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
}

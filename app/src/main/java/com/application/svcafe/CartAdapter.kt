package com.application.svcafe

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class CartAdapter(private val context: Activity, private val cartItemList: MutableList<CartItem>) :
    RecyclerView.Adapter<CartAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = cartItemList[position]
        holder.itemName.text = currentItem.drink.strDrink
        holder.itemPrice.text = "Price: $${currentItem.price}"
        holder.itemQuantity.text = currentItem.quantity.toString()

        // Load the image using Picasso
        Picasso.get().load(currentItem.drink.strDrinkThumb).into(holder.itemImage)

        holder.buttonIncrease.setOnClickListener {
            currentItem.quantity++
            holder.itemQuantity.text = currentItem.quantity.toString()
            updateFirebaseCart()
        }

        holder.buttonDecrease.setOnClickListener {
            if (currentItem.quantity > 1) {
                currentItem.quantity--
                holder.itemQuantity.text = currentItem.quantity.toString()
                updateFirebaseCart()
            } else {
                removeItem(position)
            }
        }

        holder.removeItemButton.setOnClickListener {
            removeItem(position)
        }
    }

    override fun getItemCount(): Int {
        return cartItemList.size
    }

    private fun removeItem(position: Int) {
        cartItemList.removeAt(position)
        notifyItemRemoved(position)
        updateFirebaseCart()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        val itemName: TextView = itemView.findViewById(R.id.itemName)
        val itemPrice: TextView = itemView.findViewById(R.id.itemPrice)
        val itemQuantity: TextView = itemView.findViewById(R.id.itemQuantity)
        val buttonIncrease: Button = itemView.findViewById(R.id.buttonIncrease)
        val buttonDecrease: Button = itemView.findViewById(R.id.buttonDecrease)
        val removeItemButton: Button = itemView.findViewById(R.id.removeItemButton)
    }

    private fun updateFirebaseCart() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val cartRef = FirebaseDatabase.getInstance().getReference("cart").child(userId)
            cartRef.setValue(cartItemList)
        }
    }
}

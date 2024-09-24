package com.application.svcafe

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class DrinksAdapter(private val context: Activity, private var drinksList: List<DrinksData>) :
    RecyclerView.Adapter<DrinksAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.each_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = drinksList[position]

        holder.title.text = currentItem.strDrink

        Picasso.get().load(currentItem.strDrinkThumb).into(holder.image)

        fetchDrinkDetails(currentItem) { price, description ->
            holder.description.text = description
            holder.price.text = "$$price"

            holder.addToCartButton.setOnClickListener {
                CartManager.addItem(currentItem, price)
                Toast.makeText(context, "${currentItem.strDrink} added to cart", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return drinksList.size
    }

    private fun fetchDrinkDetails(drink: DrinksData, callback: (Double, String) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("menuItems").child(drink.idDrink)
        database.get().addOnSuccessListener { snapshot ->
            val price = snapshot.child("price").getValue(Double::class.java) ?: 0.0
            val description = snapshot.child("description").getValue(String::class.java) ?: "No description available"
            callback(price, description)
        }.addOnFailureListener {
            callback(0.0, "No description available")
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ShapeableImageView = itemView.findViewById(R.id.productImage)
        var title: TextView = itemView.findViewById(R.id.productName)
        var description: TextView = itemView.findViewById(R.id.productDescription)
        var price: TextView = itemView.findViewById(R.id.productPrice)
        var addToCartButton: Button = itemView.findViewById(R.id.addToCartButton)
    }

    fun updateDrinks(newDrinksList: List<DrinksData>) {
        drinksList = newDrinksList
        notifyDataSetChanged()
    }
}

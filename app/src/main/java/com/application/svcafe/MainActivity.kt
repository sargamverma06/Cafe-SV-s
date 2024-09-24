package com.application.svcafe

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var drinksTextView: TextView
    private lateinit var greetingTextView: TextView
    private lateinit var nameTextView: TextView
    private lateinit var logoutButton: ImageView
    private lateinit var searchBar: EditText
    private lateinit var cartImageView: ImageView

    private lateinit var originalDrinksList: List<DrinksData>
    private lateinit var drinksAdapter: DrinksAdapter

    // Initialize activity and set up views
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        window.statusBarColor = Color.parseColor("#ffffff")

        recyclerView = findViewById(R.id.recyclerView)
        drinksTextView = findViewById(R.id.drinks)
        greetingTextView = findViewById(R.id.greetingTextView)
        nameTextView = findViewById(R.id.nameTextView)
        logoutButton = findViewById(R.id.buttonLogout)
        searchBar = findViewById(R.id.searchBar)
        cartImageView = findViewById(R.id.cartImageView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch drinks menu from API
        fetchDrinksMenu()

        // Set user name in the greeting
        setUserName()

        logoutButton.setOnClickListener {
            logout()
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterDrinks(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        cartImageView.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }
    }
    // Fetch the drinks menu from the API
    private fun fetchDrinksMenu() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.thecocktaildb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiInterface = retrofit.create(ApiInterface::class.java)
        val call = apiInterface.getDrinks()

        call.enqueue(object : Callback<Drinks> {
            override fun onResponse(call: Call<Drinks>, response: Response<Drinks>) {
                if (response.isSuccessful) {
                    originalDrinksList = response.body()?.drinks ?: emptyList()
                    if (originalDrinksList.isNotEmpty()) {
                        drinksTextView.visibility = View.GONE
                        drinksAdapter = DrinksAdapter(this@MainActivity, originalDrinksList)
                        recyclerView.adapter = drinksAdapter
                    } else {
                        drinksTextView.visibility = View.VISIBLE
                    }
                } else {
                    Log.e("MainActivity", "Response was not successful: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Drinks>, t: Throwable) {
                Log.e("MainActivity", "API call failed: ${t.message}")
                drinksTextView.visibility = View.VISIBLE
            }
        })
    }
    // Set the user's name in the greeting text view
    private fun setUserName() {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = database.getReference("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        nameTextView.text = user.fullName
                        greetingTextView.text = "Greetings,"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("MainActivity", "Failed to read user name: ${databaseError.message}")
                }
            })
        }
    }
    // Log out the current user
    private fun logout() {
        mAuth.signOut()
        val intent = Intent(this, IntroActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
    // Filter the drinks list based on the search query
    private fun filterDrinks(query: String) {
        val lowerCaseQuery = query.lowercase()
        val filteredDrinksList = if (query.isEmpty()) {
            originalDrinksList
        } else {
            originalDrinksList.filter {
                it.strDrink.lowercase().contains(lowerCaseQuery)
            }
        }

        drinksAdapter.updateDrinks(filteredDrinksList)

        // Fetch and update price and description for filtered drinks
        for (drink in filteredDrinksList) {
            fetchDrinkDetails(drink)
        }
    }

    private fun fetchDrinkDetails(drink: DrinksData) {
        val database = FirebaseDatabase.getInstance().getReference("menuItems").child(drink.idDrink)
        database.get().addOnSuccessListener { snapshot ->
            val price = snapshot.child("price").getValue(Double::class.java) ?: 0.0
            val description = snapshot.child("description").getValue(String::class.java) ?: "No description available"

            // Update drink object with fetched details
            drink.price = price
            drink.description = description

            // Notify adapter of data change
            drinksAdapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Log.e("MainActivity", "Failed to fetch drink details: ${it.message}")
        }
    }
}
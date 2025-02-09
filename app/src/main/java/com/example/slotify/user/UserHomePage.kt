package com.example.slotify.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.slotify.R
//import com.example.slotify.adapters.BusinessAdapter
//import com.example.slotify.models.Business
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserHomePage : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // UI Components
    private lateinit var BusinessName: TextView
    private lateinit var businessMoto: TextView
    private lateinit var profileLogo: CircleImageView
    private lateinit var upcomingBooking: LinearLayout
    private lateinit var bookingTitle: TextView
    private lateinit var bookingTime: TextView
    private lateinit var bookingLocation: TextView
    private lateinit var bookingImage: CircleImageView
    private lateinit var businessTypeContainer: LinearLayout
    private lateinit var businessRecyclerView: RecyclerView

    private lateinit var homeButton: ImageButton
    private lateinit var profileButton: ImageButton
    private lateinit var addButton: ImageButton
    private lateinit var notificationButton: ImageButton
    private lateinit var favouriteButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home_page)

        // Initialize FirebaseFirestore & FirebaseAuth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Retrieve the email from Intent
        val email = intent.getStringExtra("email") ?: return

        // Initialize UI components
        BusinessName = findViewById(R.id.business_name)
        businessMoto = findViewById(R.id.business_moto)
        profileLogo = findViewById(R.id.profile)
        upcomingBooking = findViewById(R.id.upcoming_booking)
        bookingTitle = findViewById(R.id.tv_booking_title)
        bookingTime = findViewById(R.id.tv_booking_time)
        bookingLocation = findViewById(R.id.tv_booking_location)
        bookingImage = findViewById(R.id.iv_booking_image)
        businessTypeContainer = findViewById(R.id.service_list)
        businessRecyclerView = findViewById(R.id.rv_service_list)

        homeButton = findViewById(R.id.home_button)
        profileButton = findViewById(R.id.profile_button)
        addButton = findViewById(R.id.add_button)
        notificationButton = findViewById(R.id.notification_button)
        favouriteButton = findViewById(R.id.favourite_button)

        // Hide upcoming bookings initially
        upcomingBooking.visibility = View.GONE

        // Load user profile
        loadUserProfile(email)

        // Check for upcoming bookings
        checkUpcomingBookings(email)

        // Load business types
        loadBusinessTypes()

        // Load all businesses in RecyclerView
        //loadBusinesses()

        // Button click listeners
        setupBottomNavigation(email)
    }

    private fun loadUserProfile(email: String) {
        firestore.collection("Users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDoc = documents.first()
                    val profileImageUrl = userDoc.getString("profileImageUrl") ?: ""

                    // Load profile image if available
                    if (profileImageUrl.isNotEmpty()) {
                        Picasso.get().load(profileImageUrl).into(profileLogo)
                    } else {
                        profileLogo.setImageResource(R.drawable.profile_logo)
                    }
                } else {
                    Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUpcomingBookings(email: String) {
        firestore.collection("Bookings")
            .whereEqualTo("email", email)
            .whereEqualTo("status", "upcoming")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val booking = documents.first()
                    bookingTitle.text = booking.getString("serviceName")
                    bookingTime.text = booking.getString("time")
                    bookingLocation.text = booking.getString("location")

                    val imageUrl = booking.getString("imageUrl") ?: ""
                    if (imageUrl.isNotEmpty()) {
                        Picasso.get().load(imageUrl).into(bookingImage)
                    }

                    upcomingBooking.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to fetch bookings: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadBusinessTypes() {
        firestore.collection("businessType")
            .get()
            .addOnSuccessListener { documents ->
                businessTypeContainer.removeAllViews()
                documents.forEach { document ->
                    val businessType = document.getString("typeName") ?: "Unknown"
                    val button = Button(this)
                    button.text = businessType
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(8, 0, 8, 0) // Add spacing between buttons
                    button.layoutParams = params

                    button.setOnClickListener {
                        Toast.makeText(this, "Clicked on $businessType", Toast.LENGTH_SHORT).show()
                    }
                    businessTypeContainer.addView(button)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load business types", Toast.LENGTH_SHORT).show()
            }
    }

//    private fun loadBusinesses() {
//        val businessList = mutableListOf<Business>()
//        val adapter = BusinessAdapter(this, businessList)
//        businessRecyclerView.layoutManager = LinearLayoutManager(this)
//        businessRecyclerView.adapter = adapter
//
//        firestore.collection("businesses")
//            .get()
//            .addOnSuccessListener { documents ->
//                for (document in documents) {
//                    val business = Business(
//                        name = document.getString("name") ?: "Unknown",
//                        location = document.getString("location") ?: "Unknown",
//                        imageUrl = document.getString("imageUrl") ?: ""
//                    )
//                    businessList.add(business)
//                }
//                adapter.notifyDataSetChanged()
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Failed to load businesses", Toast.LENGTH_SHORT).show()
//            }
//    }

    private fun setupBottomNavigation(email: String) {
        homeButton.setOnClickListener {
            startActivity(Intent(this, UserHomePage::class.java).putExtra("email", email))
        }
        profileButton.setOnClickListener {
            startActivity(Intent(this, UserProfile::class.java).putExtra("email", email))
        }
        addButton.setOnClickListener {
            startActivity(Intent(this, UserBooking::class.java).putExtra("email", email))
        }
        notificationButton.setOnClickListener {
            startActivity(Intent(this, UserNotification::class.java).putExtra("email", email))
        }
        favouriteButton.setOnClickListener {
            startActivity(Intent(this, UserHomePage::class.java).putExtra("email", email))
        }
    }
}

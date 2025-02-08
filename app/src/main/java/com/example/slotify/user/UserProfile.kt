package com.example.slotify.user

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.slotify.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserProfile : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // UI Components
    private lateinit var BusinessName: TextView
    private lateinit var businessMoto: TextView
    private lateinit var profileLogo: CircleImageView

    private lateinit var profileImage: CircleImageView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var userPhone: TextView
    private lateinit var userAddress: TextView

    private lateinit var editProfile: ImageView
    private lateinit var signOut: ImageView

    private lateinit var homeButton: ImageButton
    private lateinit var profileButton: ImageButton
    private lateinit var addButton: ImageButton
    private lateinit var notificationButton: ImageButton
    private lateinit var favouriteButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // Initialize FirebaseFirestore & FirebaseAuth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Retrieve the email from Intent
        val email = intent.getStringExtra("email") ?: return

        // Initialize ImageViews
        BusinessName = findViewById(R.id.business_name)
        businessMoto = findViewById(R.id.business_moto)
        profileLogo = findViewById(R.id.profile)
        profileImage = findViewById(R.id.iv_image)
        userName = findViewById(R.id.tv_name)
        userEmail = findViewById(R.id.tv_email)
        userPhone = findViewById(R.id.tv_phone)
        userAddress = findViewById(R.id.tv_address)

        editProfile = findViewById(R.id.iv_edit_profile)
        signOut = findViewById(R.id.iv_sign_out)

        homeButton = findViewById(R.id.home_button)
        profileButton = findViewById(R.id.profile_button)
        addButton = findViewById(R.id.add_button)
        notificationButton = findViewById(R.id.notification_button)
        favouriteButton = findViewById(R.id.favorite_button)


        firestore.collection("Users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDoc = documents.first()
                    val name = userDoc.getString("name") ?: "N/A"
                    val phone = userDoc.getString("phone") ?: "N/A"
                    val address = userDoc.getString("address") ?: "N/A"
                    val profileImageUrl = userDoc.getString("profileImageUrl") ?: ""

                    // Set admin details to TextViews
                    userName.text = "Name: $name"
                    userEmail.text = "Email: $email"
                    userPhone.text = "Phone: $phone"
                    userAddress.text = "Address: $address"

                    // Load profile image if available
                    if (profileImageUrl.isNotEmpty()) {
                        Picasso.get().load(profileImageUrl).into(profileImage)
                        Picasso.get().load(profileImageUrl).into(profileLogo)
                    } else {
                        profileImage.setImageResource(R.drawable.profile_logo)
                        profileLogo.setImageResource(R.drawable.profile_logo)
                    }
                } else {
                    Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }





        // Edit Profile Click Event
        editProfile.setOnClickListener {
            val intent = Intent(this, UserEditProfile::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }

        // Sign Out Click Event
        signOut.setOnClickListener {
            signOutUser()
        }

        homeButton.setOnClickListener {
            val intent = Intent(this, UserHomePage::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }

        addButton.setOnClickListener {
            val intent = Intent(this, UserBooking::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }

        notificationButton.setOnClickListener {
            val intent = Intent(this, UserNotification::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }

        favouriteButton.setOnClickListener {
            val intent = Intent(this, UserHomePage::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }
    }

    private fun signOutUser() {
        Toast.makeText(this, "Signing Out...", Toast.LENGTH_SHORT).show()
        // Example sign-out logic
        // FirebaseAuth.getInstance().signOut();

        // Redirect to login screen
        val intent = Intent(this, UserSignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}


/*
package com.example.slotify.user

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.slotify.R
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

        // Initialize ImageViews
        BusinessName = findViewById(R.id.business_name)
        businessMoto = findViewById(R.id.business_moto)
        profileLogo = findViewById(R.id.profile)


        homeButton = findViewById(R.id.home_button)
        profileButton = findViewById(R.id.profile_button)
        addButton = findViewById(R.id.add_button)
        notificationButton = findViewById(R.id.notification_button)
        favouriteButton = findViewById(R.id.favourite_button)

        firestore.collection("Users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val adminDoc = documents.first()
                    val profileImageUrl = adminDoc.getString("profileImageUrl") ?: ""

                    // Load profile image if available
                    if (profileImageUrl.isNotEmpty()) {
                        Picasso.get().load(profileImageUrl).into(profileLogo)
                    } else {
                        profileLogo.setImageResource(R.drawable.profile_logo)
                    }

                } else {
                    Toast.makeText(this, "User logo not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }










        homeButton.setOnClickListener {
            val intent = Intent(this, UserHomePage::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }

        addButton.setOnClickListener {
            val intent = Intent(this, UserBooking::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }

        notificationButton.setOnClickListener {
            val intent = Intent(this, UserNotification::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }

        favouriteButton.setOnClickListener {
            val intent = Intent(this, UserHomePage::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }
    }
}
 */

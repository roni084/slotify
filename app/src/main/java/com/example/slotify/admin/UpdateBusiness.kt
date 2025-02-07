package com.example.slotify.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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

class UpdateBusiness : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // UI Components
    private lateinit var BusinessName: TextView
    private lateinit var businessMoto: TextView
    private lateinit var profileLogo: CircleImageView

    private lateinit var businessImage: ImageView
    private lateinit var name: TextView
    private lateinit var businessAddress: TextView
    private lateinit var businessPhone: TextView
    private lateinit var openingTime: TextView
    private lateinit var closingTime: TextView
    private lateinit var totalEmployee: TextView
    private lateinit var totalService: TextView
    private lateinit var offDay: TextView

    private lateinit var btnEdit: Button
    private lateinit var btnBack: Button

    private lateinit var homeButton: ImageButton
    private lateinit var profileButton: ImageButton
    private lateinit var addButton: ImageButton
    private lateinit var notificationButton: ImageButton
    private lateinit var dashboardButton: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_business)

        // Initialize FirebaseFirestore & FirebaseAuth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Retrieve the email from Intent
        val email = intent.getStringExtra("email") ?: return
        val businessType = intent.getStringExtra("businessType") ?: return
        val businessName = intent.getStringExtra("businessName") ?: return

        // Initialize ImageViews
        BusinessName = findViewById(R.id.business_name)
        businessMoto = findViewById(R.id.business_moto)
        profileLogo = findViewById(R.id.profile)

        businessImage = findViewById(R.id.iv_image)
        name = findViewById(R.id.tv_name)
        businessAddress = findViewById(R.id.tv_address)
        businessPhone = findViewById(R.id.tv_call)
        openingTime = findViewById(R.id.tv_opening)
        closingTime = findViewById(R.id.tv_closing)
        totalEmployee = findViewById(R.id.tv_total_employee)
        totalService = findViewById(R.id.tv_total_service)
        offDay = findViewById(R.id.tv_off_day)

        btnEdit = findViewById(R.id.btn_edit)
        btnBack = findViewById(R.id.btn_back)

        homeButton = findViewById(R.id.home_button)
        profileButton = findViewById(R.id.profile_button)
        addButton = findViewById(R.id.add_button)
        notificationButton = findViewById(R.id.notification_button)
        dashboardButton = findViewById(R.id.dashboard_button)

        firestore.collection("Admins").document(email)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val profileImageUrl = document.getString("profileImageUrl") ?: ""

                    if (profileImageUrl.isNotEmpty()) {
                        Picasso.get().load(profileImageUrl).into(profileLogo)
                    } else {
                        profileLogo.setImageResource(R.drawable.profile_logo)
                    }
                } else {
                    Toast.makeText(this, "Admin profile not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show()
            }


        firestore.collection(businessType).document(businessName)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val address = document.getString("address") ?: "N/A"
                    val phone = document.getString("call") ?: "N/A"
                    val opening_Time = document.getString("opening time") ?: "N/A"
                    val closing_Time = document.getString("closing time") ?: "N/A"
                    val employee = document.getString("total employees") ?: "N/A"
                    val services = document.getString("total services") ?: "N/A"
                    val off_day = document.getString("off day") ?: "N/A"
                    val businessImageUrl = document.getString("businessImageUrl") ?: ""

                    // Set business details to TextViews
                    name.text = "Name: $businessName"
                    businessAddress.text = "Address: $address"
                    businessPhone.text = "Call: $phone"
                    openingTime.text = "Opening Time: $opening_Time"
                    closingTime.text = "Closing Time: $closing_Time"
                    totalEmployee.text = "Total Employees: $employee"
                    totalService.text = "Total Services: $services"
                    offDay.text = "Off Day: $off_day"

                    // Load business image
                    if (businessImageUrl.isNotEmpty()) {
                        Picasso.get().load(businessImageUrl).into(businessImage)
                    } else {
                        businessImage.setImageResource(R.drawable.profile_logo)
                    }
                } else {
                    Toast.makeText(this, "Business profile not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error fetching business data", Toast.LENGTH_SHORT).show()
            }


        // Edit Profile Click Event
        btnEdit.setOnClickListener {
            val intent = Intent(this, EditBusiness::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }
        btnBack.setOnClickListener {
            val intent = Intent(this, AdminHomePage::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }

        profileLogo.setOnClickListener {
            val intent = Intent(this, AdminProfile::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }

        homeButton.setOnClickListener {
            val intent = Intent(this, AdminHomePage::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(this, AdminProfile::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }

        addButton.setOnClickListener {
            val intent = Intent(this, AddService::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }

        notificationButton.setOnClickListener {
            val intent = Intent(this, AdminNotification::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }

        dashboardButton.setOnClickListener {
            val intent = Intent(this, AdminHomePage::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }

    }
}
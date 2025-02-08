package com.example.slotify.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.slotify.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class AddService : AppCompatActivity() {
    private val IMAGE_REQ: Int = 1
    private var imagePath: Uri? = null
    private var imageUrl: String? = null

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // UI Components
    private lateinit var BusinessName: TextView
    private lateinit var businessMoto: TextView
    private lateinit var profileLogo: CircleImageView
    private lateinit var profileImage: CircleImageView
    private lateinit var uploadImage: Button
    private lateinit var etName: EditText
    private lateinit var etCost: EditText
    private lateinit var etTime: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnAdd: Button

    private lateinit var homeButton: ImageButton
    private lateinit var profileButton: ImageButton
    private lateinit var addButton: ImageButton
    private lateinit var notificationButton: ImageButton
    private lateinit var dashboardButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service)

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Retrieve email from intent
        val email = intent.getStringExtra("email") ?: return
        val businessType = intent.getStringExtra("businessType") ?: return
        val businessName = intent.getStringExtra("businessName") ?: return

        // Initialize UI Components
        BusinessName = findViewById(R.id.business_name)
        businessMoto = findViewById(R.id.business_moto)
        profileLogo = findViewById(R.id.profile)
        profileImage = findViewById(R.id.iv_image)
        uploadImage = findViewById(R.id.btn_upload)
        etName = findViewById(R.id.et_name)
        etCost = findViewById(R.id.et_cost)
        etTime = findViewById(R.id.et_time)
        etDescription = findViewById(R.id.et_descp)
        btnAdd = findViewById(R.id.btn_update)

        homeButton = findViewById(R.id.home_button)
        profileButton = findViewById(R.id.profile_button)
        addButton = findViewById(R.id.add_button)
        notificationButton = findViewById(R.id.notification_button)
        dashboardButton = findViewById(R.id.dashboard_button)

        // Load Admin Profile Image
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
                    Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show()
            }

        // Select Image from Gallery
        uploadImage.setOnClickListener { selectImage() }

        etCost.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val input = it.toString().replace(" TK", "").trim()
                    if (input.isNotEmpty() && input.matches(Regex("\\d+"))) {
                        etCost.removeTextChangedListener(this) // Temporarily remove listener to avoid infinite loop
                        etCost.setText("$input TK")
                        etCost.setSelection(etCost.text.length) // Move cursor to the end
                        etCost.addTextChangedListener(this) // Reattach listener
                    }
                }
            }
        })

        etTime.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val input = it.toString().replace(" Minutes", "").replace(" hour", "").replace(" hours", "").trim()
                    if (input.isNotEmpty() && input.matches(Regex("\\d+"))) {
                        val minutes = input.toInt()
                        val formattedTime = if (minutes < 60) {
                            "$minutes Minutes"
                        } else {
                            val hours = minutes / 60
                            val remainingMinutes = minutes % 60
                            if (remainingMinutes == 0) "$hours hour${if (hours > 1) "s" else ""}"
                            else "$hours hour${if (hours > 1) "s" else ""} $remainingMinutes Minutes"
                        }

                        etTime.removeTextChangedListener(this) // Temporarily remove listener to avoid infinite loop
                        etTime.setText(formattedTime)
                        etTime.setSelection(etTime.text.length) // Move cursor to the end
                        etTime.addTextChangedListener(this) // Reattach listener
                    }
                }
            }
        })

        // Store Service Data in Firestore
        btnAdd.setOnClickListener { addServiceToFirestore(businessName, email, businessType) }

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
            val intent = Intent(this, AdminProfile::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQ)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQ && resultCode == Activity.RESULT_OK && data != null) {
            imagePath = data.data
            Picasso.get().load(imagePath).into(profileImage)
            uploadImageToCloudinary()
        }
    }

    private fun uploadImageToCloudinary() {
        if (imagePath == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            return
        }

        MediaManager.get().upload(imagePath)
            .unsigned("Admins")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Toast.makeText(this@AddService, "Uploading image...", Toast.LENGTH_SHORT).show()
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    imageUrl = resultData["secure_url"].toString()
                    Toast.makeText(this@AddService, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                    Picasso.get().load(imageUrl).into(profileImage)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Toast.makeText(this@AddService, "Image upload failed: ${error.description}", Toast.LENGTH_SHORT).show()
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    private fun addServiceToFirestore(businessName: String, email: String, businessType: String) {
        val serviceName = etName.text.toString().trim()
        val cost = etCost.text.toString().trim()
        val serveTime = etTime.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (serviceName.isEmpty() || cost.isEmpty() || serveTime.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
            return
        }

        val serviceData = hashMapOf(
            "serviceName" to serviceName,
            "cost" to cost,
            "serveTime" to serveTime,
            "description" to description,
            "imageUrl" to (imageUrl ?: "")
        )

        firestore.collection(businessName).document(serviceName)
            .set(serviceData)
            .addOnSuccessListener {
                Toast.makeText(this, "Service added successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AdminHomePage::class.java)
                intent.putExtra("email", email)
                intent.putExtra("businessType", businessType)
                intent.putExtra("businessName", businessName)
                startActivity(intent)

            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add service: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

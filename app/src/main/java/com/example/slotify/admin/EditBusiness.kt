package com.example.slotify.admin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.slotify.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class EditBusiness : AppCompatActivity() {
    private val IMAGE_REQ: Int = 1
    private var imagePath: Uri? = null
    private var imageUrl: String? = null

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // UI Components
    private lateinit var BusinessName: TextView
    private lateinit var businessMoto: TextView
    private lateinit var profileLogo: CircleImageView

    private lateinit var businessImage: ImageView
    private lateinit var name: EditText
    private lateinit var address: EditText
    private lateinit var phone: EditText
    private lateinit var opening: EditText
    private lateinit var closing: EditText
    private lateinit var employees: EditText
    private lateinit var offDay: EditText


    private lateinit var uploadImage: Button
    private lateinit var updateBusiness: Button

    private lateinit var homeButton: ImageButton
    private lateinit var profileButton: ImageButton
    private lateinit var addButton: ImageButton
    private lateinit var notificationButton: ImageButton
    private lateinit var dashboardButton: ImageButton

    // Original data
    private var originalName: String = ""
    private var originalPhone: String = ""
    private var originalAddress: String = ""
    private var originalOpening: String = ""
    private var originalClosing: String = ""
    private var originalEmployees: String = ""
    private var originalOffDay: String = ""
    private var originalImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_business)

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Retrieve email from intent
        val email = intent.getStringExtra("email") ?: return
        val businessType = intent.getStringExtra("businessType") ?: return
        val businessName = intent.getStringExtra("businessName") ?: return

        // Initialize UI components
        initializeViews(email, businessType, businessName)

        // Load existing data
        loadBusinessData(email, businessType, businessName)

        // Set click listeners
        uploadImage.setOnClickListener { selectImage() }
        updateBusiness.setOnClickListener { updateProfile(email, businessType, businessName) }

        profileLogo.setOnClickListener {
            val intent = Intent(this, AdminHomePage::class.java)
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

    private fun initializeViews(email: String, businessType: String, businessName: String) {
        BusinessName = findViewById(R.id.business_name)
        businessMoto = findViewById(R.id.business_moto)
        profileLogo = findViewById(R.id.profile)

        businessImage = findViewById(R.id.iv_image)
        name = findViewById(R.id.et_name)
        address = findViewById(R.id.et_address)
        phone = findViewById(R.id.et_phone)
        opening = findViewById(R.id.et_opening)
        closing = findViewById(R.id.et_closing)
        employees = findViewById(R.id.et_employee)
        offDay = findViewById(R.id.et_off_day)

        uploadImage = findViewById(R.id.btn_upload)
        updateBusiness = findViewById(R.id.btn_update)

        homeButton = findViewById(R.id.home_button)
        profileButton = findViewById(R.id.profile_button)
        addButton = findViewById(R.id.add_button)
        notificationButton = findViewById(R.id.notification_button)
        dashboardButton = findViewById(R.id.dashboard_button)


        firestore.collection("Admins")
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
                    Toast.makeText(this, "logo not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadBusinessData(email: String, businessType: String, businessName: String) {
        firestore.collection(businessType).document(businessName)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    originalName = document.getString("name") ?: ""
                    originalPhone = document.getString("call") ?: ""
                    originalAddress = document.getString("address") ?: ""
                    originalOpening = document.getString("opening time") ?: ""
                    originalClosing = document.getString("closing time") ?: ""
                    originalEmployees = document.getString("total employees") ?: ""
                    originalOffDay = document.getString("off day") ?: ""

                    originalImageUrl = document.getString("businessImageUrl") ?: ""

                    name.setText(originalName)
                    phone.setText(originalPhone)
                    address.setText(originalAddress)
                    opening.setText(originalOpening)
                    closing.setText(originalClosing)
                    employees.setText(originalEmployees)
                    offDay.setText(originalOffDay)

                    if (originalImageUrl.isNotEmpty()) {
                        Picasso.get().load(originalImageUrl).into(businessImage)
                        Picasso.get().load(originalImageUrl).into(profileLogo)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQ)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQ && resultCode == RESULT_OK && data != null) {
            imagePath = data.data
            Picasso.get().load(imagePath).into(businessImage)
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
                    // Upload started
                    Toast.makeText(this@EditBusiness, "Upload started...", Toast.LENGTH_SHORT).show()
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // Upload in progress
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    // Assign to class-level variable (remove 'val' to use the existing declaration)
                    imageUrl = resultData["secure_url"].toString()
                    Toast.makeText(this@EditBusiness, "Image uploaded: $imageUrl", Toast.LENGTH_SHORT).show()

                    // Display the uploaded image immediately after success
                    Picasso.get().load(imageUrl).into(businessImage)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    // Upload failed
                    Toast.makeText(this@EditBusiness, "Upload failed: ${error.description}", Toast.LENGTH_SHORT).show()
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    // Upload rescheduled
                }
            })
            .dispatch()
    }


    private fun updateProfile(email: String, businessType: String, businessName: String) {
        val newName = name.text.toString().trim()
        val newPhone = phone.text.toString().trim()
        val newAddress = address.text.toString().trim()
        val newOpening = opening.text.toString().trim()
        val newClosing = closing.text.toString().trim()
        val newEmployees = employees.text.toString().trim()
        val newOffDay = offDay.text.toString().trim()

        val updates = HashMap<String, Any>()
        if (newName != originalName) updates["name"] = newName
        if (newPhone != originalPhone) updates["call"] = newPhone
        if (newAddress != originalAddress) updates["address"] = newAddress
        if (newOpening != originalOpening) updates["opening time"] = newOpening
        if (newClosing != originalClosing) updates["closing time"] = newClosing
        if (newEmployees != originalEmployees) updates["total employees"] = newEmployees
        if (newOffDay != originalOffDay) updates["off day"] = newOffDay

        // Only update image if a new one was uploaded
        if (imageUrl != null && imageUrl != originalImageUrl) {
            updates["businessImageUrl"] = imageUrl!!
        }

        val businessRef = firestore.collection(businessType).document(businessName)

        // Check if the document exists
        businessRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // If document exists, update it
                    businessRef.update(updates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                            redirectToAdminHome(email, businessType, businessName)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // If document does not exist, create it first
                    businessRef.set(updates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Business profile created & updated", Toast.LENGTH_SHORT).show()
                            redirectToAdminHome(email, businessType, businessName)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to create business profile: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error checking business data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to redirect after updating
    private fun redirectToAdminHome(email: String, businessType: String, businessName: String) {
        val intent = Intent(this, AdminHomePage::class.java)
        intent.putExtra("email", email)
        intent.putExtra("businessType", businessType)
        intent.putExtra("businessName", businessName)
        startActivity(intent)
    }

}

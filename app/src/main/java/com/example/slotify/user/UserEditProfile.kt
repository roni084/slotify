package com.example.slotify.user

import android.content.Intent
import android.net.Uri
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
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.slotify.R
import com.example.slotify.admin.AdminHomePage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserEditProfile : AppCompatActivity() {
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
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var userPhone: TextView
    private lateinit var userAddress: TextView

    private lateinit var uploadImage: Button
    private lateinit var updateProfile: Button

    private lateinit var homeButton: ImageButton
    private lateinit var profileButton: ImageButton
    private lateinit var addButton: ImageButton
    private lateinit var notificationButton: ImageButton
    private lateinit var favouriteButton: ImageButton

    // Original data
    private var originalName: String = ""
    private var originalPhone: String = ""
    private var originalAddress: String = ""
    private var originalImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_edit_profile)

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
        userName = findViewById(R.id.et_name)
        userEmail = findViewById(R.id.et_email)
        userPhone = findViewById(R.id.et_phone)
        userAddress = findViewById(R.id.et_address)
        uploadImage = findViewById(R.id.btn_upload)
        updateProfile = findViewById(R.id.btn_update)

        homeButton = findViewById(R.id.home_button)
        profileButton = findViewById(R.id.profile_button)
        addButton = findViewById(R.id.add_button)
        notificationButton = findViewById(R.id.notification_button)
        favouriteButton = findViewById(R.id.favorite_button)

        // Disable email field (not editable)
        userEmail.isEnabled = false
        userEmail.setText(email)

        firestore.collection("Users").document(email)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val profileImageUrl = document.getString("profileImageUrl") ?: ""

                    if (profileImageUrl.isNotEmpty()) {
                        Picasso.get().load(profileImageUrl).into(profileLogo)
                        Picasso.get().load(profileImageUrl).into(profileImage)
                    } else {
                        profileLogo.setImageResource(R.drawable.profile_logo)
                        profileImage.setImageResource(R.drawable.profile_logo)
                    }
                } else {
                    Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show()
            }

        // Load existing data
        loadUserData(email)

        // Set click listeners
        uploadImage.setOnClickListener { selectImage() }
        updateProfile.setOnClickListener { updateProfile(email) }

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

    private fun loadUserData(email: String) {
        firestore.collection("Admins").document(email)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    originalName = document.getString("name") ?: ""
                    originalPhone = document.getString("phone") ?: ""
                    originalAddress = document.getString("address") ?: ""
                    originalImageUrl = document.getString("profileImageUrl") ?: ""

                    userName.setText(originalName)
                    userPhone.setText(originalPhone)
                    userAddress.setText(originalAddress)
                    if (originalImageUrl.isNotEmpty()) {
                        Picasso.get().load(originalImageUrl).into(profileImage)
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
            .unsigned("Users")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    // Upload started
                    Toast.makeText(this@UserEditProfile, "Upload started...", Toast.LENGTH_SHORT).show()
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // Upload in progress
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    imageUrl = resultData["secure_url"].toString()

                    // Save to Firestore under Admin document
                    val adminRef = firestore.collection("Admins").document(userEmail.text.toString())
                    adminRef.update("profileImageUrl", imageUrl!!)
                        .addOnSuccessListener {
                            Toast.makeText(this@UserEditProfile, "Image uploaded and saved!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@UserEditProfile, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
                        }

                    // Load the image into ImageView
                    Picasso.get().load(imageUrl).into(profileImage)
                }


                override fun onError(requestId: String, error: ErrorInfo) {
                    // Upload failed
                    Toast.makeText(this@UserEditProfile, "Upload failed: ${error.description}", Toast.LENGTH_SHORT).show()
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    // Upload rescheduled
                }
            })
            .dispatch()
    }

    private fun updateProfile(email: String) {
        val newName = userName.text.toString().trim()
        val newPhone = userPhone.text.toString().trim()
        val newAddress = userAddress.text.toString().trim()

        val updates = HashMap<String, Any>()
        if (newName != originalName) updates["name"] = newName
        if (newPhone != originalPhone) updates["phone"] = newPhone
        if (newAddress != originalAddress) updates["address"] = newAddress

        // Only update image if a new one was uploaded
        if (imageUrl != null && imageUrl != originalImageUrl) {
            updates["profileImageUrl"] = imageUrl!!
        }

        if (updates.isNotEmpty()) {
            firestore.collection("Users").document(email)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()

                    // Redirect to AdminHomePage
                    val intent = Intent(this, UserHomePage::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No changes to update", Toast.LENGTH_SHORT).show()
        }
    }

}

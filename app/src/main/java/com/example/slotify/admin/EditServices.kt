package com.example.slotify.admin

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.slotify.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class EditServices : AppCompatActivity() {
    private val IMAGE_REQ: Int = 1
    private var imagePath: Uri? = null
    private var imageUrl: String? = null

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // UI Components
    private lateinit var BusinessName: TextView
    private lateinit var businessMoto: TextView
    private lateinit var profileLogo: CircleImageView
    private lateinit var serviceList: LinearLayout

    private lateinit var allService: Button

    private lateinit var homeButton: ImageButton
    private lateinit var profileButton: ImageButton
    private lateinit var addButton: ImageButton
    private lateinit var notificationButton: ImageButton
    private lateinit var dashboardButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_services)

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
        serviceList = findViewById(R.id.service_list)

        allService = findViewById(R.id.btn_all)

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

        // Fetch and display services
        fetchAndDisplayServicesInButton(email, businessType, businessName)

        fetchAndDisplayServices(email, businessType, businessName)

        allService.setOnClickListener {
            // update logic here
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
            val intent = Intent(this, AdminProfile::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }
    }

    private fun fetchAndDisplayServices(email: String, businessType: String, businessName: String) {
        val serviceList = mutableListOf<Service>()

        firestore.collection(businessName).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val service = document.toObject(Service::class.java)
                    serviceList.add(service)
                }

                val adapter = ServiceAdapter(this, serviceList) { selectedService ->
                    // Handle Edit Button Click
                    showDialog(email, businessType, businessName, selectedService.serviceName)
                }

                val recyclerView: RecyclerView = findViewById(R.id.rv_service_list)
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error fetching services: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun fetchAndDisplayServicesInButton(email: String, businessType: String, businessName: String) {
        firestore.collection(businessName).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    serviceList.removeAllViews() // Clear previous views if any

                    documents.forEach { document ->
                        val serviceName = document.getString("serviceName") ?: "No Name"

                        val button = Button(this)
                        button.text = serviceName
                        button.setBackgroundResource(R.drawable.button_background) // Ensure button matches the design
                        button.setTextColor(ContextCompat.getColor(this, R.color.black))
                        button.textSize = 14f
                        button.setPadding(16, 1, 16, 1)

                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.setMargins(8, 0, 8, 0) // Add spacing between buttons
                        button.layoutParams = params

                        button.setOnClickListener {
                            Toast.makeText(this, "Selected: $serviceName", Toast.LENGTH_SHORT).show()
                            // Handle button click if needed
                            showDialog(email, businessType, businessName, serviceName)

                        }

                        serviceList.addView(button) // Add button to the horizontal list
                    }
                } else {
                    Toast.makeText(this, "No services found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error fetching services: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDialog(email: String, businessType: String, businessName: String, serviceName: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_service, null)
        val servicePhoto: ImageView = dialogView.findViewById(R.id.iv_image)
        val btnUpload: Button = dialogView.findViewById(R.id.btn_upload)

        val name: TextInputEditText = dialogView.findViewById(R.id.et_name)
        val cost: TextInputEditText = dialogView.findViewById(R.id.et_cost)
        val time: TextInputEditText = dialogView.findViewById(R.id.et_time)
        val description: TextInputEditText = dialogView.findViewById(R.id.et_desp)
        val btnUpdate: Button = dialogView.findViewById(R.id.btn_update)
        val btnDelete: Button = dialogView.findViewById(R.id.btn_delete)

        cost.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val input = it.toString().replace(" TK", "").trim()
                    if (input.isNotEmpty() && input.matches(Regex("\\d+"))) {
                        cost.removeTextChangedListener(this) // Temporarily remove listener to avoid infinite loop
                        cost.setText("$input TK")
                        cost.text?.let { it1 -> cost.setSelection(it1.length) } // Move cursor to the end
                        cost.addTextChangedListener(this) // Reattach listener
                    }
                }
            }
        })

        time.addTextChangedListener(object : TextWatcher {
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

                        time.removeTextChangedListener(this) // Temporarily remove listener to avoid infinite loop
                        time.setText(formattedTime)
                        time.text?.let { it1 -> time.setSelection(it1.length) } // Move cursor to the end
                        time.addTextChangedListener(this) // Reattach listener
                    }
                }
            }
        })

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Fetch existing service data
        firestore.collection(businessName).document(serviceName).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    name.setText(document.getString("serviceName"))
                    cost.setText(document.getString("cost"))
                    time.setText(document.getString("serveTime"))
                    description.setText(document.getString("description"))
                    val imageUrl = document.getString("imageUrl") ?: ""
                    if (imageUrl.isNotEmpty()) {
                        Picasso.get().load(imageUrl).into(servicePhoto)
                    }
                }
            }

        btnUpload.setOnClickListener {
            selectImage()
        }

        btnUpdate.setOnClickListener {
            val updatedData = hashMapOf(
                "serviceName" to name.text.toString(),
                "cost" to cost.text.toString(),
                "serveTime" to time.text.toString(),
                "description" to description.text.toString(),
                "imageUrl" to (imageUrl ?: "")
            )

            firestore.collection(businessName).document(serviceName)
                .update(updatedData as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Service updated successfully!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    fetchAndDisplayServicesInButton(email, businessType, businessName)
                    fetchAndDisplayServices(email, businessType, businessName) // Refresh the service list
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update service: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnDelete.setOnClickListener {
            val dialogView1 = LayoutInflater.from(this).inflate(R.layout.dialog_delete, null)
            val yes: TextView = dialogView1.findViewById(R.id.tv_yes)
            val no: TextView = dialogView1.findViewById(R.id.tv_no)

            val dialog1 = AlertDialog.Builder(this)
                .setView(dialogView1)
                .create()

            yes.setOnClickListener {
                // Delete the service from Firestore
                firestore.collection(businessName).document(serviceName)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Service deleted successfully!", Toast.LENGTH_SHORT).show()
                        dialog1.dismiss() // Close the confirmation dialog
                        dialog.dismiss() // Close the main dialog
                        fetchAndDisplayServicesInButton(email, businessType, businessName) // Refresh the service list
                        fetchAndDisplayServices(email, businessType, businessName) // Refresh the service list
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to delete service: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            no.setOnClickListener {
                dialog1.dismiss()
            }

            dialog1.show()
        }

        dialog.show()
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
                    Toast.makeText(this@EditServices, "Uploading image...", Toast.LENGTH_SHORT).show()
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    //TODO("Not yet implemented")
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    imageUrl = resultData["secure_url"].toString()
                    Toast.makeText(this@EditServices, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Toast.makeText(this@EditServices, "Image upload failed: ${error.description}", Toast.LENGTH_SHORT).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    //TODO("Not yet implemented")
                }
            })
            .dispatch()
    }
}
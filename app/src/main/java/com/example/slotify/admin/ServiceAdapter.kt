package com.example.slotify.admin
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.slotify.R
import com.squareup.picasso.Picasso

class ServiceAdapter(
    private val context: Context,
    private val services: List<Service>,
    private val onEditClick: (Service) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val servicePhoto: ImageView = itemView.findViewById(R.id.iv_service_photo)
        val serviceName: TextView = itemView.findViewById(R.id.tv_service_name)
        val editButton: Button = itemView.findViewById(R.id.btn_edit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.admin_item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]

        holder.serviceName.text = service.serviceName
        if (service.imageUrl.isNotEmpty()) {
            Picasso.get().load(service.imageUrl).into(holder.servicePhoto)
        } else {
            holder.servicePhoto.setImageResource(R.drawable.profile_logo) // Placeholder image
        }

        holder.editButton.setOnClickListener { onEditClick(service) }
    }

    override fun getItemCount(): Int = services.size
}

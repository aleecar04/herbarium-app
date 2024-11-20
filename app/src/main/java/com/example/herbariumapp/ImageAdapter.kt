package com.example.herbariumapp

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private val imageList = mutableListOf<Bitmap>()

    // Crear la vista para cada elemento en el RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    // Bind de la imagen al ImageView
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val bitmap = imageList[position]
        holder.imageView.setImageBitmap(bitmap)
    }

    // Número de elementos en la lista
    override fun getItemCount(): Int = imageList.size

    // Agregar imagen a la lista y notificar al adaptador
    fun addImage(image: Bitmap) {
        imageList.add(image)
        notifyItemInserted(imageList.size - 1)
    }

    // ViewHolder para cada imagen
    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }
}

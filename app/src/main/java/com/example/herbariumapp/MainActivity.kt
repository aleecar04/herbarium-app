package com.example.herbariumapp

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.herbariumapp.ImageAdapter
import com.example.herbariumapp.R
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var viewGalleryButton: Button
    private lateinit var cameraButton: Button
    private lateinit var backButton: Button
    private lateinit var galleryTitle: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter

    // Usamos un contrato para manejar el resultado de la selección de archivo
    private val openFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Obtener la URI del archivo seleccionado
                val uri = result.data?.data
                uri?.let {
                    // Obtener el bitmap de la URI seleccionada
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    imageAdapter.addImage(bitmap)  // Agregar la imagen al RecyclerView
                }
            } else {
                // Si el resultado es cancelado, cerrar la actividad
                finish() // Regresa a la actividad anterior
            }
        }

    // Solicitar imagen de la cámara
    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                imageAdapter.addImage(imageBitmap)  // Agregar la imagen capturada al RecyclerView
            } else {
                // Si el resultado es cancelado, cerrar la actividad
                finish() // Regresa a la actividad anterior
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencias a los elementos del layout
        viewGalleryButton = findViewById(R.id.btnViewGallery)
        cameraButton = findViewById(R.id.btnCamera)
        backButton = findViewById(R.id.btnBack)
        galleryTitle = findViewById(R.id.tvGalleryTitle)
        recyclerView = findViewById(R.id.recyclerView)

        // Configuración del RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        imageAdapter = ImageAdapter()
        recyclerView.adapter = imageAdapter

        // Verificar si se tienen los permisos para usar la cámara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        }

        // Acción cuando el botón es presionado para seleccionar una imagen
        viewGalleryButton.setOnClickListener {
            openFilePicker()
        }

        // Acción cuando el botón es presionado para tomar una foto
        cameraButton.setOnClickListener {
            // Lanza la CameraActivity para tomar una foto
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        // Acción cuando el botón "Volver" es presionado
        backButton.setOnClickListener {
            finish()  // Cierra la actividad y regresa a la anterior
        }
    }

    private fun openFilePicker() {
        // Creamos un intent para abrir el selector de imágenes
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"  // Solo imágenes
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        // Lanzamos el intent para elegir una imagen
        openFileLauncher.launch(intent)
    }

    private fun openCamera() {
        // Intent para abrir la cámara y tomar una foto
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePhotoLauncher.launch(intent)
    }

    // Manejo de permisos de la cámara (si es necesario)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de cámara concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

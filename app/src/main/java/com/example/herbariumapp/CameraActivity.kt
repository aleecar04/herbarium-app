package com.example.herbariumapp

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class CameraActivity : AppCompatActivity() {

    private lateinit var backButton: Button

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                // Aquí puedes agregar el bitmap a tu RecyclerView o almacenarlo
                // Para esto puedes enviar la imagen a MainActivity si lo deseas
            } else {
                Toast.makeText(this, "Captura cancelada o fallida", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_main)  // Asegúrate de que el layout existe y está bien configurado

        backButton = findViewById(R.id.backButton)  // Referencia al botón Volver

        // Llamamos a la cámara
        openCamera()

        // Acción del botón "Volver"
        backButton.setOnClickListener {
            finish()  // Regresa a MainActivity
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Verificar si hay alguna aplicación que pueda manejar este intent
        if (intent.resolveActivity(packageManager) != null) {
            takePhotoLauncher.launch(intent)
        } else {
            Toast.makeText(this, "No hay aplicación de cámara disponible", Toast.LENGTH_SHORT).show()
        }
    }
}

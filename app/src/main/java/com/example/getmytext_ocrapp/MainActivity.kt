package com.example.getmytext_ocrapp

import android.app.LauncherActivity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.method.ScrollingMovementMethod
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private lateinit var cameraImage: AppCompatImageView
    private lateinit var cameraButton: AppCompatButton
    private lateinit var ocrResult: AppCompatTextView
    private lateinit var copyText: AppCompatButton

    private var currentPhotoPath: String? = null
    private lateinit var requestPermissionLauncherActivity: ActivityResultLauncher<String>
    private lateinit var takePicture: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        cameraImage = findViewById(R.id.cameraImage)
        cameraButton = findViewById(R.id.captureButton)
        ocrResult = findViewById(R.id.ocrResult)
        copyText = findViewById(R.id.copyButton)

        requestPermissionLauncherActivity =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    captureImage()
                } else {
                    Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                currentPhotoPath?.let { path ->
                    val bitmap = BitmapFactory.decodeFile(path)
                    cameraImage.setImageBitmap(bitmap)
                    recognizeText(bitmap)

                }
            } else
                Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show()
        }
        cameraImage.setOnClickListener {
            requestPermissionLauncherActivity.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun captureImage() {
        val photoFile: File? = try {
            createImageFile()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error occurred ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
        photoFile?.also {
            val photoUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", it)
            takePicture.launch(photoUri)
        }

    }

    private fun recognizeText(bitmap: Bitmap){
        val image = InputImage.fromBitmap(bitmap, 0)

        // When using Latin script library
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image).addOnSuccessListener { ocrText ->
            ocrResult.text = ocrText.text
            ocrResult.movementMethod = ScrollingMovementMethod()
            copyText.isVisible = true
            copyText.setOnClickListener {
                val clipBoard = ContextCompat.getSystemService(this, android.content.ClipboardManager::class.java)
                val clip= android.content.ClipData.newPlainText("Recognized Text", ocrText.text)
                clipBoard?.setPrimaryClip(clip)
                Toast.makeText(this, "Text Copied.", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener { e->
            Toast.makeText(this, "Failed to recognize text, ${e.message}", Toast.LENGTH_SHORT).show()
        }

        // When using Chinese script library
//        val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

        // When using Devanagari script library
//        val recognizer = TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())

        // When using Japanese script library
//        val recognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

        // When using Korean script library
//        val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

    }
}
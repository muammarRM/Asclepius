package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import org.tensorflow.lite.task.gms.vision.classifier.Classifications
import java.io.File
import com.yalantis.ucrop.UCrop
import android.app.AlertDialog

class MainActivity : AppCompatActivity(), ImageClassifierHelper.ClassifierListener {
    private lateinit var binding: ActivityMainBinding
    private var currentImageUri: Uri? = null
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private var aspectRatio: Pair<Float, Float> = Pair(16f, 9f)

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("imageUri", currentImageUri?.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageClassifierHelper = ImageClassifierHelper(context = this, classifierListener = this)

        if (savedInstanceState != null) {
            val uriString = savedInstanceState.getString("imageUri")
            if (uriString != null) {
                currentImageUri = Uri.parse(uriString)
                showImage()
            }
        }

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener { analyzeImage() }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            showAspectRatioDialog(uri)
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun showAspectRatioDialog(imageUri: Uri) {
        val ratios = arrayOf("1:1", "16:9", "4:3")
        val aspectRatios = arrayOf(
            Pair(1f, 1f),
            Pair(16f, 9f),
            Pair(4f, 3f)
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Aspect Ratio")
            .setItems(ratios) { _, which ->
                aspectRatio = aspectRatios[which]
                cropImage(imageUri)
            }
            .show()
    }

    private fun cropImage(imageUri: Uri) {
        showLoading(true)
        val destinationUri = Uri.fromFile(File.createTempFile("cropped_image_", ".jpg", cacheDir))
        UCrop.of(imageUri, destinationUri)
            .withAspectRatio(aspectRatio.first, aspectRatio.second)
            .withMaxResultSize(800, 800)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        showLoading(false)
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                UCrop.REQUEST_CROP -> {
                    val resultUri = UCrop.getOutput(data!!)
                    resultUri?.let {
                        currentImageUri = it
                        showImage()
                    }
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            showToast(cropError?.message ?: "Crop error")
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun analyzeImage() {
        currentImageUri?.let { uri ->
            showLoading(true)
            imageClassifierHelper.classifyStaticImage(uri)
        } ?: showToast("Please select an image first.")
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onError(error: String) {
        showToast(error)
    }

    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
        results?.let {
            val firstClassification = it.first().categories.first()
            val resultText = firstClassification.label
            val confidenceScore = firstClassification.score * 100

            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, currentImageUri.toString())
            intent.putExtra(ResultActivity.EXTRA_RESULT, resultText)
            intent.putExtra(ResultActivity.EXTRA_CONFIDENCE, confidenceScore)

            startActivity(intent)
        }
        showLoading(false)
    }
}

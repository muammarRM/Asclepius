package com.dicoding.asclepius.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.entity.AppDatabase
import com.dicoding.asclepius.entity.Prediction
import kotlinx.coroutines.launch

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appDatabase = AppDatabase.getDatabase(this)

        val imageUriString = intent.getStringExtra(EXTRA_IMAGE_URI)
        val resultString = intent.getStringExtra(EXTRA_RESULT)
        val confidenceValue = intent.getFloatExtra(EXTRA_CONFIDENCE, 0f)

        imageUriString?.let {
            val imageUri = Uri.parse(it)
            binding.resultImage.setImageURI(imageUri)
        }

        binding.resultText.text = getString(R.string.result_text, resultString, confidenceValue)
        savePredictionToDatabase(imageUriString, resultString, confidenceValue)
    }

    private fun savePredictionToDatabase(imageUriString: String?, resultString: String?, confidenceValue: Float) {
        val prediction = Prediction(
            imageUri = imageUriString ?: "",
            result = resultString ?: "",
            confidence = confidenceValue
        )

        lifecycleScope.launch {
            appDatabase.predictionDao().insert(prediction)
        }
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT = "extra_result"
        const val EXTRA_CONFIDENCE = "extra_confidence"
    }
}
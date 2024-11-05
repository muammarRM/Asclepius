package com.dicoding.asclepius.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.asclepius.NewsAdapter
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.entity.AppDatabase
import com.dicoding.asclepius.entity.Prediction
import com.dicoding.asclepius.retrofit.ArticlesItem
import com.dicoding.asclepius.retrofit.NewsApi
import com.yalantis.ucrop.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var appDatabase: AppDatabase
    private lateinit var newsAdapter: NewsAdapter

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

        setupRecyclerView()

        fetchCancerNews()
    }
    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        newsAdapter = NewsAdapter(emptyList())
        binding.recyclerView.adapter = newsAdapter
    }

    private fun fetchCancerNews() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://newsapi.org/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(NewsApi::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.getCancerNews("cancer", "health", "en", "80118bd1b806446fb96e475768e489ec")
                updateNewsUI(response.articles)
            } catch (e: Exception) {
                showToast("Failed to fetch news: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateNewsUI(articles: List<ArticlesItem>) {
        runOnUiThread {
            newsAdapter.updateArticles(articles)
        }
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
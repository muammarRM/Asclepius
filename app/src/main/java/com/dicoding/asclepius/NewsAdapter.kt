package com.dicoding.asclepius

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.asclepius.retrofit.ArticlesItem
import com.dicoding.asclepius.databinding.ItemNewsBinding
import com.bumptech.glide.Glide

class NewsAdapter(private var articles: List<ArticlesItem>) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(private val binding: ItemNewsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: ArticlesItem) {

            binding.newsTitle.text = article.title ?: "No Title Available"
            binding.newsDescription.text = article.description?.toString() ?: "No Description Available"

            if (article.urlToImage != null) {
                Glide.with(binding.newsImage.context)
                    .load(article.urlToImage.toString())
                    .placeholder(R.drawable.ic_place_holder)
                    .into(binding.newsImage)
            } else {
                binding.newsImage.setImageResource(R.drawable.ic_place_holder)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = articles[position]
        holder.bind(article)
    }

    override fun getItemCount() = articles.size

    fun updateArticles(newArticles: List<ArticlesItem>) {
        articles = newArticles
        notifyDataSetChanged()
    }
}

package com.hunsu.climbfeedback.mainfrag.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hunsu.climbfeedback.R

data class Item(val imageResId: Int, val text: String)

class RvAdapter (
    private val context: Context,
    private val itemList: List<Item>,
    private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<RvAdapter.RvViewHolder>() {
    class RvViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.guide_thumbnail)
        val textView: TextView = itemView.findViewById(R.id.guide_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item, parent, false)
        return RvViewHolder(view)
    }

    override fun onBindViewHolder(holder: RvViewHolder, position: Int) {
        val item = itemList[position]

        val bitmap = BitmapFactory.decodeResource(holder.itemView.context.resources, item.imageResId)
        holder.imageView.setImageBitmap(bitmap)

        holder.textView.text = item.text

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = itemList.size
}
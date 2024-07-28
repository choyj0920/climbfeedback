package com.hunsu.climbfeedback

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.hunsu.climbfeedback.R

class FramesAdapter(
    private val frames: List<Bitmap>,
    private val onClick: (Bitmap,Int) -> Unit
) : RecyclerView.Adapter<FramesAdapter.FrameViewHolder>() {

    inner class FrameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val frameImageView: ImageView = itemView.findViewById(R.id.frameImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FrameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_frame, parent, false)
        return FrameViewHolder(view)
    }

    override fun onBindViewHolder(holder: FrameViewHolder, position: Int) {
        val bitmap = frames[position]
        holder.frameImageView.setImageBitmap(bitmap)
        holder.itemView.setOnClickListener {
            onClick(bitmap,position)
        }
    }

    override fun getItemCount(): Int = frames.size
}

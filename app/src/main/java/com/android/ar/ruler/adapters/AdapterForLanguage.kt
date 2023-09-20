package com.android.ar.ruler.adapters

import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.ar.ruler.R
import com.android.ar.ruler.models.LanguagesModel
import com.android.ar.ruler.utils.Constants
import java.util.Locale

class AdapterForLanguage(private val prefUtils: SharedPreferences, private val arrayList: ArrayList<LanguagesModel>, val itemClick:(LanguagesModel, Int)->Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var isSelected = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_language, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val language: LanguagesModel = arrayList[position]
        (holder as ViewHolder)
        holder.tvLanguage.text = language.name

        holder.itemView.setOnClickListener {
            itemClick(language,position)
            isSelected = position
            notifyDataSetChanged()
        }

        if (isSelected != -1) {
            if (position == isSelected) {
                holder.selectedImage.setImageResource(R.drawable.ic_select)
            } else {
                holder.selectedImage.setImageResource(R.drawable.ic_unselect)
            }
        } else {
            if (prefUtils.getString(Constants.CHANGE_LANGUAGE, Locale.getDefault().language) == language.code) {
                holder.selectedImage.setImageResource(R.drawable.ic_select)
            } else {
                holder.selectedImage.setImageResource(R.drawable.ic_unselect)
            }
        }
    }

    fun getList(): ArrayList<LanguagesModel> {
        return arrayList
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val selectedImage: ImageView = itemView.findViewById(R.id.selectedImage)
        val tvLanguage: TextView = itemView.findViewById(R.id.tvLanguage)
    }

}
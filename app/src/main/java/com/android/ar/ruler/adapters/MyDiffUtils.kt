package com.android.ar.ruler.adapters

import androidx.recyclerview.widget.DiffUtil
import com.android.ar.ruler.models.Person

class MyDiffUtils(
    private val oldList: List<Person>,
    private val newList: List<Person>
): DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when{
            oldList[oldItemPosition].id != newList[newItemPosition].id ->{
                false
            }
            oldList[oldItemPosition].name != newList[newItemPosition].name ->{
                false
            }
            oldList[oldItemPosition].age != newList[newItemPosition].age ->{
                false
            }
            else->{
                true
            }
        }
    }
}
package com.android.ar.ruler.adapters

//import android.content.Context
//import android.content.res.Resources
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.scorpio.antitheftalarm.R
//import com.scorpio.antitheftalarm.advertisement.NativeHelper
//import com.scorpio.antitheftalarm.databinding.MenuAdapterAdsBinding
//import com.scorpio.antitheftalarm.databinding.MenuAdapterFirstBinding
//import com.scorpio.antitheftalarm.databinding.MenuAdapterLastBinding
//import com.scorpio.antitheftalarm.databinding.MenuAdapterOtherBinding
//import com.scorpio.antitheftalarm.models.MainActivityItemsData
//
//class RecyclerViewWithMultipleViews(private val context: Context) : ListAdapter<MainActivityItemsData, RecyclerView.ViewHolder>(DiffCallback()) {
//
//    private val itemFirst = 1
//    private val itemOther = 2
//    private val itemLast = 3
//    private val itemAds = 4
//
//    private class DiffCallback : DiffUtil.ItemCallback<MainActivityItemsData>(){
//        override fun areItemsTheSame(
//            oldItem: MainActivityItemsData,
//            newItem: MainActivityItemsData
//        ) = oldItem.name == newItem.name
//
//        override fun areContentsTheSame(
//            oldItem: MainActivityItemsData,
//            newItem: MainActivityItemsData
//        ) = oldItem == newItem
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return when(viewType){
//            itemFirst -> FirstMenuViewHolder(MenuAdapterFirstBinding.inflate(LayoutInflater.from(parent.context),parent,false))
//            itemLast -> LastMenuViewHolder(MenuAdapterLastBinding.inflate(LayoutInflater.from(parent.context),parent,false))
//            itemOther -> OtherMenuViewHolder(MenuAdapterOtherBinding.inflate(LayoutInflater.from(parent.context),parent,false))
//            itemAds -> AdsMenuViewHolder(MenuAdapterAdsBinding.inflate(LayoutInflater.from(parent.context),parent,false))
//            else -> {throw Resources.NotFoundException("Class Not Found")}
//        }
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        val item: MainActivityItemsData = getItem(position)
//        when(holder){
//            is FirstMenuViewHolder -> { holder.onBind(item) }
//            is LastMenuViewHolder -> { holder.onBind(item) }
//            is OtherMenuViewHolder -> { holder.onBind(item) }
//            is AdsMenuViewHolder -> { holder.onBind(item) }
//        }
//    }
//
//    override fun getItemViewType(position: Int): Int {
//        return when (position) {
//            0 -> itemFirst
//            currentList.size - 1 -> itemLast
//            1 -> itemAds
//            currentList.size - 2 -> itemAds
//            else -> itemOther
//        }
//    }
//
//    inner class FirstMenuViewHolder(private var binding: MenuAdapterFirstBinding) : RecyclerView.ViewHolder(binding.root){
//        fun onBind(data: MainActivityItemsData){
//
//        }
//    }
//
//    inner class LastMenuViewHolder(binding: MenuAdapterLastBinding) : RecyclerView.ViewHolder(binding.root){
//        fun onBind(data: MainActivityItemsData){
//
//        }
//    }
//
//    inner class OtherMenuViewHolder(private val binding: MenuAdapterOtherBinding) : RecyclerView.ViewHolder(binding.root){
//        fun onBind(data: MainActivityItemsData){
//            binding.apply {
//                Glide.with(imgDetection).load(data.image).into(imgDetection)
//                txtDetection.text = data.name
//            }
//        }
//    }
//
//    inner class AdsMenuViewHolder(private val binding: MenuAdapterAdsBinding) : RecyclerView.ViewHolder(binding.root){
//        fun onBind(data: MainActivityItemsData){
//            binding.apply {
//                NativeHelper(context)
//                    .loadAdsWithConfiguration(
//                        clAds,
//                        admobNativeContainer,
//                        if(data.name == "ad") 270 else 100,
//                        context.resources.getString(R.string.admob_main_native),
//                        "",
//                        1
//                    ) {}
//            }
//        }
//    }
//}
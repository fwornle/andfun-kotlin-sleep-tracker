package com.example.android.trackmysleepquality.sleeptracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.databinding.ListItemSleepNightBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val ITEM_VIEW_TYPE_HEADER = 0
private val ITEM_VIEW_TYPE_ITEM = 1

class SleepNightAdapter(val clickListener: SleepNightListener): ListAdapter<DataItem, RecyclerView.ViewHolder>(SleepNightDiffCallback()) {

    // allow ItemViewType to be determined
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.SleepNightItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    // allow current ViewHolder item to be bound to it's respective data
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        // bind data to current ViewHolder item (sleep quality VH has a 'bind' method)
        when (holder) {
            is ViewHolder -> {
                val nightItem = getItem(position) as DataItem.SleepNightItem
                holder.bind(clickListener, nightItem.sleepNight)
            }
        }

    }

    // returns a ViewHolder (of 'generic' type [RecyclerView.ViewHolder] as it can be of both
    // types know to this adapter (TextView-ish for header, custom for sleep quality)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        // return ViewHolder according to the type of item to be created
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)

            // the adapter only knows the above two types of ViewHolders
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }

    }

    // ViewHolder class used to display the sleep quality information (icon + text - from layout)
    class ViewHolder private constructor(val binding: ListItemSleepNightBinding):
        RecyclerView.ViewHolder(binding.root) {

        // inflate layout for RC views and use it to initialize the VH
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSleepNightBinding
                    .inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        // binding method
        fun bind(clickListener: SleepNightListener, item: SleepNight) {

            // bind current SleepNight item to layout variable 'sleep'
            binding.sleep = item

            // bind clickListener callback function to layout variable 'clickListener'
            binding.clickListener = clickListener

            // flush binding queue (?!)
            binding.executePendingBindings()

        }

    }

    // additional ViewHolder class (used to display the header)
    class TextViewHolder(view: View): RecyclerView.ViewHolder(view) {

        // offer 'from' method to inflate the header layout (here from the resource layout file)
        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.header, parent, false)
                return TextViewHolder(view)
            }
        }

    }

    // view data differ...
    class SleepNightDiffCallback: DiffUtil.ItemCallback<DataItem>() {

        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }

    }

    // convert a List<SleepNight> (from DB) to a List<DataItem> (for display in RecyclerView)
    // ... this adds the header
    // ... using Coroutines as this can involve reading data from the DB
    private val adapterScope = CoroutineScope(Dispatchers.Default)
    fun addHeaderAndSubmitList(list: List<SleepNight>?) {
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(DataItem.Header)
                else -> listOf(DataItem.Header) + list.map { DataItem.SleepNightItem(it) }
            }

            // this will wait for the 'items' to be delivered by the adapterScope (coroutine)
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

}


// click listener - defined here, as we will use it from within the ViewHolder (which knows
// on what 'view item' the user has clicked)
//
// parameter 'clickListener' is defined to hold a callback function that expects the sleepID
// and calls upon some function body (Unit - this can be passed to the constructor of the class
// as lambda)
class SleepNightListener(val clickListener: (sleepId: Long) -> Unit) {
    fun onClick(night: SleepNight) = clickListener(night.nightId)
}


// class DataItem is used to define the two principal types of list items to be handled by the
// adapter when preparing views (inside the ViewHolder) for the RecyclerView
//
// sealed to prevent other parts of the code to extend the class with further items
sealed class DataItem {

    // each DataItems has an ID
    // ... which can be set to a value dependent on the type of item it represents (see overrides)
    abstract val id: Long

    // wrapper around class SleepNight - map 'id' to 'nightId' of the wrapped SleepNight object
    data class SleepNightItem(val sleepNight: SleepNight): DataItem() {
        override val id = sleepNight.nightId
    }

    // this header will have a fixed ID (min value)
    object Header: DataItem() {
        override val id = Long.MIN_VALUE
    }

}
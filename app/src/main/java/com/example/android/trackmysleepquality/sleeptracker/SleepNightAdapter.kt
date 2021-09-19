package com.example.android.trackmysleepquality.sleeptracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.databinding.ListItemSleepNightBinding

class SleepNightAdapter(val clickListener: SleepNightListener): ListAdapter<SleepNight, SleepNightAdapter.ViewHolder>(SleepNightDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // bind data to current ViewHolder item
        holder.bind(clickListener, getItem(position))

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    /**
     * ViewHolder that holds our view (from layout)
     */
    class ViewHolder private constructor(val binding: ListItemSleepNightBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: SleepNightListener, item: SleepNight) {

            // bind current SleepNight item to layout variable 'sleep'
            binding.sleep = item

            // bind clickListener callback function to layout variable 'clickListener'
            binding.clickListener = clickListener

            // flush binding queue (?!)
            binding.executePendingBindings()

        }

        // inflate layout for RC views and use it to initialize the VH
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSleepNightBinding
                    .inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    // view data differ...
    class SleepNightDiffCallback: DiffUtil.ItemCallback<SleepNight>() {

        override fun areItemsTheSame(oldItem: SleepNight, newItem: SleepNight): Boolean {
            return oldItem.nightId == newItem.nightId
        }

        override fun areContentsTheSame(oldItem: SleepNight, newItem: SleepNight): Boolean {
            return oldItem == newItem
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



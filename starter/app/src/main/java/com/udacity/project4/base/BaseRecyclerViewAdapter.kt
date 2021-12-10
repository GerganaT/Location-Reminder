package com.udacity.project4.base

import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.udacity.project4.R
import androidx.appcompat.widget.PopupMenu

abstract class BaseRecyclerViewAdapter<T>(private val callback: ((item: T) -> Unit)? = null) :
    RecyclerView.Adapter<DataBindingViewHolder<T>>() {

    private var _items: MutableList<T> = mutableListOf()

    /**
     * Returns the _items data
     */
    private val items: List<T>?
        get() = this._items

    override fun getItemCount() = _items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder<T> {
        val layoutInflater = LayoutInflater.from(parent.context)

        val binding = DataBindingUtil
            .inflate<ViewDataBinding>(layoutInflater, getLayoutRes(viewType), parent, false)
        binding.lifecycleOwner = getLifecycleOwner()
        return DataBindingViewHolder(binding)
    }


    override fun onBindViewHolder(holder: DataBindingViewHolder<T>, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            callback?.invoke(item)
            val cntxt = holder.itemView.context
            val popupMenu = PopupMenu(cntxt, holder.itemView)
            popupMenu.inflate(R.menu.reminder_options)
            popupMenu.gravity = Gravity.END
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.edit_reminder -> {
                        //TODO add implementation
                        true
                    }
                    R.id.delete_reminder -> {
                        //TODO add implementation
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()

        }
    }

    fun getItem(position: Int) = _items[position]

    /**
     * Adds data to the actual Dataset
     *
     * @param items to be merged
     */
    fun addData(items: List<T>) {
        _items.addAll(items)
        notifyDataSetChanged()
    }

    /**
     * Clears the _items data
     */
    fun clear() {
        _items.clear()
        notifyDataSetChanged()
    }

    @LayoutRes
    abstract fun getLayoutRes(viewType: Int): Int

    open fun getLifecycleOwner(): LifecycleOwner? {
        return null
    }
}


package com.tirtagt.sgp_bangkit.api

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/** https://developer.android.com/develop/ui/views/layout/recyclerview#implement-adapter */
class BasicListAdapter(private val dataset: ArrayList<SGPBangkitItem>): RecyclerView.Adapter<BasicListAdapter.ViewHolder>() {
    private val linearBackgroundColorState = arrayListOf<Boolean>()
    var selectedItemId: Int? = null
    var selectedItemPos: Int? = null

    init {
        for (i in 1..dataset.size) {
            linearBackgroundColorState.add(false)
        }
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val linearLayout = view
        val textView: TextView = view.findViewById(R.id.recyclerViewTextItem)

        init {
            textView.text = ""
            textView.tag = null
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.text_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = dataset[position].nama
        viewHolder.textView.tag = dataset[position].id
        viewHolder.linearLayout.setOnClickListener { onItemClicked(dataset[position].id, position) }
        if (linearBackgroundColorState[position]) {
            viewHolder.linearLayout.setBackgroundColor(Color.LTGRAY)
            viewHolder.textView.setTextColor(Color.BLACK)
        }
        else {
            viewHolder.linearLayout.setBackgroundColor(Color.TRANSPARENT)
            viewHolder.textView.setTextColor(Color.WHITE)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return dataset.size
    }

    private fun onItemClicked(itemId: Int, position: Int) {
        val oldItemPos = selectedItemPos
        if (oldItemPos != null) {
            linearBackgroundColorState[oldItemPos] = false
            notifyItemChanged(oldItemPos)
        }

        selectedItemId = itemId
        selectedItemPos = position
        linearBackgroundColorState[position] = true
        notifyItemChanged(position)
    }
}
package com.moneygoat.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moneygoat.app.R
import com.moneygoat.app.data.entity.CategoryTotal

/**
 * CategoryTotalAdapter is a specialized ListAdapter used to display aggregated spending
 * data in the Analytics/Totals view.
 * 
 * It leverages the ListAdapter's built-in DiffUtil functionality to efficiently update 
 * only the list items that have changed, providing smooth animations when filters are applied.
 */
class CategoryTotalAdapter : ListAdapter<CategoryTotal, CategoryTotalAdapter.VH>(object : DiffUtil.ItemCallback<CategoryTotal>() {
    
    /**
     * Determines if two objects represent the same category total.
     * We use the category name as the unique identifier in this projection.
     */
    override fun areItemsTheSame(a: CategoryTotal, b: CategoryTotal): Boolean {
        return a.categoryName == b.categoryName
    }

    /**
     * Checks if the actual data (the total amount) has changed for the same category.
     */
    override fun areContentsTheSame(a: CategoryTotal, b: CategoryTotal): Boolean {
        return a == b
    }
}) {
    /**
     * ViewHolder for the category total row.
     * Holds references to the category name and the formatted currency amount.
     */
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvCatTotalName)
        val tvAmt: TextView = v.findViewById(R.id.tvCatTotalAmount)
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH {
        // Inflate the item layout defined in XML
        val view = LayoutInflater.from(p.context).inflate(R.layout.item_category_total, p, false)
        return VH(view)
    }

    override fun onBindViewHolder(h: VH, pos: Int) { 
        val item = getItem(pos)
        
        // Populate the views with data from the CategoryTotal object
        h.tvName.text = item.categoryName
        
        // Display amount with currency symbol and 2 decimal places
        h.tvAmt.text = "R %.2f".format(item.totalAmount)
    }
}

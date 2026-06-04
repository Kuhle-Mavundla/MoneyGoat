package com.moneygoat.app.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moneygoat.app.R
import com.moneygoat.app.data.entity.Expense
import java.io.File

/**
 * ExpenseAdapter is responsible for rendering a list of individual financial transactions.
 * It uses the modern ListAdapter pattern to handle data updates efficiently on a background thread.
 * 
 * Special Feature: Receipt Viewing
 * If an expense has an associated image path, the adapter displays a photo icon.
 * Clicking the item will launch a dialog showing the full-resolution receipt image.
 */
class ExpenseAdapter(private val context: Context) : ListAdapter<Expense, ExpenseAdapter.VH>(object : DiffUtil.ItemCallback<Expense>() {
    
    private val TAG = "MoneyGoat_ExpenseAdapter"

    /**
     * Checks if two objects represent the same database record using their primary key.
     */
    override fun areItemsTheSame(a: Expense, b: Expense): Boolean {
        return a.id == b.id
    }

    /**
     * Checks if the visual content of the expense has changed.
     */
    override fun areContentsTheSame(a: Expense, b: Expense): Boolean {
        return a == b
    }
}) {
    private val TAG = "MoneyGoat_ExpenseAdapter"

    /**
     * ViewHolder holds references to the UI components for a single expense row.
     */
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvDesc: TextView = v.findViewById(R.id.tvExpenseDescription)
        val tvAmt: TextView = v.findViewById(R.id.tvExpenseAmount)
        val tvDate: TextView = v.findViewById(R.id.tvExpenseDate)
        val tvTime: TextView = v.findViewById(R.id.tvExpenseTime)
        val ivPhoto: ImageView = v.findViewById(R.id.ivHasPhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        // Log the creation of new view holders (useful for monitoring recycling behavior)
        Log.v(TAG, "Creating new ViewHolder for expense item")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val expense = getItem(pos)
        
        // Data Binding
        h.tvDesc.text = expense.description
        h.tvAmt.text = "R %.2f".format(expense.amount)
        h.tvDate.text = expense.date
        h.tvTime.text = "${expense.startTime} - ${expense.endTime}"
        
        // --- Receipt Image Logic ---
        
        // Check if the file path exists and the file is actually on the disk
        if (expense.photoPath != null) {
            val imageFile = File(expense.photoPath)
            if (imageFile.exists()) {
                Log.d(TAG, "Receipt found for: ${expense.description} at ${expense.photoPath}")
                h.ivPhoto.visibility = View.VISIBLE
                
                /**
                 * Item click listener: Opens the receipt image in a popup dialog.
                 */
                h.itemView.setOnClickListener {
                    Log.i(TAG, "User opening receipt for: ${expense.description}")
                    try {
                        val bmp = BitmapFactory.decodeFile(expense.photoPath)
                        if (bmp != null) {
                            val iv = ImageView(context).apply { 
                                setImageBitmap(bmp)
                                adjustViewBounds = true
                                setPadding(16, 16, 16, 16) 
                            }
                            
                            AlertDialog.Builder(context)
                                .setTitle("Receipt for: ${expense.description}")
                                .setView(iv)
                                .setPositiveButton("Close", null)
                                .show()
                        } else {
                            Log.e(TAG, "Failed to decode bitmap from path: ${expense.photoPath}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error displaying receipt dialog", e)
                    }
                }
            } else {
                Log.w(TAG, "Photo path recorded but file not found on device for ID: ${expense.id}")
                h.ivPhoto.visibility = View.GONE
                h.itemView.setOnClickListener(null)
            }
        } else { 
            // Reset visibility and listeners for recycled views
            h.ivPhoto.visibility = View.GONE
            h.itemView.setOnClickListener(null) 
        }
    }
}

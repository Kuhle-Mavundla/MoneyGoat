package com.moneygoat.app.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
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

class ExpenseAdapter(private val context: Context) : ListAdapter<Expense, ExpenseAdapter.VH>(object : DiffUtil.ItemCallback<Expense>() {
    override fun areItemsTheSame(a: Expense, b: Expense) = a.id == b.id
    override fun areContentsTheSame(a: Expense, b: Expense) = a == b
}) {
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvDesc: TextView = v.findViewById(R.id.tvExpenseDescription)
        val tvAmt: TextView = v.findViewById(R.id.tvExpenseAmount)
        val tvDate: TextView = v.findViewById(R.id.tvExpenseDate)
        val tvTime: TextView = v.findViewById(R.id.tvExpenseTime)
        val ivPhoto: ImageView = v.findViewById(R.id.ivHasPhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val e = getItem(pos)
        h.tvDesc.text = e.description; h.tvAmt.text = "R %.2f".format(e.amount)
        h.tvDate.text = e.date; h.tvTime.text = "${e.startTime} - ${e.endTime}"
        if (e.photoPath != null && File(e.photoPath).exists()) {
            h.ivPhoto.visibility = View.VISIBLE
            h.itemView.setOnClickListener {
                val bmp = BitmapFactory.decodeFile(e.photoPath)
                if (bmp != null) {
                    val iv = ImageView(context).apply { setImageBitmap(bmp); adjustViewBounds = true; setPadding(16,16,16,16) }
                    AlertDialog.Builder(context).setTitle("Receipt: ${e.description}").setView(iv).setPositiveButton("Close", null).show()
                }
            }
        } else { h.ivPhoto.visibility = View.GONE; h.itemView.setOnClickListener(null) }
    }
}

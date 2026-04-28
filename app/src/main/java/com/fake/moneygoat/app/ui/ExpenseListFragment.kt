package com.moneygoat.app.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moneygoat.app.R
import com.moneygoat.app.adapter.ExpenseAdapter
import com.moneygoat.app.viewmodel.ExpenseViewModel
import java.util.Calendar

class ExpenseListFragment : Fragment() {
    private lateinit var expenseVM: ExpenseViewModel
    private lateinit var adapter: ExpenseAdapter
    private var startDate = ""; private var endDate = ""

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View? = inflater.inflate(R.layout.fragment_expense_list, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = (requireActivity() as MainActivity).userId
        expenseVM = ViewModelProvider(this)[ExpenseViewModel::class.java]
        adapter = ExpenseAdapter(requireContext())

        val btnStart = view.findViewById<Button>(R.id.btnFilterStart)
        val btnEnd = view.findViewById<Button>(R.id.btnFilterEnd)
        val btnApply = view.findViewById<Button>(R.id.btnApplyFilter)
        val rv = view.findViewById<RecyclerView>(R.id.rvExpenses)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyList)

        rv.layoutManager = LinearLayoutManager(requireContext()); rv.adapter = adapter
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        startDate = "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, 1)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        endDate = "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH))
        btnStart.text = startDate; btnEnd.text = endDate

        btnStart.setOnClickListener { val c = Calendar.getInstance(); DatePickerDialog(requireContext(), { _, y, m, d -> startDate = "%04d-%02d-%02d".format(y,m+1,d); btnStart.text = startDate }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() }
        btnEnd.setOnClickListener { val c = Calendar.getInstance(); DatePickerDialog(requireContext(), { _, y, m, d -> endDate = "%04d-%02d-%02d".format(y,m+1,d); btnEnd.text = endDate }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() }
        btnApply.setOnClickListener { loadData(userId, tvEmpty) }
        loadData(userId, tvEmpty)
    }

    private fun loadData(userId: Long, tvEmpty: TextView) {
        expenseVM.setDateFilter(userId, startDate, endDate)
        expenseVM.filteredExpenses.observe(viewLifecycleOwner) { expenses ->
            adapter.submitList(expenses)
            tvEmpty.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}

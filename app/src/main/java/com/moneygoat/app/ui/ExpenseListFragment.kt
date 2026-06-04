package com.moneygoat.app.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
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

/**
 * ExpenseListFragment displays a detailed, scrollable history of user transactions.
 * 
 * It includes a temporal filtering system that allows users to audit their spending
 * over specific periods. This is particularly useful for generating weekly or 
 * monthly reports and searching for specific past transactions.
 */
class ExpenseListFragment : Fragment() {
    private val TAG = "MoneyGoat_ExpenseList"
    
    private lateinit var expenseVM: ExpenseViewModel
    private lateinit var adapter: ExpenseAdapter
    
    // Date range state for filtering results
    private var startDate = ""
    private var endDate = ""

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View? {
        Log.d(TAG, "Inflating ExpenseListFragment layout")
        return inflater.inflate(R.layout.fragment_expense_list, c, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Extract the active userId from the parent activity
        val userId = (requireActivity() as MainActivity).userId
        Log.i(TAG, "Initializing Expense List for User ID: $userId")
        
        expenseVM = ViewModelProvider(this)[ExpenseViewModel::class.java]
        adapter = ExpenseAdapter(requireContext())

        // UI Component Binding
        val btnStart = view.findViewById<Button>(R.id.btnFilterStart)
        val btnEnd = view.findViewById<Button>(R.id.btnFilterEnd)
        val btnApply = view.findViewById<Button>(R.id.btnApplyFilter)
        val rv = view.findViewById<RecyclerView>(R.id.rvExpenses)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyList)

        // Setup RecyclerView with a linear vertical layout
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
        Log.v(TAG, "RecyclerView and Adapter configured")
        
        // --- Step 1: Default Date Range Setup ---
        // By default, we show expenses for the current month.
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        
        // Format: YYYY-MM-01
        startDate = "%04d-%02d-%02d".format(year, month, 1)
        
        // Format: YYYY-MM-[Last Day]
        val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        endDate = "%04d-%02d-%02d".format(year, month, lastDay)
        
        btnStart.text = startDate
        btnEnd.text = endDate
        Log.d(TAG, "Default filter period: $startDate to $endDate")

        // --- Step 2: Input Event Listeners ---

        // Opens a DatePickerDialog to select the beginning of the audit period
        btnStart.setOnClickListener { 
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d -> 
                startDate = "%04d-%02d-%02d".format(y, m + 1, d)
                btnStart.text = startDate 
                Log.v(TAG, "Start date filter updated: $startDate")
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() 
        }
        
        // Opens a DatePickerDialog to select the end of the audit period
        btnEnd.setOnClickListener { 
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d -> 
                endDate = "%04d-%02d-%02d".format(y, m + 1, d)
                btnEnd.text = endDate 
                Log.v(TAG, "End date filter updated: $endDate")
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() 
        }
        
        // Triggers the data reload based on current button values
        btnApply.setOnClickListener { 
            Log.i(TAG, "Applying date filter: $startDate to $endDate")
            loadData(userId, tvEmpty) 
        }
        
        // Initial data load when the fragment is first displayed
        loadData(userId, tvEmpty)
    }

    /**
     * Communicates the filtering parameters to the ViewModel.
     * Observes the resulting LiveData stream and updates the RecyclerView adapter.
     * 
     * @param userId The current user's ID.
     * @param tvEmpty Reference to the "No data" placeholder text view.
     */
    private fun loadData(userId: Long, tvEmpty: TextView) {
        Log.d(TAG, "Requesting filtered expenses from ViewModel")
        expenseVM.setDateFilter(userId, startDate, endDate)
        
        // Observe changes to the filtered list
        expenseVM.filteredExpenses.observe(viewLifecycleOwner) { expenses ->
            Log.d(TAG, "Expenses retrieved. Count: ${expenses.size}")
            
            // Update the adapter using DiffUtil (handled inside submitList)
            adapter.submitList(expenses)
            
            // Manage UI state based on whether data exists
            if (expenses.isEmpty()) {
                Log.v(TAG, "Showing empty list state")
                tvEmpty.visibility = View.VISIBLE
            } else {
                tvEmpty.visibility = View.GONE
            }
        }
    }
}

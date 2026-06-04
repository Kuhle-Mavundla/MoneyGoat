package com.moneygoat.app.ui

import android.app.DatePickerDialog
import android.graphics.Color
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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.moneygoat.app.R
import com.moneygoat.app.adapter.CategoryTotalAdapter
import com.moneygoat.app.viewmodel.ExpenseViewModel
import com.moneygoat.app.viewmodel.GoalViewModel
import java.util.Calendar

/**
 * CategoryTotalsFragment provides a visual and tabular breakdown of spending.
 * 
 * Features:
 * 1. PieChart: Visualizes the percentage distribution of expenses across categories.
 * 2. BarChart: Compares total actual spending against user-defined minimum and maximum budget goals.
 * 3. RecyclerView: Displays a detailed list of total amounts spent per category.
 * 4. Temporal Filtering: Allows users to audit specific date ranges.
 */
class CategoryTotalsFragment : Fragment() {
    private val TAG = "MoneyGoat_Analytics"
    
    private lateinit var expenseVM: ExpenseViewModel
    private lateinit var goalVM: GoalViewModel
    private lateinit var adapter: CategoryTotalAdapter
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    
    // Filtering state
    private var startDate = ""
    private var endDate = ""
    private var currentMonth = 0
    private var currentYear = 0

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View? {
        Log.d(TAG, "Inflating CategoryTotalsFragment")
        return inflater.inflate(R.layout.fragment_category_totals, c, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val userId = (requireActivity() as MainActivity).userId
        Log.i(TAG, "Initializing Analytics UI for User ID: $userId")
        
        // Initialize ViewModels and Adapters
        expenseVM = ViewModelProvider(this)[ExpenseViewModel::class.java]
        goalVM = ViewModelProvider(this)[GoalViewModel::class.java]
        adapter = CategoryTotalAdapter()

        // UI Component Binding
        pieChart = view.findViewById(R.id.pieChart)
        barChart = view.findViewById(R.id.barChart)
        val btnStart = view.findViewById<Button>(R.id.btnCatFilterStart)
        val btnEnd = view.findViewById<Button>(R.id.btnCatFilterEnd)
        val btnApply = view.findViewById<Button>(R.id.btnCatApplyFilter)
        val rv = view.findViewById<RecyclerView>(R.id.rvCategoryTotals)
        
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // --- Step 1: Default Filter Initialization (Current Month) ---
        val cal = Calendar.getInstance()
        currentMonth = cal.get(Calendar.MONTH) + 1
        currentYear = cal.get(Calendar.YEAR)
        
        // Start of month: YYYY-MM-01
        startDate = "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), currentMonth, 1)
        // End of month
        val lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        endDate = "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), currentMonth, lastDay)
        
        btnStart.text = startDate
        btnEnd.text = endDate
        Log.v(TAG, "Default range set to current month: $startDate to $endDate")

        // --- Step 2: User Interaction Listeners ---

        btnStart.setOnClickListener { 
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d -> 
                startDate = "%04d-%02d-%02d".format(y, m + 1, d)
                btnStart.text = startDate 
                currentMonth = m + 1 // Keep track of month for goal comparisons
                currentYear = y
                Log.d(TAG, "Start date updated to: $startDate")
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() 
        }
        
        btnEnd.setOnClickListener { 
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d -> 
                endDate = "%04d-%02d-%02d".format(y, m + 1, d)
                btnEnd.text = endDate 
                Log.d(TAG, "End date updated to: $endDate")
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() 
        }
        
        btnApply.setOnClickListener { 
            Log.i(TAG, "Applying custom date filter: $startDate to $endDate")
            loadData(userId) 
        }
        
        // --- Step 3: Chart Configuration & Data Load ---
        setupPieChart()
        setupBarChart()
        loadData(userId)
    }

    /**
     * Configures aesthetic and behavioral properties of the PieChart.
     */
    private fun setupPieChart() {
        Log.v(TAG, "Configuring PieChart properties")
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true // "Donut" style chart
            setHoleColor(Color.TRANSPARENT)
            setCenterTextSize(14f)
            setEntryLabelColor(Color.BLACK)
            legend.isEnabled = true
            animateY(1000)
        }
    }

    /**
     * Configures axes and labels for the goal comparison BarChart.
     */
    private fun setupBarChart() {
        Log.v(TAG, "Configuring BarChart properties")
        barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            animateY(1000)
            
            xAxis.apply {
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                // Custom labels for the three bars
                valueFormatter = IndexAxisValueFormatter(listOf("Spent", "Min Goal", "Max Goal"))
            }
        }
    }

    /**
     * Triggers the data retrieval process via the ViewModel.
     * Observes three separate LiveData streams to populate the UI.
     */
    private fun loadData(userId: Long) {
        Log.d(TAG, "Requesting data update for user $userId")
        expenseVM.setDateFilter(userId, startDate, endDate)
        
        // Observe per-category spending totals
        expenseVM.categoryTotals.observe(viewLifecycleOwner) { totals ->
            Log.d(TAG, "Category totals received: ${totals.size} items")
            adapter.submitList(totals)
            view?.findViewById<TextView>(R.id.tvEmptyCat)?.visibility = if (totals.isEmpty()) View.VISIBLE else View.GONE
            
            // Map the data to PieEntries (Category Name, Amount)
            updatePieChart(totals.map { it.categoryName to it.totalAmount })
        }
        
        // Observe the grand total for the selected period
        expenseVM.totalSpent.observe(viewLifecycleOwner) { total ->
            val spent = total ?: 0.0
            Log.d(TAG, "Grand total for period: R $spent")
            view?.findViewById<TextView>(R.id.tvGrandTotal)?.text = "Total: R %.2f".format(spent)
            
            // Fetch relevant monthly goal to compare against actual spending
            goalVM.getGoal(userId, currentMonth, currentYear).observe(viewLifecycleOwner) { goal ->
                if (goal != null) {
                    Log.v(TAG, "Budget goal found for comparison: Min=${goal.minimumGoal}, Max=${goal.maximumGoal}")
                }
                updateBarChart(spent, goal?.minimumGoal ?: 0.0, goal?.maximumGoal ?: 0.0)
            }
        }
    }

    /**
     * Populates the PieChart with dynamic spending data.
     */
    private fun updatePieChart(data: List<Pair<String, Double>>) {
        Log.v(TAG, "Updating PieChart visualization")
        val entries = data.map { PieEntry(it.second.toFloat(), it.first) }
        val dataSet = PieDataSet(entries, "").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }
        
        pieChart.data = PieData(dataSet)
        pieChart.centerText = "Categorical Split"
        pieChart.invalidate() // Refresh the chart
    }

    /**
     * Renders three bars: Actual Spent, Minimum Target (Savings), and Maximum Target (Limit).
     */
    private fun updateBarChart(spent: Double, minGoal: Double, maxGoal: Double) {
        Log.v(TAG, "Updating BarChart: Spent=$spent, Min=$minGoal, Max=$maxGoal")
        val entries = ArrayList<BarEntry>().apply {
            add(BarEntry(0f, spent.toFloat()))
            add(BarEntry(1f, minGoal.toFloat()))
            add(BarEntry(2f, maxGoal.toFloat()))
        }

        val dataSet = BarDataSet(entries, "Actual vs Goals").apply {
            // Color coding: Green for actual spending, Light for targets, Red for limits
            colors = listOf(
                resources.getColor(R.color.primary_green, null),
                resources.getColor(R.color.button_light, null),
                resources.getColor(R.color.expense_red, null)
            )
            valueTextSize = 10f
        }

        barChart.data = BarData(dataSet)
        barChart.invalidate() // Refresh the chart
    }
}

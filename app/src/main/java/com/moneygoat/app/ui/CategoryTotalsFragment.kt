package com.moneygoat.app.ui

import android.app.DatePickerDialog
import android.graphics.Color
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
 * Fragment that displays aggregated spending data per category.
 * Provides a breakdown of where money was spent within a specific date range.
 * Now includes visual graphs for categorical breakdown and goal progress.
 */
class CategoryTotalsFragment : Fragment() {
    private lateinit var expenseVM: ExpenseViewModel
    private lateinit var goalVM: GoalViewModel
    private lateinit var adapter: CategoryTotalAdapter
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    
    private var startDate = ""
    private var endDate = ""
    private var currentMonth = 0
    private var currentYear = 0

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View? = 
        inflater.inflate(R.layout.fragment_category_totals, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = (requireActivity() as MainActivity).userId
        
        expenseVM = ViewModelProvider(this)[ExpenseViewModel::class.java]
        goalVM = ViewModelProvider(this)[GoalViewModel::class.java]
        adapter = CategoryTotalAdapter()

        pieChart = view.findViewById(R.id.pieChart)
        barChart = view.findViewById(R.id.barChart)
        val btnStart = view.findViewById<Button>(R.id.btnCatFilterStart)
        val btnEnd = view.findViewById<Button>(R.id.btnCatFilterEnd)
        val btnApply = view.findViewById<Button>(R.id.btnCatApplyFilter)
        val rv = view.findViewById<RecyclerView>(R.id.rvCategoryTotals)
        
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // Set initial filter to current month
        val cal = Calendar.getInstance()
        currentMonth = cal.get(Calendar.MONTH) + 1
        currentYear = cal.get(Calendar.YEAR)
        
        cal.set(Calendar.DAY_OF_MONTH, 1)
        startDate = "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, 1)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        endDate = "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        
        btnStart.text = startDate
        btnEnd.text = endDate

        btnStart.setOnClickListener { 
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d -> 
                startDate = "%04d-%02d-%02d".format(y, m + 1, d)
                btnStart.text = startDate 
                currentMonth = m + 1
                currentYear = y
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() 
        }
        
        btnEnd.setOnClickListener { 
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d -> 
                endDate = "%04d-%02d-%02d".format(y, m + 1, d)
                btnEnd.text = endDate 
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() 
        }
        
        btnApply.setOnClickListener { loadData(userId) }
        
        setupPieChart()
        setupBarChart()
        loadData(userId)
    }

    private fun setupPieChart() {
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setCenterTextSize(14f)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.legend.isEnabled = true
        pieChart.animateY(1000)
    }

    private fun setupBarChart() {
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setDrawBarShadow(false)
        barChart.setDrawValueAboveBar(true)
        barChart.animateY(1000)
        
        val xAxis = barChart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Spent", "Min Goal", "Max Goal"))
    }

    private fun loadData(userId: Long) {
        expenseVM.setDateFilter(userId, startDate, endDate)
        
        expenseVM.categoryTotals.observe(viewLifecycleOwner) { totals ->
            adapter.submitList(totals)
            view?.findViewById<TextView>(R.id.tvEmptyCat)?.visibility = if (totals.isEmpty()) View.VISIBLE else View.GONE
            updatePieChart(totals.map { it.categoryName to it.totalAmount })
        }
        
        expenseVM.totalSpent.observe(viewLifecycleOwner) { total ->
            val spent = total ?: 0.0
            view?.findViewById<TextView>(R.id.tvGrandTotal)?.text = "Total: R %.2f".format(spent)
            
            // Fetch goal for the selected period to update BarChart
            goalVM.getGoal(userId, currentMonth, currentYear).observe(viewLifecycleOwner) { goal ->
                updateBarChart(spent, goal?.minimumGoal ?: 0.0, goal?.maximumGoal ?: 0.0)
            }
        }
    }

    private fun updatePieChart(data: List<Pair<String, Double>>) {
        val entries = data.map { PieEntry(it.second.toFloat(), it.first) }
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.BLACK
        
        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.centerText = "Total Spending"
        pieChart.invalidate()
    }

    private fun updateBarChart(spent: Double, minGoal: Double, maxGoal: Double) {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, spent.toFloat()))
        entries.add(BarEntry(1f, minGoal.toFloat()))
        entries.add(BarEntry(2f, maxGoal.toFloat()))

        val dataSet = BarDataSet(entries, "Budget vs Actual")
        dataSet.colors = listOf(
            resources.getColor(R.color.primary_green, null),
            resources.getColor(R.color.button_light, null),
            resources.getColor(R.color.expense_red, null)
        )
        dataSet.valueTextSize = 10f

        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.invalidate()
    }
}

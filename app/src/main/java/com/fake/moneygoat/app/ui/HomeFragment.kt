package com.moneygoat.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.moneygoat.app.R
import com.moneygoat.app.viewmodel.ExpenseViewModel
import com.moneygoat.app.viewmodel.GoalViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Fragment representing the dashboard/home screen.
 * Displays current month's spending, budget progress, and recent expenses.
 */
class HomeFragment : Fragment() {
    private lateinit var expenseVM: ExpenseViewModel
    private lateinit var goalVM: GoalViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val act = requireActivity() as MainActivity
        val userId = act.userId
        
        // Initialize ViewModels
        expenseVM = ViewModelProvider(this)[ExpenseViewModel::class.java]
        goalVM = ViewModelProvider(this)[GoalViewModel::class.java]

        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        val tvMonth = view.findViewById<TextView>(R.id.tvMonthLabel)
        val tvSpent = view.findViewById<TextView>(R.id.tvSpentAmount)
        val tvGoalInfo = view.findViewById<TextView>(R.id.tvGoalInfo)
        val progress = view.findViewById<ProgressBar>(R.id.progressBudget)
        val tvPercent = view.findViewById<TextView>(R.id.tvProgressPercent)
        val tvRecent = view.findViewById<TextView>(R.id.tvRecentExpenses)

        tvWelcome.text = "Welcome, ${act.username}!"
        
        // Determine current month and year
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        tvMonth.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)

        // Set date filter for the entire current month
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        
        // Trigger data load in ViewModel
        expenseVM.setDateFilter(userId, startDate, endDate)

        // Observe total spending and update budget progress
        expenseVM.totalSpent.observe(viewLifecycleOwner) { total ->
            val spent = total ?: 0.0
            tvSpent.text = String.format("R %.2f", spent)
            
            // Fetch goals for this month to calculate progress percentage
            goalVM.getGoal(userId, month, year).observe(viewLifecycleOwner) { goal ->
                if (goal != null) {
                    val pct = if (goal.maximumGoal > 0) ((spent / goal.maximumGoal) * 100).toInt() else 0
                    progress.progress = pct.coerceAtMost(100)
                    tvPercent.text = "${pct.coerceAtMost(100)}%"
                    tvGoalInfo.text = "Goal: R %.2f - R %.2f".format(goal.minimumGoal, goal.maximumGoal)
                    
                    // Highlight spending in red if it exceeds the maximum goal
                    if (spent > goal.maximumGoal) {
                        tvSpent.setTextColor(resources.getColor(R.color.expense_red, null))
                    }
                } else { 
                    progress.progress = 0
                    tvPercent.text = "0%"
                    tvGoalInfo.text = "No goals set yet." 
                }
            }
        }
        
        // Observe expenses to show the 5 most recent ones
        expenseVM.filteredExpenses.observe(viewLifecycleOwner) { expenses ->
            val recent = expenses.take(5)
            tvRecent.text = if (recent.isEmpty()) "No expenses this month yet."
            else recent.joinToString("\n") { "\u2022 ${it.description} \u2014 R %.2f (${it.date})".format(it.amount) }
        }
    }
}

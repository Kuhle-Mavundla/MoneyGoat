package com.moneygoat.app.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
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
 * HomeFragment serves as the dashboard for the user.
 * It provides a high-level summary of the current month's financial status,
 * including total spending vs. budget goals and a list of recent transactions.
 */
class HomeFragment : Fragment() {
    private val TAG = "MoneyGoat_Home"
    private lateinit var expenseVM: ExpenseViewModel
    private lateinit var goalVM: GoalViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "Inflating HomeFragment layout")
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Access shared user data from the parent Activity
        val act = requireActivity() as MainActivity
        val userId = act.userId
        Log.i(TAG, "Dashboard initialized for User ID: $userId")
        
        // Initialize ViewModels to observe data changes
        expenseVM = ViewModelProvider(this)[ExpenseViewModel::class.java]
        goalVM = ViewModelProvider(this)[GoalViewModel::class.java]

        // UI Component references
        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        val tvMonth = view.findViewById<TextView>(R.id.tvMonthLabel)
        val tvSpent = view.findViewById<TextView>(R.id.tvSpentAmount)
        val tvGoalInfo = view.findViewById<TextView>(R.id.tvGoalInfo)
        val progress = view.findViewById<ProgressBar>(R.id.progressBudget)
        val tvPercent = view.findViewById<TextView>(R.id.tvProgressPercent)
        val tvRecent = view.findViewById<TextView>(R.id.tvRecentExpenses)
        val tvGoalStatus = view.findViewById<TextView>(R.id.tvGoalStatus)

        tvWelcome.text = "Welcome, ${act.username}!"
        
        // Determine the current month and year for data filtering and goal retrieval
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
        tvMonth.text = monthName
        Log.v(TAG, "Current reporting period: $monthName")

        // Calculate the first and last days of the current month for the database query
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        
        Log.d(TAG, "Applying date filter: $startDate to $endDate")
        expenseVM.setDateFilter(userId, startDate, endDate)

        /**
         * Observe the total spent amount. When this changes, we re-evaluate 
         * progress towards the monthly budget goal.
         */
        expenseVM.totalSpent.observe(viewLifecycleOwner) { total ->
            val spent = total ?: 0.0
            Log.d(TAG, "Total spent for $monthName: R $spent")
            tvSpent.text = String.format("R %.2f", spent)
            
            // Fetch goal for the current user and month
            goalVM.getGoal(userId, month, year).observe(viewLifecycleOwner) { goal ->
                if (goal != null) {
                    Log.v(TAG, "Goal found: Min=${goal.minimumGoal}, Max=${goal.maximumGoal}")
                    
                    // Calculate percentage of the maximum budget used
                    val pct = if (goal.maximumGoal > 0) ((spent / goal.maximumGoal) * 100).toInt() else 0
                    progress.progress = pct.coerceAtMost(100)
                    tvPercent.text = "${pct.coerceAtMost(100)}%"
                    tvGoalInfo.text = "Range: R %.2f - R %.2f".format(goal.minimumGoal, goal.maximumGoal)
                    
                    // Apply visual feedback based on spending vs goals
                    when {
                        spent > goal.maximumGoal -> {
                            Log.w(TAG, "User has exceeded maximum budget!")
                            tvGoalStatus.text = "Over Budget! (Limit Exceeded)"
                            tvGoalStatus.setTextColor(resources.getColor(R.color.expense_red, null))
                            progress.progressTintList = ColorStateList.valueOf(resources.getColor(R.color.expense_red, null))
                        }
                        spent >= goal.minimumGoal -> {
                            Log.i(TAG, "User is within their target spending/savings range.")
                            tvGoalStatus.text = "On Track (Within Goal Range)"
                            tvGoalStatus.setTextColor(resources.getColor(R.color.primary_green, null))
                            progress.progressTintList = ColorStateList.valueOf(resources.getColor(R.color.primary_green, null))
                        }
                        else -> {
                            Log.d(TAG, "Spending is below the minimum threshold.")
                            tvGoalStatus.text = "Below Minimum Savings Target"
                            tvGoalStatus.setTextColor(resources.getColor(R.color.text_secondary, null))
                            progress.progressTintList = ColorStateList.valueOf(resources.getColor(R.color.button_light, null))
                        }
                    }
                } else { 
                    Log.i(TAG, "No budget goal defined for $month/$year")
                    progress.progress = 0
                    tvPercent.text = "0%"
                    tvGoalInfo.text = "No goals set yet."
                    tvGoalStatus.text = "Please set your monthly goals."
                }
            }
        }
        
        /**
         * Observe filtered expenses to display the most recent activities.
         */
        expenseVM.filteredExpenses.observe(viewLifecycleOwner) { expenses ->
            Log.v(TAG, "Updating recent expenses list. Count: ${expenses.size}")
            val recent = expenses.take(5)
            tvRecent.text = if (recent.isEmpty()) {
                "No expenses this month yet."
            } else {
                recent.joinToString("\n") { 
                    "\u2022 ${it.description} \u2014 R %.2f (${it.date})" .format(it.amount) 
                }
            }
        }
    }
}

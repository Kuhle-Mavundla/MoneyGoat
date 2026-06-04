package com.moneygoat.app.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.moneygoat.app.R
import com.moneygoat.app.viewmodel.GoalViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * GoalSettingsFragment allows users to define their financial boundaries for a given month.
 * 
 * It features a dual-input system:
 * 1. SeekBars for quick, visual adjustments of budget targets.
 * 2. EditTexts for precise numeric entry.
 * 
 * Users set two key metrics:
 * - Minimum Goal: The minimum amount they aim to save or the floor of their spending.
 * - Maximum Goal: The absolute spending limit (ceiling) for the month.
 */
class GoalSettingsFragment : Fragment() {
    private val TAG = "MoneyGoat_GoalsUI"
    private lateinit var goalVM: GoalViewModel

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View? {
        Log.d(TAG, "Inflating GoalSettingsFragment")
        return inflater.inflate(R.layout.fragment_goal_settings, c, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val mainAct = requireActivity() as MainActivity
        val userId = mainAct.userId
        Log.i(TAG, "Initializing Goal Settings for user: ${mainAct.username}")
        
        goalVM = ViewModelProvider(this)[GoalViewModel::class.java]

        // UI Binding
        val tvMonth = view.findViewById<TextView>(R.id.tvGoalMonth)
        val etMin = view.findViewById<EditText>(R.id.etMinGoal)
        val etMax = view.findViewById<EditText>(R.id.etMaxGoal)
        val seekMin = view.findViewById<SeekBar>(R.id.seekBarMin)
        val seekMax = view.findViewById<SeekBar>(R.id.seekBarMax)
        val tvMinVal = view.findViewById<TextView>(R.id.tvMinValue)
        val tvMaxVal = view.findViewById<TextView>(R.id.tvMaxValue)
        val btnSave = view.findViewById<Button>(R.id.btnSaveGoal)

        // Set the reporting context to the current calendar month
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        val periodLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
        tvMonth.text = periodLabel
        Log.v(TAG, "Current goal period: $periodLabel")

        // --- SeekBar Configuration ---

        // Minimum Goal (Savings/Floor)
        seekMin.max = 50000
        seekMin.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) { 
                if (fromUser) { 
                    etMin.setText(p.toString())
                    tvMinVal.text = "R $p" 
                    Log.v(TAG, "Min SeekBar adjusted to: $p")
                } 
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Maximum Goal (Spending Limit/Ceiling)
        seekMax.max = 100000
        seekMax.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) { 
                if (fromUser) { 
                    etMax.setText(p.toString())
                    tvMaxVal.text = "R $p" 
                    Log.v(TAG, "Max SeekBar adjusted to: $p")
                } 
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        /**
         * Observe the existing goal for the current month.
         * If a goal was previously set, we populate the UI with those values.
         */
        goalVM.getGoal(userId, month, year).observe(viewLifecycleOwner) { goal ->
            if (goal != null) {
                Log.d(TAG, "Existing goal retrieved for $periodLabel. Updating UI.")
                etMin.setText(goal.minimumGoal.toInt().toString())
                etMax.setText(goal.maximumGoal.toInt().toString())
                seekMin.progress = goal.minimumGoal.toInt()
                seekMax.progress = goal.maximumGoal.toInt()
                tvMinVal.text = "R ${goal.minimumGoal.toInt()}"
                tvMaxVal.text = "R ${goal.maximumGoal.toInt()}"
            } else {
                Log.i(TAG, "No existing goal found for $periodLabel. Starting with fresh inputs.")
            }
        }

        /**
         * Save Button Logic
         * Validates numeric input and business logic (Min < Max) before persisting.
         */
        btnSave.setOnClickListener {
            val min = etMin.text.toString().trim().toDoubleOrNull()
            val max = etMax.text.toString().trim().toDoubleOrNull()
            
            Log.d(TAG, "Save goal requested: Min=$min, Max=$max")
            
            if (min == null || max == null) { 
                Log.w(TAG, "Goal save aborted: Null or invalid numeric input")
                Toast.makeText(requireContext(), "Please enter valid numeric goals", Toast.LENGTH_SHORT).show()
                return@setOnClickListener 
            }
            
            // Logic validation: Minimum target should naturally be less than the maximum limit
            if (min > max) { 
                Log.w(TAG, "Goal save aborted: Min ($min) exceeds Max ($max)")
                Toast.makeText(requireContext(), "Minimum target cannot exceed maximum limit", Toast.LENGTH_SHORT).show()
                return@setOnClickListener 
            }

            // Persistence via ViewModel (Room + Firebase)
            Log.i(TAG, "Validation passed. Persisting goals for user: ${mainAct.username}")
            goalVM.saveGoal(userId, mainAct.username, month, year, min, max)
            
            Toast.makeText(requireContext(), "Monthly goals successfully saved!", Toast.LENGTH_SHORT).show()
        }
    }
}

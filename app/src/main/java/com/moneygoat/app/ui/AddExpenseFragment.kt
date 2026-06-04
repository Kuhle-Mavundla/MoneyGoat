package com.moneygoat.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.moneygoat.app.R
import com.moneygoat.app.data.entity.Category
import com.moneygoat.app.data.entity.Expense
import com.moneygoat.app.viewmodel.CategoryViewModel
import com.moneygoat.app.viewmodel.ExpenseViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * AddExpenseFragment allows users to input a new financial transaction.
 * It features a comprehensive form including:
 * - Description and Amount
 * - Date selection via DatePickerDialog
 * - Start/End time range for the activity
 * - Category selection from a dynamic list
 * - Image capture for digital receipt archiving
 */
class AddExpenseFragment : Fragment() {
    private val TAG = "MoneyGoat_AddExpense"
    private lateinit var expenseVM: ExpenseViewModel
    private lateinit var categoryVM: CategoryViewModel

    // State variables for form input
    private var selectedDate = ""
    private var selectedStartTime = ""
    private var selectedEndTime = ""
    private var photoUri: Uri? = null
    private var photoPath: String? = null
    private var categories = listOf<Category>()

    /**
     * Handles the result of the camera permission request.
     * If granted, it proceeds to launch the system camera.
     */
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Log.i(TAG, "Camera permission granted by user")
            launchCamera()
        } else {
            Log.w(TAG, "Camera permission denied by user")
            Toast.makeText(requireContext(), "Camera permission required to scan receipts", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Handles the result of the TakePicture intent.
     * If successful, updates the UI to show the captured receipt.
     */
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            Log.d(TAG, "Photo successfully saved at: $photoPath")
            view?.findViewById<ImageView>(R.id.ivPhoto)?.apply {
                setImageURI(photoUri)
                visibility = View.VISIBLE
            }
        } else {
            Log.v(TAG, "Photo capture cancelled or failed")
            photoPath = null // Reset path if capture failed
        }
    }

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View? {
        Log.d(TAG, "Inflating AddExpenseFragment")
        return inflater.inflate(R.layout.fragment_add_expense, c, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainAct = requireActivity() as MainActivity
        val userId = mainAct.userId

        // Initialize ViewModels
        expenseVM = ViewModelProvider(this)[ExpenseViewModel::class.java]
        categoryVM = ViewModelProvider(this)[CategoryViewModel::class.java]

        // UI references
        val etDesc = view.findViewById<EditText>(R.id.etDescription)
        val etAmt = view.findViewById<EditText>(R.id.etAmount)
        val btnDate = view.findViewById<Button>(R.id.btnSelectDate)
        val btnStart = view.findViewById<Button>(R.id.btnStartTime)
        val btnEnd = view.findViewById<Button>(R.id.btnEndTime)
        val spinner = view.findViewById<Spinner>(R.id.spinnerCategory)
        val btnPhoto = view.findViewById<Button>(R.id.btnTakePhoto)
        val btnSave = view.findViewById<Button>(R.id.btnSaveExpense)

        // Populate Category Spinner: Only shows categories created by this user
        categoryVM.getCategories(userId).observe(viewLifecycleOwner) { cats ->
            Log.v(TAG, "Updating category spinner with ${cats.size} categories")
            categories = cats
            val names = if (cats.isEmpty()) listOf("No categories - create one first") else cats.map { it.name }
            spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }

        // Date Picker: Ensures standardized yyyy-MM-dd format for database consistency
        btnDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                selectedDate = "%04d-%02d-%02d".format(y, m + 1, d)
                btnDate.text = selectedDate
                Log.d(TAG, "Date selected: $selectedDate")
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
        
        // Start Time Picker
        btnStart.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, h, m ->
                selectedStartTime = "%02d:%02d".format(h, m)
                btnStart.text = selectedStartTime
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }
        
        // End Time Picker
        btnEnd.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, h, m ->
                selectedEndTime = "%02d:%02d".format(h, m)
                btnEnd.text = selectedEndTime
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }
        
        // Receipt Photo logic
        btnPhoto.setOnClickListener {
            Log.i(TAG, "Requesting receipt photo")
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                requestCameraPermission.launch(android.Manifest.permission.CAMERA)
            }
        }
        
        // Save Logic: Performs data validation before database insertion
        btnSave.setOnClickListener {
            val desc = etDesc.text.toString().trim()
            val amtStr = etAmt.text.toString().trim()

            Log.d(TAG, "Attempting to save expense: '$desc'")

            // Validate required fields
            if (desc.isEmpty() || amtStr.isEmpty() || selectedDate.isEmpty() || selectedStartTime.isEmpty() || selectedEndTime.isEmpty()) {
                Log.w(TAG, "Save failed: Incomplete form data")
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ensure categories exist
            if (categories.isEmpty()) {
                Log.w(TAG, "Save failed: No categories available")
                Toast.makeText(requireContext(), "Create a category in Category Manager first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate numeric amount
            val amt = amtStr.toDoubleOrNull()
            if (amt == null || amt <= 0) {
                Log.w(TAG, "Save failed: Invalid amount '$amtStr'")
                Toast.makeText(requireContext(), "Enter a valid positive amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val expense = Expense(
                date = selectedDate,
                startTime = selectedStartTime,
                endTime = selectedEndTime,
                description = desc,
                amount = amt,
                categoryId = categories[spinner.selectedItemPosition].id,
                userId = userId,
                photoPath = photoPath
            )

            Log.i(TAG, "Validation passed. Saving expense to Room and Firebase.")
            expenseVM.addExpense(expense, mainAct.username)
            
            Toast.makeText(requireContext(), "Expense successfully saved!", Toast.LENGTH_SHORT).show()
            resetForm(etDesc, etAmt, btnDate, btnStart, btnEnd, view.findViewById(R.id.ivPhoto))
        }
    }

    /**
     * Clears all input fields to prepare for the next entry.
     */
    private fun resetForm(desc: EditText, amt: EditText, date: Button, start: Button, end: Button, image: ImageView) {
        Log.v(TAG, "Resetting AddExpense form fields")
        desc.text.clear()
        amt.text.clear()
        selectedDate = ""
        selectedStartTime = ""
        selectedEndTime = ""
        photoPath = null
        photoUri = null
        date.text = "Select Date"
        start.text = "Start Time"
        end.text = "End Time"
        image.visibility = View.GONE
    }

    /**
     * Generates a temporary file for the image and launches the system camera app.
     * Uses FileProvider for secure URI sharing with the camera app.
     */
    private fun launchCamera() {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile("RECEIPT_${timeStamp}_", ".jpg", storageDir)

            photoPath = file.absolutePath
            photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)

            Log.d(TAG, "Launching camera. Temp file created at: $photoPath")
            takePicture.launch(photoUri!!)
        } catch (e: Exception) {
            Log.e(TAG, "Error while preparing camera: ${e.message}", e)
            Toast.makeText(requireContext(), "Could not open camera", Toast.LENGTH_SHORT).show()
        }
    }
}

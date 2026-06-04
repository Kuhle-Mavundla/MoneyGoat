package com.moneygoat.app.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moneygoat.app.R
import com.moneygoat.app.data.entity.Category
import com.moneygoat.app.viewmodel.CategoryViewModel

/**
 * CategoryManagerFragment provides the UI for users to customize their spending categories.
 *
 * Features:
 * - List existing categories in a RecyclerView.
 * - Add new custom categories (e.g., "Subscription", "Pet Care").
 * - Delete categories that are no longer needed.
 *
 * This helps users personalize the app to match their specific spending habits.
 */
class CategoryManagerFragment : Fragment() {
    private val TAG = "MoneyGoat_CatManager"
    private lateinit var categoryVM: CategoryViewModel

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View? {
        Log.d(TAG, "Inflating CategoryManagerFragment")
        return inflater.inflate(R.layout.fragment_category_manager, c, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve session data from activity
        val mainAct = requireActivity() as MainActivity
        val userId = mainAct.userId
        val username = mainAct.username

        categoryVM = ViewModelProvider(this)[CategoryViewModel::class.java]

        val etName = view.findViewById<EditText>(R.id.etCategoryName)
        val btnAdd = view.findViewById<Button>(R.id.btnAddCategory)
        val rv = view.findViewById<RecyclerView>(R.id.rvCategories)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyCategories)
        
        // Initialize RecyclerView with a linear layout (vertical list)
        rv.layoutManager = LinearLayoutManager(requireContext())
        Log.v(TAG, "RecyclerView initialized with LinearLayoutManager")

        /**
         * Add Category Button Logic
         * Validates the input and calls the ViewModel to persist the new category.
         */
        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            Log.d(TAG, "Add category requested: '$name'")

            if (name.isEmpty()) { 
                Log.w(TAG, "Category add failed: Input is empty")
                Toast.makeText(requireContext(), "Enter a category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener 
            }

            // ViewModel handles local DB insertion and Firebase sync
            categoryVM.addCategory(name, userId, username)

            Log.i(TAG, "Category '$name' passed to ViewModel for processing")
            etName.text.clear()
            Toast.makeText(requireContext(), "Category added!", Toast.LENGTH_SHORT).show()
        }

        /**
         * Observe the categories LiveData.
         * The UI will automatically refresh whenever a category is added or deleted.
         */
        categoryVM.getCategories(userId).observe(viewLifecycleOwner) { cats ->
            Log.d(TAG, "Category list updated. Count: ${cats.size}")

            // Toggle visibility of the "No Categories" placeholder message
            if (cats.isEmpty()) {
                Log.v(TAG, "Displaying empty state message")
                tvEmpty.visibility = View.VISIBLE
            } else {
                tvEmpty.visibility = View.GONE
            }

            // Set up the adapter with a deletion callback
            rv.adapter = CatAdapter(cats) { category ->
                Log.i(TAG, "User requested deletion of category: ${category.name}")
                categoryVM.deleteCategory(category)
            }
        }
    }

    /**
     * Inner adapter class for displaying Category entities.
     * Includes a ViewHolder and handles data binding to the item layout.
     */
    inner class CatAdapter(
        private val items: List<Category>,
        private val onDel: (Category) -> Unit
    ) : RecyclerView.Adapter<CatAdapter.VH>() {
        
        /**
         * ViewHolder holds references to the views for each list item.
         */
        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvName: TextView = v.findViewById(R.id.tvCatItemName)
            val btnDel: Button = v.findViewById(R.id.btnDeleteCategory) 
        }

        override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH {
            val view = LayoutInflater.from(p.context).inflate(R.layout.item_category, p, false)
            return VH(view)
        }

        override fun onBindViewHolder(h: VH, pos: Int) { 
            val category = items[pos]
            h.tvName.text = category.name

            // Set listener for the delete button in each row
            h.btnDel.setOnClickListener {
                onDel(category)
            }
        }

        override fun getItemCount() = items.size
    }
}

package com.markus.gym_notes

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.markus.gym_notes.databinding.ActivityMainBinding
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var ListViewCategories: ListView
    private lateinit var categories: ArrayList<String>
    private lateinit var fab: FloatingActionButton
    private lateinit var itemAdapter: ArrayAdapter<String>
    private val db by lazy { AppDatabase.getInstance(applicationContext) }

    //private lateinit var categoryAdapter:
    private var currentCategories: List<Category> = emptyList()

    private fun addNewExercise(
        categoryId: Int,
        name: String,
        weight: Double,
        description: String,
        weightHistory: ArrayList<Double>
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            val newExercise = Exercise(
                categoryId = categoryId,
                name = name,
                weight = weight,
                description = description,
                weightHistory = weightHistory
            )
            db.exerciseDao().insert(newExercise)
            Log.d("MainActivity", "New exercise added: $name")
        }
    }

    private fun addNewCategory(categoryName: String, categoryDescription: String) {
        // lifecycleScope runs this in a coroutine
        // Dispatchers.IO is the thread pool optimized for disk/network operations
        lifecycleScope.launch(Dispatchers.IO) {
            val newCategory = Category(name = categoryName, description = categoryDescription)
            db.categoryDao().insert(newCategory)
            // You can't update UI from here, but you can log
            Log.d("MainActivity", "Category deleted: $categoryName")
        }
    }

    private fun rmCategory(category: Category) {
        // lifecycleScope runs this in a coroutine
        // Dispatchers.IO is the thread pool optimized for disk/network operations
        lifecycleScope.launch(Dispatchers.IO) {
            db.categoryDao().delete(category)
            // You can't update UI from here, but you can log
            Log.d("MainActivity", "New category added: ${category.name}")
            launch(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "'${category.name}' removed", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun getCategories() {
        lifecycleScope.launch(Dispatchers.IO) {
            db.categoryDao().getCategories()
        }
    }

    private fun showAddCategoryDialog() {
        // 1. Create an alert dialog builder
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add new category")

        // 2. Set up the input field (EditText)
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        input.hint = "Write category here"
        input.setTextAppearance(R.style.DialogEditTextStyle)

        val whiteColorStateList =
            ColorStateList.valueOf(Color.WHITE) // Besser lesbar auf weißem Hintergrund
        input.backgroundTintList = whiteColorStateList
        input.setTextColor(Color.WHITE) // Textfarbe setzen
        input.setHintTextColor(Color.LTGRAY) // Hinweis-Textfarbe setzen

        // Set padding for the EditText
        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // Add horizontal and vertical margins
        params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        input.layoutParams = params
        container.addView(input)

        builder.setView(container) // Set the container view with the EditText

        // 3. Set up the dialog buttons
        builder.setPositiveButton("Add") { dialog, _ ->
            val categoryName = input.text.toString().trim()

            // Add to the list ONLY if the text is not empty
            if (!categoryName.isNullOrBlank()) {
                addNewCategory(categoryName, categoryDescription = input.text.toString().trim())
                Toast.makeText(this, "'$categoryName' added", Toast.LENGTH_SHORT).show()

                dialog.dismiss()
            } else {
                Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        // 4. Create and show the dialog
        builder.show()
    }

    private fun showRmCategoryDialog() {
        // 1. Create an alert dialog builder
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Remove category")

        val categoryNames = currentCategories.map { it.name }.toTypedArray()

        // 3. Set the list items and handle the click event for deletion
        builder.setItems(categoryNames) { dialog, which ->
            // 'which' is the index of the item that was clicked
            val categoryToRemove = currentCategories[which]

            rmCategory(categoryToRemove)

            // Optionally, show a confirmation message
            Toast.makeText(this, "'$categoryToRemove' removed", Toast.LENGTH_SHORT).show()
        }
        // 4. Add a "Cancel" button
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        // 5. Create and show the dialog
        builder.create().show()
    }

    private fun showSelectCategoryDialog() {
        // 1. Create a variable to store the checked item's position
        var selectedCategoryIndex = 0

        // 2. Create the array of names to display
        val categoryNames = currentCategories.map{it.name}.toTypedArray()

        // 3. Build the dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose a category")

        // 4. Set the items. When clicked, it updates 'selectedCategoryIndex'
        builder.setSingleChoiceItems(categoryNames, 0) { dialog, which ->
            // 'which' is the new position the user clicked
            selectedCategoryIndex = which
        }

        // 5. Set the "OK" button to *use* the selected index
        builder.setPositiveButton("OK") { dialog, _ ->
            // Use the index to get the full Category object
            val selectedCategory = currentCategories[selectedCategoryIndex]

            // NOW you have the ID!
            val categoryId = selectedCategory.id

            // You can now use this ID, for example,
            // to show an "Add Exercise" dialog
            Log.d("MainActivity", "The selected Category ID is: $categoryId")

            showAddExerciseDialog(categoryId)
        }

        // 6. Add a "Cancel" button
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        // 7. Show the dialog
        builder.create().show()
    }

    private fun createStyledEditText(hint: String, isNumber: Boolean = false): EditText {
        val editText = EditText(this)
        editText.hint = hint

        // Set Input Type (Text vs Number)
        if (isNumber) {
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }

        // Styling
        editText.setTextAppearance(R.style.DialogEditTextStyle)
        editText.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
        editText.setTextColor(Color.WHITE)
        editText.setHintTextColor(Color.LTGRAY)

        // Layout Params (Margins)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = 30 // Space between inputs
        editText.layoutParams = params

        return editText
    }

    private fun showAddExerciseDialog(categoryId: Int) {
        // 1. Create an alert dialog builder
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add new exercise")

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL

        // Give it some padding
        val containerParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // Convert dimen to pixels
        val marginSize = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        containerParams.setMargins(marginSize, 0, marginSize, 0) // Add margins
        container.layoutParams = containerParams
        container.setPadding(marginSize, marginSize, marginSize, marginSize) // Add internal padding

        val inputName = createStyledEditText("Exercise name", false)
        val inputWeight = createStyledEditText("Weight", true)
        val inputDescription = createStyledEditText("Description", false)

        container.addView(inputName)
        container.addView(inputWeight)
        container.addView(inputDescription)

        builder.setView(container) // Set the container view with the EditText

        // 3. Set up the dialog buttons
        builder.setPositiveButton("Add") { dialog, _ ->

            val exerciseName = inputName.text.toString().trim()
            val weightText = inputWeight.text.toString().trim()
            val exerciseDes = inputDescription.text.toString().trim()

            // Add to the list ONLY if the text is not empty
            if (!exerciseName.isNullOrBlank()) {

                val weight = weightText.toDoubleOrNull() ?: 0.0
                val weightHistory = arrayListOf<Double>()

                addNewExercise(categoryId, exerciseName, weight, exerciseDes, weightHistory)

                Toast.makeText(this, "'$exerciseName' added", Toast.LENGTH_SHORT).show()

                dialog.dismiss()
            } else {
                Toast.makeText(this, "Exercise name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        // 4. Create and show the dialog
        builder.show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ListViewCategories = findViewById(R.id.Arms)
        fab = findViewById(R.id.fab)

        categories = ArrayList()

        itemAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        ListViewCategories.adapter = itemAdapter

        db.categoryDao().getCategories().observe(this) { updatedCategories ->
            currentCategories = updatedCategories

            val categoryNames = updatedCategories.map { it.name }

            categories.clear()
            categories.addAll(categoryNames)
            itemAdapter.notifyDataSetChanged()

            Log.d("MainActivity", "UI updated with ${updatedCategories.size} items from Database.")
        }

        ListViewCategories.setOnItemLongClickListener { arg0, arg1, pos, id ->

            val builder = AlertDialog.Builder(this)
            val inflater = LayoutInflater.from(this)
            val dialogView = inflater.inflate(R.layout.dialog_layout, null)
            val dialogTextView = dialogView.findViewById<TextView>(R.id.dialog_text)
            val item = categories[pos]
            val confirmationMessage =
                String.format(resources.getString(R.string.dialog_remove_confirmation), item)
            dialogTextView.text = confirmationMessage

            val textColor = ContextCompat.getColor(
                this,
                R.color.dialog_text_color
            )
            dialogTextView.setTextColor(textColor)

            builder.setView(dialogView)

            builder.setPositiveButton("YES") { dialog, which ->
                val categoryToRemove = currentCategories.find { it.name == item }
                if (categoryToRemove != null) {
                    rmCategory(categoryToRemove)
                }
            }
            builder.setNegativeButton("NO") { dialog, which ->
            }
            builder.show()
            true
        }

        fab.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Exercise")

            // 1. Container erstellen (LinearLayout vertikal)
            val container = LinearLayout(this)
            container.orientation = LinearLayout.VERTICAL
            // Padding für den gesamten Container statt Margins für einzelne Elemente
            val paddingDp = 16 // Entspricht ca. 48px bei vielen Dichten
            val paddingPx = (paddingDp * resources.displayMetrics.density).toInt()
            container.setPadding(paddingPx, paddingPx / 2, paddingPx, paddingPx / 2)


            // 2. Spinner (Dropdown) erstellen und konfigurieren
            val spinner = Spinner(this)
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            // Layout-Parameter für den Spinner
            val spinnerParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            spinnerParams.bottomMargin = paddingPx / 2 // Abstand zum EditText hinzufügen
            spinner.layoutParams = spinnerParams

            // Spinner zum Container hinzufügen (ZUERST)
            container.addView(spinner)

            // 3. EditText erstellen und konfigurieren (wie zuvor, aber ohne Margin Params hier)
            val input = EditText(this)
            input.hint = "Write Exercise here"
            input.inputType = InputType.TYPE_CLASS_TEXT
            // Stelle sicher, dass R.style.DialogEditTextStyle existiert oder entferne diese Zeile
            input.setTextAppearance(R.style.DialogEditTextStyle)

            val whiteColorStateList =
                ColorStateList.valueOf(Color.WHITE) // Besser lesbar auf weißem Hintergrund
            input.backgroundTintList = whiteColorStateList
            input.setTextColor(Color.WHITE) // Textfarbe setzen
            input.setHintTextColor(Color.LTGRAY) // Hinweis-Textfarbe setzen

            // Layout-Parameter für das EditText (nimmt den Rest des Platzes ein)
            val inputParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            input.layoutParams = inputParams

            // EditText zum Container hinzufügen (DANACH)
            container.addView(input)

            // 4. Container dem Builder übergeben
            builder.setView(container)

            // 5. Buttons konfigurieren
            builder.setPositiveButton("ADD") { dialog, which ->
                showSelectCategoryDialog()
            }

            builder.setNegativeButton("CANCEL") { dialog, which ->
                dialog.cancel() // Dialog schließen
            }

            builder.show()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                Toast.makeText(this, "Settings", Toast.LENGTH_LONG).show()
                true
            }

            R.id.addCategory -> {
                showAddCategoryDialog()
                true
            }

            R.id.rmCategory -> {
                showRmCategoryDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var lvArms: ListView
    private lateinit var ArmsList: ArrayList<String>
    private lateinit var fab: FloatingActionButton
    private lateinit var itemAdapter: ArrayAdapter<String>
    private val categories = mutableListOf<Category>()


    private fun showAddCategoryDialog() {
        // 1. Create an alert dialog builder
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add new category")

        // 2. Set up the input field (EditText)
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        input.hint = "Write category here"
        input.setTextAppearance(R.style.DialogEditTextStyle)

        val whiteColorStateList = ColorStateList.valueOf(Color.WHITE) // Besser lesbar auf weißem Hintergrund
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
            val newCategory = Category(name=input.text.toString().trim(), description = "d")
            //newCategory.name = input.text.toString().trim()

            // Add to the list ONLY if the text is not empty
            if (!newCategory.name.isNullOrBlank()) {
                categories.add(newCategory)
                categories.sortBy{it.name}
                Toast.makeText(this, "'$newCategory' added", Toast.LENGTH_SHORT).show()

                // IMPORTANT: Notify your adapter that the data has changed
                itemAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Region name cannot be empty", Toast.LENGTH_SHORT).show()
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

        // 2. Convert your list to an array to display in the dialog
        val listItems = ArmsList.toTypedArray()

        // 3. Set the list items and handle the click event for deletion
        builder.setItems(listItems) { dialog, which ->
            // 'which' is the index of the item that was clicked
            val itemToRemove = ArmsList[which]

            // Remove the item from your source list
            ArmsList.removeAt(which)

            // IMPORTANT: You must notify your adapter that the data has changed
            // so the UI can update to reflect the deletion.
            // For example, if you are using a RecyclerView:
            // yourAdapter.notifyItemRemoved(which)

            // Optionally, show a confirmation message
            Toast.makeText(this, "'$itemToRemove' removed", Toast.LENGTH_SHORT).show()
        }

        // 4. Add a "Cancel" button
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        // 5. Create and show the dialog
        builder.show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        lvArms = findViewById(R.id.Arms)
        fab = findViewById(R.id.fab)

        ArmsList = ArrayList()

        itemAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArmsList)
        lvArms.adapter = itemAdapter

        lvArms.setOnItemLongClickListener( {arg0, arg1, pos, id ->

            val builder = AlertDialog.Builder(this)
            val inflater = LayoutInflater.from(this)
            val dialogView = inflater.inflate(R.layout.dialog_layout, null)
            val dialogTextView = dialogView.findViewById<TextView>(R.id.dialog_text)
            val item = ArmsList[pos]
            val confirmationMessage = String.format(resources.getString(R.string.dialog_remove_confirmation), item)
            dialogTextView.text = confirmationMessage

            val textColor = ContextCompat.getColor(
                this,
                R.color.dialog_text_color
            )
            dialogTextView.setTextColor(textColor)

            builder.setView(dialogView)

            builder.setPositiveButton("YES") { dialog, which ->
                ArmsList.removeAt(pos)
                itemAdapter.notifyDataSetChanged()
            }

            builder.setNegativeButton("NO") {dialog, which ->

            }

            builder.show()
            true
        })

        fab.setOnClickListener{
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
            // --- WICHTIG: Ersetze dies durch deine echten Kategorien ---
            val categories = categories
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

            val whiteColorStateList = ColorStateList.valueOf(Color.WHITE) // Besser lesbar auf weißem Hintergrund
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
                val selectedCategory = spinner.selectedItem.toString()
                val exerciseName = input.text.toString()
                // --- Entscheide, wie du die Daten verwenden willst ---
                // Beispiel 2: Kategorie und Namen kombiniert hinzufügen
                ArmsList.add("$selectedCategory: $exerciseName")

                ArmsList.sort()

                itemAdapter.notifyDataSetChanged()
                // Beispiel 3: Separat übergeben (wenn ArmsList das unterstützt)
                // ArmsList.add(category = selectedCategory, name = exerciseName)

                println("Selected: $selectedCategory, Input: $exerciseName") // Zum Debuggen
            }

            builder.setNegativeButton("CANCEL") { dialog, which ->
                dialog.cancel() // Dialog schließen
            }

            builder.show()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater : MenuInflater = menuInflater
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
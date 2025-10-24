package com.markus.gym_notes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_table",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE // Optional: Deletes exercises if their category is deleted
        )
    ]
)

data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val categoryId: Int, // Stores the ID of the category, not the object itself
    val name: String,
    val weight: Double,
    val description: String,
    val weightHistory: ArrayList<Double>
)
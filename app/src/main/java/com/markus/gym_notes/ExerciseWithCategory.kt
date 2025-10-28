package com.markus.gym_notes

import androidx.room.Embedded
import androidx.room.Relation

// This class is a "container" to hold the combined query result.
// It is NOT a table in the database.
data class ExerciseWithCategory(

    @Embedded
    val exercise: Exercise,

    @Relation(
        parentColumn = "categoryId", // The ID from the Exercise class
        entityColumn = "id"          // The ID from the Category class
    )
    val category: Category
)
package com.markus.gym_notes

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ExerciseDao {

    // This is the new function to add an exercise
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(exercise: Exercise)

    /**
     * This query gets all exercises and automatically fetches their
     * related category.
     */
    @Transaction // Ensures this runs as a single, safe operation
    @Query("SELECT * FROM exercise_table")
    fun getExercisesWithCategories(): LiveData<List<ExerciseWithCategory>>

    /**
     * This query gets a single exercise by its ID and also fetches
     * its related category.
     */
    @Transaction
    @Query("SELECT * FROM exercise_table WHERE id = :exerciseId")
    fun getExerciseWithCategory(exerciseId: Int): LiveData<ExerciseWithCategory>

    // ... other functions like insert, update, delete ...
}
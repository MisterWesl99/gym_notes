package com.markus.gym_notes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface CategoryDao {

    // Inserts a category. If a category with the same name exists, it will be ignored.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category)
}
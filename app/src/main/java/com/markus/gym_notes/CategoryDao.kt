package com.markus.gym_notes

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CategoryDao {

    // Inserts a category. If a category with the same name exists, it will be ignored.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category)

    @Delete()
    suspend fun delete(category: Category)

    @Transaction
    @Query("SELECT * FROM category_table")
    fun getCategories(): LiveData<List<Category>>
}
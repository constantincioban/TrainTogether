package com.konovus.traintogether.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.konovus.traintogether.data.local.entities.PersonalRecordDB
import com.konovus.traintogether.data.utils.GymExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalRecordDao {
    @Query("SELECT * FROM personal_records ORDER BY date DESC")
    fun getAllPersonalRecords(): Flow<List<PersonalRecordDB>>

    @Query("SELECT * FROM personal_records WHERE id = :recordId")
    suspend fun getPersonalRecordById(recordId: Int): PersonalRecordDB?

    @Query("SELECT * FROM personal_records WHERE exercise = :exercise ORDER BY date DESC")
    fun getRecordsByExercise(exercise: GymExercise): Flow<List<PersonalRecordDB>>

    @Query("SELECT * FROM personal_records WHERE exercise = :exercise ORDER BY weight DESC LIMIT 1")
    suspend fun getBestRecordForExercise(exercise: GymExercise): PersonalRecordDB?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonalRecord(record: PersonalRecordDB): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonalRecords(records: List<PersonalRecordDB>)

    @Update
    suspend fun updatePersonalRecord(record: PersonalRecordDB)

    @Delete
    suspend fun deletePersonalRecord(record: PersonalRecordDB)

    @Query("DELETE FROM personal_records WHERE id = :recordId")
    suspend fun deletePersonalRecordById(recordId: Int)

    @Query("DELETE FROM personal_records")
    suspend fun deleteAllPersonalRecords()

    @Query("SELECT COUNT(*) FROM personal_records")
    suspend fun getPersonalRecordCount(): Int
}

package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ManhwaDao {
    @Query("SELECT * FROM manhwas ORDER BY id DESC")
    fun getAllManhwas(): Flow<List<ManhwaEntity>>

    @Query("SELECT * FROM manhwas WHERE id = :id LIMIT 1")
    suspend fun getManhwaById(id: Long): ManhwaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManhwa(manhwa: ManhwaEntity): Long

    @Update
    suspend fun updateManhwa(manhwa: ManhwaEntity)

    @Query("DELETE FROM manhwas WHERE id = :id")
    suspend fun deleteManhwaById(id: Long)

    @Query("DELETE FROM manhwas")
    suspend fun deleteAll()
}

@Dao
interface PanelDao {
    @Query("SELECT * FROM panels WHERE manhwaId = :manhwaId ORDER BY panelIndex ASC")
    fun getPanelsForManhwaFlow(manhwaId: Long): Flow<List<PanelEntity>>

    @Query("SELECT * FROM panels WHERE manhwaId = :manhwaId ORDER BY panelIndex ASC")
    suspend fun getPanelsForManhwa(manhwaId: Long): List<PanelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPanel(panel: PanelEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPanels(panels: List<PanelEntity>)

    @Query("DELETE FROM panels WHERE manhwaId = :manhwaId")
    suspend fun deletePanelsForManhwa(manhwaId: Long)
}

@Dao
interface OnomatopoeiaDao {
    @Query("SELECT * FROM onomatopoeias WHERE panelId = :panelId")
    suspend fun getOnomatopoeiasForPanel(panelId: Long): List<OnomatopoeiaEntity>

    @Query("SELECT * FROM onomatopoeias WHERE panelId IN (:panelIds)")
    suspend fun getOnomatopoeiasForPanels(panelIds: List<Long>): List<OnomatopoeiaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOnomatopoeia(onomatopoeia: OnomatopoeiaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOnomatopoeias(onomatopoeias: List<OnomatopoeiaEntity>)
}

@Dao
interface VisualFxDao {
    @Query("SELECT * FROM visual_fx WHERE panelId = :panelId")
    suspend fun getVisualFxForPanel(panelId: Long): List<VisualFxEntity>

    @Query("SELECT * FROM visual_fx WHERE panelId IN (:panelIds)")
    suspend fun getVisualFxForPanels(panelIds: List<Long>): List<VisualFxEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisualFx(fx: VisualFxEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisualFxs(fxs: List<VisualFxEntity>)
}

package com.mustafayusef.sharay.database


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.croczi.teami.database.entitis.FeedbackRequestLocal
import com.croczi.teami.models.MyResources
import com.mustafayusef.sharay.database.entitis.MyResourcesLocal


@Dao
interface FeedBack_Dao {
    @Query("select * from FeedbackRequestLocal")
    suspend fun getAllResource():List<FeedbackRequestLocal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
   suspend fun insertFeedBack(Resources:FeedbackRequestLocal)

  @Query("delete from FeedbackRequestLocal where IdDb=:id")
   fun deleteFeedBackById(id: Int?):Int

//
//    @Query("DELETE FROM latestCar where idDb NOT IN (SELECT idDb from latestCar ORDER BY idDb DESC LIMIT 10)")
//    suspend fun deleteCars():Int

//    @Query("DELETE FROM FeedbackRequestLocal")
//    suspend fun deleteAllResource()
}
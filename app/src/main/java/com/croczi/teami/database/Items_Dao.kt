package com.mustafayusef.sharay.database


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.croczi.teami.database.entitis.ItemLocal
import com.croczi.teami.models.MyResources
import com.mustafayusef.sharay.database.entitis.MyResourcesLocal


@Dao
interface Items_Dao {
    @Query("select * from ItemLocal")
    suspend fun getAllItems():List<ItemLocal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
   suspend fun inserAllItems(Resources: List<ItemLocal>)

//  @Query("delete from latestCar where id=:id")
//   fun deletsurvayById(id: Int?):Int

//
//    @Query("DELETE FROM latestCar where idDb NOT IN (SELECT idDb from latestCar ORDER BY idDb DESC LIMIT 10)")
//    suspend fun deleteCars():Int

    @Query("DELETE FROM ItemLocal")
    suspend fun deleteAllItems()
}
package com.mustafayusef.sharay.database


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.croczi.teami.database.entitis.StatusPharmasyResourceLocal


@Dao
interface StatusPh_Dao {
    @Query("select * from StatusPharmasyResourceLocal")
    suspend fun getAllStatusPharnasy():List<StatusPharmasyResourceLocal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
   suspend fun inserAllStatusPharnasy(Resources: List<StatusPharmasyResourceLocal>)

//  @Query("delete from latestCar where id=:id")
//   fun deletsurvayById(id: Int?):Int

//
//    @Query("DELETE FROM latestCar where idDb NOT IN (SELECT idDb from latestCar ORDER BY idDb DESC LIMIT 10)")
//    suspend fun deleteCars():Int

    @Query("DELETE FROM StatusPharmasyResourceLocal")
    suspend fun deleteAllStatusPharnasy()
}
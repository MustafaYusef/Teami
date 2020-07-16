package com.mustafayusef.sharay.database


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.croczi.teami.database.entitis.OrderRequestLocal
import com.croczi.teami.models.MyResources
import com.croczi.teami.models.Order
import com.mustafayusef.sharay.database.entitis.MyResourcesLocal


@Dao
interface Orders_Dao {
    @Query("select * from OrderRequestLocal")
    suspend fun getAllOrders():List<OrderRequestLocal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
   suspend fun insertOrder(Resources: OrderRequestLocal)

  @Query("delete from OrderRequestLocal where IdDb=:id")
   fun deleteOrderById(id: Int?):Int

        @Query("DELETE FROM OrderRequestLocal")
    suspend fun deleteAll()

//
//    @Query("DELETE FROM latestCar where idDb NOT IN (SELECT idDb from latestCar ORDER BY idDb DESC LIMIT 10)")
//    suspend fun deleteCars():Int

//    @Query("DELETE FROM MyResourcesLocal")
//    suspend fun deleteAllResource()
}
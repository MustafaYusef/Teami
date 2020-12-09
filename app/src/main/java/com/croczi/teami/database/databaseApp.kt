package com.mustafayusef.sharay.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.croczi.teami.database.entitis.*

import com.mustafayusef.sharay.database.entitis.MyResourcesLocal


@Database(entities = [MyResourcesLocal::class,FeedbackRequestLocal::class
        ,ItemLocal::class,OrderRequestLocal::class,StatusPharmasyResourceLocal::class,
         StatusResourceLocal::class], version = 7)
public abstract class databaseApp : RoomDatabase() {


    abstract fun resource_Dao(): Resource_Dao
    abstract fun feedBack_Dao(): FeedBack_Dao
    abstract fun items_Dao(): Items_Dao
    abstract fun orders_Dao(): Orders_Dao
    abstract fun statusDoc_Dao(): StatusDoc_Dao
    abstract fun statusPh_Dao(): StatusPh_Dao
    companion object {
        @Volatile
        private var INSTANCE: databaseApp? = null
        private val Lock=Any()

        operator fun invoke(context: Context)=INSTANCE?: synchronized(Lock){
            INSTANCE?:getDatabase(context).also {
                INSTANCE=it
            }
        }
//        val MIGRATION_3_4 = object : Migration(3, 4) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("ALTER TABLE OrderRequestLocal ADD COLUMN created_at TEXT")
//            }
//        }
//
//        val MIGRATION_4_5 = object : Migration(4, 5) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("ALTER TABLE FeedbackRequestLocal ADD COLUMN created_at TEXT")
//            }
//        }

        fun getDatabase(context: Context)=
            Room.databaseBuilder(
                context.applicationContext,
                databaseApp::class.java,
                "sharay"
            ).fallbackToDestructiveMigration() .build()

    }
}




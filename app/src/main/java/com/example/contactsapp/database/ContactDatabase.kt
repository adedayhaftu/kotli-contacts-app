package com.example.contactsapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.contactsapp.model.Contact

@Database(entities = arrayOf(Contact::class), version = 1)
abstract class ContactsDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactsDao
    companion object {
        private var database: ContactsDatabase? = null
        fun getDatabase(context: Context): ContactsDatabase {
            if (database == null) {
                database = Room
                    .databaseBuilder(context, ContactsDatabase::class.java, "contactsDb")

                        .fallbackToDestructiveMigration(true)
                        .build()
            }
            return database as ContactsDatabase
        }
    }
}
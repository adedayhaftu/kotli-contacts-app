package com.example.contactsapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.contactsapp.model.Contact


@Dao
interface ContactsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
   suspend fun insertContact(contact: Contact)
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): LiveData<List<Contact>>
    @Query("SELECT * FROM Contacts WHERE contactId = :contactId")
    fun getContactById(contactId: Int): LiveData<Contact>

    @Update
    suspend fun updateContact(contact: Contact)

    @Query("Delete from contacts where contactId = :contactId")
    suspend fun deleteContactById(contactId: Int)
    @Query("SELECT * FROM contacts WHERE contactId = :contactId")
    suspend fun getSuspendContactById(contactId: Int): Contact?
}


package com.example.contactsapp.repository

import androidx.lifecycle.LiveData
import com.example.contactsapp.ContactsApp
import com.example.contactsapp.database.ContactsDatabase
import com.example.contactsapp.model.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class ContactsRepository {
    private val database = ContactsDatabase.getDatabase(ContactsApp.appContext)
    private val dao = database.contactDao()
    suspend fun saveContact(contact: Contact) {
        withContext(Dispatchers.IO) {
            dao.insertContact(contact)
        }
    }
    fun getContacts(): LiveData<List<Contact>> = dao.getAllContacts()
    fun getContactById(contactId: Int): LiveData<Contact> = dao.getContactById(contactId)
    suspend fun updateContact(contact: Contact) {
        withContext(Dispatchers.IO) {
            dao.updateContact(contact)

        }

    }
    suspend fun getSuspendContactById(contactId: Int): Contact? {
        return withContext(Dispatchers.IO) {
            dao.getSuspendContactById(contactId)
        }
    }

    suspend fun deleteContactById(contactId: Int){
        withContext(Dispatchers.IO){
            dao.deleteContactById(contactId)
        }
    }
}
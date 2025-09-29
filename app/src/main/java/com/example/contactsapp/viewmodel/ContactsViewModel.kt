package com.example.contactsapp.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.contactsapp.model.Contact
import com.example.contactsapp.repository.ContactsRepository


import kotlinx.coroutines.launch

class ContactsViewModel : ViewModel() {
    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> = _contacts

    private val _contactLiveData = MutableLiveData<Contact?>()
    val contactLiveData: LiveData<Contact?> = _contactLiveData

    private val repo = ContactsRepository()

    private val allContactsObserver: (List<Contact>) -> Unit = { contactList ->
        _contacts.value = contactList
    }

    fun getContacts() {
        repo.getContacts().removeObserver(allContactsObserver)
        repo.getContacts().observeForever(allContactsObserver)
    }

    fun saveContact(contact: Contact) {
        viewModelScope.launch {
            repo.saveContact(contact)
        }
    }

    fun getContactById(contactId: Int) {
        viewModelScope.launch {
            val contact = repo.getSuspendContactById(contactId)
            _contactLiveData.postValue(contact)
        }
    }

    fun updateContactImage(contactId: Int, imageUrl: String) {
        viewModelScope.launch {
            val currentContact = repo.getSuspendContactById(contactId)

            currentContact?.let { contactToUpdate ->
                val updatedContact = contactToUpdate.copy(imageUrl = imageUrl)
                repo.updateContact(updatedContact)
                getContactById(contactId)
            }
        }
    }

    fun deleteContactById(contactId: Int) {
        viewModelScope.launch {
            repo.deleteContactById(contactId)
            if (_contactLiveData.value?.contactId == contactId) {
                _contactLiveData.postValue(null)
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        repo.getContacts().removeObserver(allContactsObserver)

    }
}
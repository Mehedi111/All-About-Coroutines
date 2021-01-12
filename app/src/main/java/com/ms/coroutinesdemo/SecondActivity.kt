package com.ms.coroutinesdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SecondActivity : AppCompatActivity() {
    private val TAG = "SecondActivity_txt"
    private lateinit var dataStore: DataStore<Preferences>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second_acitivity)


        dataStore = createDataStore(name="test_pref")
        val key = "KEY"

        GlobalScope.launch {
            saveData(key, "I am from mars")
            val data = readData(key)

            Log.d(TAG, "onCreate: values $data")
        }
    }

    private suspend fun saveData(key: String, value: String){
        val dataStoreKey = preferencesKey<String>(key)
        dataStore.edit {
            it[dataStoreKey] = value
            Log.d(TAG, "saveData: saved")
        }
    }

    private suspend fun readData(key: String): String?{
        val dataStoreKey = preferencesKey<String>(key)
        val preferences = dataStore.data.first()
        return preferences[dataStoreKey]
    }
}
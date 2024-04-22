package com.example.predictionlib.database

import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.LinkedList

class EnDictionaryDatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    private var myDB: SQLiteDatabase? = null
    var DB_PATH = context.getDatabasePath(DB_NAME).path

    override fun onCreate(db: SQLiteDatabase) = Unit

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    @Synchronized
    override fun close() {
        if (myDB != null) {
            myDB?.close()
        }
        super.close()
    }

    @Throws(IOException::class)
    fun createDataBase() {
        val dbExist = checkDataBase()
        if (dbExist) return
        try {
            copyDataBase()
        } catch (e: IOException) {
            Log.e("$LOG_DIC_DB - create", e.message.toString())
        }
    }

    @Throws(SQLException::class)
    fun openDataBase() {
        val myPath = DB_PATH + DB_NAME
        myDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE)
    }

    private fun checkDataBase(): Boolean {
        var tempDB: SQLiteDatabase? = null
        try {
            val myPath = DB_PATH
            tempDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE)
        } catch (e: SQLiteException) {
            Log.e("$LOG_DIC_DB - check", e.message.toString())
        }
        tempDB?.close()
        return tempDB != null
    }

    @Throws(IOException::class)
    fun copyDataBase() {
        try {
            val myInput = context.assets.open(DB_NAME)
            val outputFileName = DB_PATH
            val myOutput: OutputStream = FileOutputStream(outputFileName)
            val buffer = ByteArray(1024)
            var length: Int
            while (myInput.read(buffer).also { length = it } > 0) {
                myOutput.write(buffer, 0, length)
            }
            myOutput.flush()
            myOutput.close()
            myInput.close()
        } catch (e: Exception) {
            Log.e("$LOG_DIC_DB - copyDatabase", e.message.toString())
        }
    }

    fun predictCurrentWord(number: Int, word: String): List<String>? {
        val words: MutableList<String> = LinkedList()
        val db = this.readableDatabase
        val cursor: Cursor?
        try {
            cursor = db.rawQuery(
                "SELECT DISTINCT NAME FROM $TB_EN_DICTIONARY WHERE NAME LIKE '$word%' AND NAME != '$word' ORDER BY FREQUENCY DESC LIMIT $number",
                null
            )
            if (cursor == null) return null
            var name: String
            cursor.moveToFirst()
            do {
                name = cursor.getString(0)
                words.add(name)
            } while (cursor.moveToNext())
            cursor.close()
        } catch (e: Exception) {
            Log.e(LOG_DIC_DB, e.message.toString())
        }
        return words
    }

    fun selectWord(word: String) {
        val db = this.writableDatabase
        try {
            db.execSQL("UPDATE $TB_EN_DICTIONARY SET FREQUENCY=FREQUENCY+1 WHERE NAME='$word'")
        } catch (e: Exception) {
            Log.e(LOG_DIC_DB, e.message.toString())
        }
        db.close()
    }

    fun addWord(word: String) {
        val db = this.writableDatabase
        try {
            db.execSQL("INSERT INTO $TB_EN_DICTIONARY (_id, ID, NAME, FREQUENCY) VALUES (null, null, '$word', 1)")
        } catch (e: Exception) {
            Log.e(LOG_DIC_DB, e.message.toString())
        }
        db.close()
    }

    fun wordExistInDictionary(word: String): Boolean {
        val db = this.readableDatabase
        val cursor: Cursor?
        return try {
            cursor = db.rawQuery(
                "SELECT NAME FROM $TB_EN_DICTIONARY WHERE NAME = '$word'",
                null
            )
            if (cursor == null) {
                return false
            }
            if (cursor.count > 0) {
                cursor.close()
                return true
            }
            cursor.close()
            false
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        var DB_NAME = "en.db"
        const val DB_VERSION = 1
        const val TB_EN_DICTIONARY = "enDictionary"
        const val LOG_DIC_DB = "logDicDb"
    }
}
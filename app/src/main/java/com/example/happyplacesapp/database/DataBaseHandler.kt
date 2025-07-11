package com.example.happyplacesapp.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import com.example.happyplacesapp.model.HappyPlaceModel
import com.example.happyplacesapp.MainActivity

class DataBaseHandler(activity: MainActivity) {
    SQLiteOpenHelper( context, DATABASE_NAME, null, DATABASE_VERSION) {

        companion object {
            private val DATABASE_VERSION = 1
            private val DATABASE_NAME = "HappyPlacesDatabase"
            private val TABLE_HAPPY_PLACE = "HappyPlacesTable"

            private val KEY_ID = "_id"
            private val KEY_TITLE = "title"
            private val KEY_IMAGE = "image"
            private val KEY_DESCRIPTION = "description"
            private val KEY_DATE = "date"
            private val KEY_LOCATION = "location"
            private val KEY_LATITUDE = "latitude"
            private val KEY_LONGITUDE = "longitude"
        }
        override fun onCreate(db: SQLiteDatabase?) {
            //creating table with fields
            val TABLE_HAPPY_PLACE = null
            val KEY_ID = null
            val KEY_TITLE = null
            val CREATE_HAPPY_PLACE_TABLE = ("CREATE TABLE " + TABLE_HAPPY_PLACE + "("
                    + KEY_ID + " INTEGER PRIMARY KEY,"
                    + KEY_TITLE + " TEXT,"
                    + KEY_IMAGE + " TEXT,"
                    + KEY_DESCRIPTION + " TEXT,"
                    + KEY_DATE + " TEXT,"
                    + KEY_LOCATION + " TEXT,"
                    + KEY_LATITUDE + " TEXT,"
                    + KEY_LONGITUDE + " TEXT)")
            db?.execSQL(CREATE_HAPPY_PLACE_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db!!.execSQL("DROP TABLE IF EXISTS $TABLE_HAPPY_PLACE")
            onCreate(db)
        }

        fun addHappyPlace(happyPlace: HappyPlaceModel): Long {
            val db = this.writableDatabase

            val contentValues = ContentValues()
            contentValues.put(KEY_TITLE, happyPlace.title) // HappyPlaceModelClass TITLE
            contentValues.put(KEY_IMAGE, happyPlace.image) // HappyPlaceModelClass IMAGE
            contentValues.put(
                KEY_DESCRIPTION,
                happyPlace.description
            )
            contentValues.put(KEY_DATE, happyPlace.date) // HappyPlaceModelClass DATE
            contentValues.put(KEY_LOCATION, happyPlace.location) // HappyPlaceModelClass LOCATION
            contentValues.put(KEY_LATITUDE, happyPlace.latitude) // HappyPlaceModelClass LATITUDE
            contentValues.put(KEY_LONGITUDE, happyPlace.longitude) // HappyPlaceModelClass LONGITUDE

            // Inserting Row
            val result = db.insert(TABLE_HAPPY_PLACE, null, contentValues)
            //2nd argument is String containing nullColumnHack

            db.close() // Closing database connection
            return result
        }

        fun updateHappyPlace(happyPlace: HappyPlaceModel): Int {
            val db = this.writableDatabase
            val contentValues = ContentValues()
            contentValues.put(KEY_TITLE, happyPlace.title) // HappyPlaceModelClass TITLE
            contentValues.put(KEY_IMAGE, happyPlace.image) // HappyPlaceModelClass IMAGE
            contentValues.put(
                KEY_DESCRIPTION,
                happyPlace.description
            )
            contentValues.put(KEY_DATE, happyPlace.date) // HappyPlaceModelClass DATE
            contentValues.put(KEY_LOCATION, happyPlace.location) // HappyPlaceModelClass LOCATION
            contentValues.put(KEY_LATITUDE, happyPlace.latitude) // HappyPlaceModelClass LATITUDE
            contentValues.put(KEY_LONGITUDE, happyPlace.longitude) // HappyPlaceModelClass LONGITUDE

            // Updating Row
            val success =
                db.update(
                    TABLE_HAPPY_PLACE, //the table which we want to update
                    contentValues, //the values
                    KEY_ID + "=" + happyPlace.id, //which row to update
                    null
                )

            db.close() // Closing database connection
            return success
        }

        fun deleteHappyPlace(happyPlace: HappyPlaceModel): Int {
            val db = this.writableDatabase
            val success = db.delete(TABLE_HAPPY_PLACE, KEY_ID + "=" + happyPlace.id, null)
            db.close()
            return success
        }

        fun getHappyPlacesList(): ArrayList<HappyPlaceModel> {
            val happyPlaceList: ArrayList<HappyPlaceModel> = ArrayList()
            val selectQuery = "SELECT * FROM $TABLE_HAPPY_PLACE"
            val db = this.readableDatabase

            try {
                val cursor: Cursor = db.rawQuery(selectQuery, null)
                if (cursor.moveToFirst()) {
                    do {
                        val id = cursor.getColumnIndex(KEY_ID)
                        val title = cursor.getColumnIndex(KEY_TITLE)
                        val image = cursor.getColumnIndex(KEY_IMAGE)
                        val description = cursor.getColumnIndex(KEY_DESCRIPTION)
                        val date = cursor.getColumnIndex(KEY_DATE)
                        val location = cursor.getColumnIndex(KEY_LOCATION)
                        val latitude = cursor.getColumnIndex(KEY_LATITUDE)
                        val longitude = cursor.getColumnIndex(KEY_LONGITUDE)

                        if (id > -1 && title > -1 && image > -1 && description > -1 && date > -1 && location > -1 && latitude > -1 && longitude > -1) {
                            val place = HappyPlaceModel(
                                cursor.getInt(id),
                                cursor.getString(title),
                                cursor.getString(image),
                                cursor.getString(description),
                                cursor.getString(date),
                                cursor.getString(location),
                                cursor.getDouble(latitude),
                                cursor.getDouble(longitude)
                            )
                            happyPlaceList.add(place)
                        }
                    } while (cursor.moveToNext())

                    cursor.close()
                }
            } catch (e: SQLiteException) {
                db.execSQL(selectQuery)
                return ArrayList()
            }
            return happyPlaceList
        }

}
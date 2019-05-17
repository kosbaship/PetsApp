/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kosbaship.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.kosbaship.android.pets.data.PetContract.PetEntry;
//                  (101)
//(101 - B) got to AndroidManifest.xml
//(101 - A)
// create this PetProvider.java to talk to our database source (PetDBHelper.java)
// extends the ContentProvider to :
//      implement the the five methods {insert, query, update, delete, getType}

/**
 * {@link ContentProvider} for Pets app.
 */
public class PetProvider extends ContentProvider {
    //    Since we’ll be logging multiple times throughout this file,
    //    it would be ideal to create a log tag as a global constant variable,
    //    so all log messages from the PetProvider will have the same log tag identifier
    //    when you are reading the system logs.
    /** Tag for the log messages */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    //(101 - E - 2 = bonus part)
    // we create this a static because we will using them many times
    // one when we setup the uri matchers
    // two when we matches those uri with what we defined
    /** URI matcher code for the content URI for the pets table */
    private static final int PETS = 100;
    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PET_ID = 101;
    //                          (101 - E)
    //(101 - E - 1)
    // declare this global variable
    // to use it inside {insert, query, update, delete}
    // start with the lower case s because it's denoting
    // its a static variables
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //(101 - E - 2)
    // setup the global variable
    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.pets/pets" will map to the
        // integer code {@link #PETS}. This URI is used to provide access to MULTIPLE rows
        // of the pets table.
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);

        // The content URI of the form "content://com.example.android.pets/pets/#" will map to the
        // integer code {@link #PET_ID}. This URI is used to provide access to ONE single row
        // of the pets table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // In this case, the "*" wildcard is used where "*" can be substituted for a string.
        // For example, "content://com.example.android.pets/pets/3" matches, but
        // "content://com.example.android.pets/pets" (without a number at the end) doesn't match.
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    //(101 - C - 1)
    //(101 - D) go to PetContract.java
    // declare a PetDbHelper object
    /** Database helper object */
    private PetDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        //(101 - C - 2)
        // initialize and Create a PetDbHelper object to gain access to the pets database
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }
    //                              (101 - F)
    // (101 - G) Go to CatalogActivity
    // there is 3 major parts of this method
    // 1 - get the db object
    // 2 - send a uri through uri matcher and this will help us whch way to fork off into
    // 3 - execute the right path
    //the return value will be a cursor which an object contains rows of the data
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        //(101 - F - 1)
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        //(101 - F - 2)
        // This cursor will hold the result of the query
        Cursor cursor;

        //(101 - F - 3)
        // Figure out if the URI matcher can match the URI to a specific code
        // and do not forget those codes we defined them early in the step (101 - E - 2)
        // with all possible paths
        int match = sUriMatcher.match(uri);
        //(101 - F - 4)
        // decide which path to go down
        switch (match) {
            case PETS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PetEntry._ID + "=?";
                // ContentUris.parseId(uri)):
                //      this will extract the last segment of the uri path into a number for us
                // then String.valueOf() :
                //      this will convert this segment into a string
                //      because the selectionArgs is a string array
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        //(101 - F - 5)
        // Return the cursor
        return cursor;
    }
    //                              (101 - H)
    // (101 - I) go to CatalogActivity.java
    // the return value will be uri to tell us where exactly the pet inserted (Row ID)
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        // (101 - H - 1)
        // check if there is a match
        final int match = sUriMatcher.match(uri);
        // (101 - H - 2)
        // based on match case perform this action
        // we only have one match case which add the new pet into
        // the whole table
        switch (match) {
            case PETS:
                //(101 - H) insert the pet into the database
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }
    // (101 - H - 3)
    /**
     * //create the method to actually
     * Insert a pet into the database with the given content values.
     * Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {
        //(101 - K)
        //(101 - K - 1)
        //Add Data Validation or sanity check for our app
        // Check that the name is not null
        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name");
        }
        //(101 - K - 2)
        // Check that the gender is valid
        // we pass (as argument) to the isValidGender() the integer number
        // inside the ContentValue coming from the user
        Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires valid gender");
        }
        //(101 - K - 3)
        //(101 - K - 4) Go to PetContract.java
        // If the weight is provided, check that it's greater than or equal to 0 kg
        Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Pet requires valid weight");
        }
        //(101 - K - 5)
        // No need to check the breed, any value is valid (including null).

        // (101 - H - 3 - a)
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // (101 - H - 3 - b)
        // Insert the new pet with the given values
        // we will get the row ID as a return value we can get benefit from
        // this Id in many things like
        // 1- check with it if the pet inserted or no
        // 2- add it we the return uri to the caller activity
        long id = database.insert(PetEntry.TABLE_NAME, null, values);
        // (101 - H - 3 - c)
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // (101 - H - 3 - d)
        // ContentUris.withAppendedId() :
        //        Will add the row ID to the end of the pet URI
        //        this id we get as a return value of the insertion
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }
    // (101 - L)
    // the return of this method will be the number of rows inserted into the database
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        // (101 - L - 1)
        // Figure out if the URI matcher can match the URI to a specific code
        // and do not forget those codes we defined them early in the step (101 - E - 2)
        // with all possible paths
        final int match = sUriMatcher.match(uri);
        //(101 - L - 2)
        // decide which path to go down
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetEntry._ID + "=?";
                // ContentUris.parseId(uri)):
                //      this will extract the last segment of the uri path into a number for us
                // then String.valueOf() :
                //      this will convert this segment into a string
                //      because the selectionArgs is a string array
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                //(101 - L - 7)
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }
    //(101 - L - 3)
    // it is better for u to check the whole insert method first befor
    // checking this method
    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //(101 - L - 4)
        //do the sanity check
        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        // containsKey():
        //      to check whether a key/value pair exists in the ContentValues
        if (values.containsKey(PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }
        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        // containsKey():
        //      to check whether a key/value pair exists in the ContentValues
        if (values.containsKey(PetEntry.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            // Check that the gender is valid
            // we pass (as argument) to the isValidGender() the integer number
            // inside the ContentValue coming from the user
            if (gender == null || !PetEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }
        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        // containsKey():
        //      to check whether a key/value pair exists in the ContentValues
        if (values.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }
        // No need to check the breed, any value is valid (including null).
        // If there are no values to update, then don't try to update the database
        //There is no need to do the database operation if there are no new values to update
        // with, and every database operation costs some amount of memory resources
        // on the device.
        if (values.size() == 0) {
            // go out of the update method here
            return 0;
        }
        //(101 - L - 5)
        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        //(101 - L - 6)
        // Returns the number of database rows affected by the update statement
        return database.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    //                              (101 - M)
    // returns the value of the numbers of rows deleted into the database
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //(101 - M - 1)
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        //(101 - M - 2)
        // Figure out if the URI matcher can match the URI to a specific code
        // and do not forget those codes we defined them early in the step (101 - E - 2)
        // with all possible paths
        final int match = sUriMatcher.match(uri);
        //(101 - M - 3)
        //decide which path to go dawn
        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args
                return database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetEntry._ID + "=?";
                // ContentUris.parseId(uri)):
                //      this will extract the last segment of the uri path into a number for us
                // then String.valueOf() :
                //      this will convert this segment into a string
                //      because the selectionArgs is a string array
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }
    //                                          (101 - N)
    //(102) go to list_item.xml
    //The purpose of this method is to return a String that describes the type of the data
    // stored at the input Uri. This String is known as the MIME type,
    // which can also be referred to as content type.

    //One use case where this functionality is important is if you’re
    // sending an intent with a URI set on the data field.
    // The Android system will check the MIME type of that URI to determine
    // which app component on the device can best handle your request.
    // (If the URI happens to be a content URI,
    // then the system will check with the corresponding ContentProvider
    // to ask for the MIME type using the getType() method.
    @Override
    public String getType(Uri uri) {
        //(101 - N - 1)
        //(101 - N - 2) Go to PetContract.java
        // Figure out if the URI matcher can match the URI to a specific code
        // and do not forget those codes we defined them early in the step (101 - E - 2)
        // with all possible paths
        final int match = sUriMatcher.match(uri);
        //(101 - N - 3) decide which path to go into
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}

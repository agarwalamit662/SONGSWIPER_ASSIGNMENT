/*
 * Copyright 2012-2013 Andrea De Cesare
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package example.prgguru.com.songswiper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;


import android.net.Uri;


import java.util.ArrayList;


import example.prgguru.com.songswiper.data.UserProvider;
import example.prgguru.com.songswiper.model.MOVIESONGS;


public class BrowseMovieSongs {
    private static final long serialVersionUID = 1L;
    private final static String[] projection = {
            UserProvider._MOVIE_ID,
            UserProvider._SONG_ID,
            UserProvider._MOVIE_NAME,
            UserProvider._SONG_NAME,
            UserProvider._SONG_LIKED,
            UserProvider._SONG_DELETE
    };
    private static ContentResolver mediaResolver;

    public static ArrayList<MOVIESONGS> getSongsInDirectory(Context mContext) {
        ArrayList<MOVIESONGS> moviesongs = new ArrayList<>();
        Context contexts = mContext;

        String where = UserProvider._SONG_DELETE + "=?";

        String[] selectionArgs = new String[]{String.valueOf(0)};

        ContentResolver resolver = contexts.getContentResolver();

        String sortOrder = UserProvider._MOVIE_ID;
        Uri uri = UserProvider.CONTENT_URI_MOVIES;

        Cursor cursor = resolver.query(uri, projection, where, selectionArgs, sortOrder);
        int midindex = cursor.getColumnIndex(UserProvider._MOVIE_ID);
        int sidindex = cursor.getColumnIndex(UserProvider._SONG_ID);
        int mnameindex = cursor.getColumnIndex(UserProvider._MOVIE_NAME);
        int snameindex = cursor.getColumnIndex(UserProvider._SONG_NAME);
        int slikedindex = cursor.getColumnIndex(UserProvider._SONG_LIKED);
        int sdeleteindex = cursor.getColumnIndex(UserProvider._SONG_DELETE);


        if (cursor != null && cursor.moveToFirst()) {
            do {
                int mid = cursor.getInt(midindex);
                int sid = cursor.getInt(sidindex);
                String mname = cursor.getString(mnameindex);
                String sname = cursor.getString(snameindex);
                String slike = cursor.getString(slikedindex);
                String sdelete = cursor.getString(sdeleteindex);


                moviesongs.add(new MOVIESONGS(mid, sid, mname, sname, slike,sdelete));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return moviesongs;
    }


}


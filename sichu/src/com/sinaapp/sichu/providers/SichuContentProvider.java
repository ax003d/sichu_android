package com.sinaapp.sichu.providers;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.sinaapp.sichu.models.Book.Books;
import com.sinaapp.sichu.models.BookOwn.BookOwns;

public class SichuContentProvider extends ContentProvider {

	private static final String DATABASE_NAME = "sichu.db";
	private static final int DATABASE_VERSION = 1;
	private static final UriMatcher URI_MATCHER;
	private static final int BOOKOWNS_BY_OWNER = 1;
	private static final int BOOKS = 2;
	private static final int BOOKOWNS = 3;
	private static final int BOOK_BY_GUID = 4;
	private static final int BOOKOWN_BY_GUID = 5;

	private static HashMap<String, String> booksProjectionMap;
	private static HashMap<String, String> bookownsProjectionMap;
	private static HashMap<String, String> bookownsWithBookProjectionMap;

	public static final String AUTHORITY = "com.sinaapp.sichu.providers.SichuContentProvider";

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + Books.TABLE_NAME + " (" + Books._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + Books.GUID
					+ " INTEGER UNIQUE, " + Books.ISBN + " VARCHAR(32), "
					+ Books.TITLE + " VARCHAR(128), " + Books.AUTHOR
					+ " VARCHAR(128), " + Books.DOUBAN_ID + " VARCHAR(32), "
					+ Books.COVER + " VARCHAR(128)" + " );");
			db.execSQL("CREATE TABLE " + BookOwns.TABLE_NAME + " ("
					+ BookOwns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ BookOwns.GUID + " INTEGER UNIQUE, " + BookOwns.BOOKID
					+ " INTEGER, " + BookOwns.OWNERID + " INTEGER, "
					+ BookOwns.STATUS + " VARCHAR(16), " + BookOwns.HASEBOOK
					+ " BOOLEAN, " + BookOwns.REMARK + " TEXT" + " );");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + Books.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + BookOwns.TABLE_NAME);
			onCreate(db);
		}

	} // DatabaseHelper

	private DatabaseHelper db_helper;

	@Override
	public boolean onCreate() {
		db_helper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		SQLiteDatabase db = db_helper.getReadableDatabase();

		switch (URI_MATCHER.match(uri)) {
		case BOOKOWNS_BY_OWNER:
			queryBuilder.setTables(BookOwns.TABLE_NAME + " INNER JOIN "
					+ Books.TABLE_NAME + " ON ( " + BookOwns.BOOKID + " = "
					+ Books.TABLE_NAME + "." + Books.GUID + " )");
			queryBuilder.setProjectionMap(bookownsWithBookProjectionMap);
			selection = (selection == null ? "" : selection + " AND ");
			selection = selection + BookOwns.OWNERID + " = " + uri.getLastPathSegment();
			break;
		case BOOK_BY_GUID:
			queryBuilder.setTables(Books.TABLE_NAME);
			queryBuilder.setProjectionMap(booksProjectionMap);
			selection = Books.GUID + " = " + uri.getLastPathSegment();			
			break;
		case BOOKOWN_BY_GUID:
			queryBuilder.setTables(BookOwns.TABLE_NAME);
			queryBuilder.setProjectionMap(bookownsProjectionMap);
			selection = Books.GUID + " = " + uri.getLastPathSegment();			
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = db_helper.getWritableDatabase();		
		long row_id;
		
		switch ( URI_MATCHER.match(uri) ) {
		case BOOKS:
			row_id = db.insert(Books.TABLE_NAME, Books.GUID, values);
			if ( row_id > 0 ) {
				Uri book_uri = ContentUris.withAppendedId(Books.CONTENT_URI, row_id);
				getContext().getContentResolver().notifyChange(book_uri, null);
				return book_uri;
			}			
			break;
		case BOOKOWNS:
			row_id = db.insert(BookOwns.TABLE_NAME, BookOwns.GUID, values);
			if ( row_id > 0 ) {
				Uri bookown_uri = ContentUris.withAppendedId(BookOwns.CONTENT_URI, row_id);
				getContext().getContentResolver().notifyChange(bookown_uri, null);
				return bookown_uri;
			}			
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}		
		
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = db_helper.getWritableDatabase();
		String table_name;
		switch ( URI_MATCHER.match(uri) ) {
		case BOOKOWN_BY_GUID:
			table_name = BookOwns.TABLE_NAME;
			selection = BookOwns.GUID + " = " + uri.getLastPathSegment();
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		int count = db.delete(table_name, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = db_helper.getWritableDatabase();
		int count = 0;
		switch (URI_MATCHER.match(uri)) {
		case BOOKOWN_BY_GUID:
			selection = BookOwns.GUID + " = " + uri.getLastPathSegment();
			count = db.update(BookOwns.TABLE_NAME, values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, BookOwns.TABLE_NAME + "/owner/#",
				BOOKOWNS_BY_OWNER);
		URI_MATCHER.addURI(AUTHORITY, Books.TABLE_NAME, BOOKS);
		URI_MATCHER.addURI(AUTHORITY, BookOwns.TABLE_NAME, BOOKOWNS);
		URI_MATCHER.addURI(AUTHORITY, Books.TABLE_NAME + "/guid/#", BOOK_BY_GUID);
		URI_MATCHER.addURI(AUTHORITY, BookOwns.TABLE_NAME + "/guid/#", BOOKOWN_BY_GUID);

		bookownsWithBookProjectionMap = new HashMap<String, String>();
		bookownsWithBookProjectionMap.put(BookOwns.TABLE_NAME  + "." + BookOwns.GUID, 
				BookOwns.TABLE_NAME  + "." + BookOwns.GUID);		
		bookownsWithBookProjectionMap.put(BookOwns.BOOKID, BookOwns.BOOKID);
		bookownsWithBookProjectionMap.put(BookOwns.OWNERID, BookOwns.OWNERID);
		bookownsWithBookProjectionMap.put(BookOwns.STATUS, BookOwns.STATUS);
		bookownsWithBookProjectionMap.put(BookOwns.HASEBOOK, BookOwns.HASEBOOK);
		bookownsWithBookProjectionMap.put(BookOwns.REMARK, BookOwns.REMARK);
		bookownsWithBookProjectionMap.put(Books.ISBN, Books.ISBN);
		bookownsWithBookProjectionMap.put(Books.TITLE, Books.TITLE);
		bookownsWithBookProjectionMap.put(Books.AUTHOR, Books.AUTHOR);
		bookownsWithBookProjectionMap.put(Books.DOUBAN_ID, Books.DOUBAN_ID);
		bookownsWithBookProjectionMap.put(Books.COVER, Books.COVER);
		
		bookownsProjectionMap = new HashMap<String, String>();		
		bookownsProjectionMap.put(BookOwns._ID, BookOwns._ID);
		bookownsProjectionMap.put(BookOwns.GUID, BookOwns.GUID);
		bookownsProjectionMap.put(BookOwns.BOOKID, BookOwns.BOOKID);
		bookownsProjectionMap.put(BookOwns.OWNERID, BookOwns.OWNERID);
		bookownsProjectionMap.put(BookOwns.STATUS, BookOwns.STATUS);
		bookownsProjectionMap.put(BookOwns.HASEBOOK, BookOwns.HASEBOOK);
		bookownsProjectionMap.put(BookOwns.REMARK, BookOwns.REMARK);		
		
		booksProjectionMap = new HashMap<String, String>();
		booksProjectionMap.put(Books._ID, Books._ID);
		booksProjectionMap.put(Books.ISBN, Books.ISBN);
		booksProjectionMap.put(Books.TITLE, Books.TITLE);
		booksProjectionMap.put(Books.AUTHOR, Books.AUTHOR);
		booksProjectionMap.put(Books.DOUBAN_ID, Books.DOUBAN_ID);
		booksProjectionMap.put(Books.COVER, Books.COVER);		
	}
}

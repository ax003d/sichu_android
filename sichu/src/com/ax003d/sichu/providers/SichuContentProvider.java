package com.ax003d.sichu.providers;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.ax003d.sichu.models.Book.Books;
import com.ax003d.sichu.models.BookBorrow.BookBorrows;
import com.ax003d.sichu.models.BookBorrowReq.BookBorrowReqs;
import com.ax003d.sichu.models.BookOwn.BookOwns;
import com.ax003d.sichu.models.Follow.Follows;
import com.ax003d.sichu.models.User.Users;

public class SichuContentProvider extends ContentProvider {

	private static final String DATABASE_NAME = "sichu.db";
	private static final int DATABASE_VERSION = 1;
	private static final UriMatcher URI_MATCHER;
	private static final int BOOKOWNS_BY_OWNER = 1;
	private static final int BOOKS = 2;
	private static final int BOOKOWNS = 3;
	private static final int BOOK_BY_GUID = 4;
	private static final int BOOKOWN_BY_GUID = 5;
	private static final int BOOKBORROWS = 6;
	private static final int USERS = 7;
	private static final int BOOKBORROWS_AS_OWNER = 8;
	private static final int BOOKBORROWS_AS_BORROWER = 9;
	private static final int FOLLOWS = 10;
	private static final int FOLLOWINGS = 11;
	private static final int FOLLOWERS = 12;
	private static final int BOOKBORROWREQS = 13;

	private static HashMap<String, String> booksProjectionMap;
	private static HashMap<String, String> bookownsProjectionMap;
	private static HashMap<String, String> bookownsWithBookProjectionMap;
	private static HashMap<String, String> bookborrowsProjectionMap;

	public static final String AUTHORITY = "com.ax003d.sichu.providers.SichuContentProvider";

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
			db.execSQL("CREATE TABLE " + BookBorrows.TABLE_NAME + " ("
					+ BookBorrows._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ BookBorrows.GUID + " INTEGER UNIQUE, "
					+ BookBorrows.BOOKOWNID + " INTEGER, "
					+ BookBorrows.BORROWERID + " INTEGER, "
					+ BookBorrows.BORROW_DATE + " TEXT, "
					+ BookBorrows.PLANED_RETURN_DATE + " TEXT, "
					+ BookBorrows.RETURNED_DATE + " TEXT" + " );");
			db.execSQL("CREATE TABLE " + Users.TABLE_NAME + " (" + Users._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + Users.GUID
					+ " INTEGER UNIQUE, " + Users.USERNAME + " TEXT, "
					+ Users.LAST_NAME + " TEXT, " + Users.FIRST_NAME
					+ " TEXT, " + Users.AVATAR + " TEXT" + " );");
			db.execSQL("CREATE TABLE " + Follows.TABLE_NAME + " ("
					+ Follows._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ Follows.GUID + " INTEGER UNIQUE, " + Follows.FOLLOWINGID
					+ " INTEGER, " + Follows.REMARK + " TEXT, "
					+ Follows.USERID + " INTEGER" + " );");
			db.execSQL("CREATE TABLE " + BookBorrowReqs.TABLE_NAME + " ("
					+ BookBorrowReqs._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ BookBorrowReqs.GUID + " INTEGER UNIQUE, "
					+ BookBorrowReqs.DATETIME + " TEXT, "
					+ BookBorrowReqs.REQUESTERID + " INTEGER, "
					+ BookBorrowReqs.BOOKOWNID + " INTEGER, "
					+ BookBorrowReqs.PLANED_RETURN_DATE + " TEXT, "
					+ BookBorrowReqs.REMARK + " TEXT, " + BookBorrowReqs.STATUS
					+ " INTEGER" + " );");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + Books.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + BookOwns.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + BookBorrows.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Users.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Follows.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + BookBorrowReqs.TABLE_NAME);
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
		boolean asBorrower = true;
		boolean asFollower = true;

		switch (URI_MATCHER.match(uri)) {
		case BOOKOWNS_BY_OWNER:
			queryBuilder.setTables(BookOwns.TABLE_NAME + " INNER JOIN "
					+ Books.TABLE_NAME + " ON ( " + BookOwns.BOOKID + " = "
					+ Books.TABLE_NAME + "." + Books.GUID + " )");
			queryBuilder.setProjectionMap(bookownsWithBookProjectionMap);
			selection = (selection == null ? "" : selection + " AND ");
			selection = selection + BookOwns.OWNERID + " = "
					+ uri.getLastPathSegment();
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
		case BOOKBORROWS_AS_OWNER:
			asBorrower = false;
		case BOOKBORROWS_AS_BORROWER:
			queryBuilder.setTables(BookBorrows.TABLE_NAME + " INNER JOIN "
					+ BookOwns.TABLE_NAME + " ON ( " + BookBorrows.BOOKOWNID
					+ " = " + BookOwns.TABLE_NAME + "." + BookOwns.GUID
					+ " ) INNER JOIN " + Users.TABLE_NAME + " ON ( "
					+ BookOwns.TABLE_NAME + "." + BookOwns.OWNERID + " = "
					+ Users.TABLE_NAME + "." + Users.GUID + " ) INNER JOIN "
					+ Books.TABLE_NAME + " ON ( " + BookOwns.TABLE_NAME + "."
					+ BookOwns.BOOKID + " = " + Books.TABLE_NAME + "."
					+ Books.GUID + " ) INNER JOIN " + Users.TABLE_NAME
					+ " AS Borrower ON ( " + BookBorrows.TABLE_NAME + "."
					+ BookBorrows.BORROWERID + " = " + "Borrower." + Users.GUID
					+ " )");
			queryBuilder.setProjectionMap(bookborrowsProjectionMap);
			selection = (selection == null ? "" : selection + " AND ");
			selection = selection
					+ (asBorrower ? BookBorrows.BORROWERID : BookOwns.OWNERID)
					+ " = " + uri.getLastPathSegment();
			break;
		case FOLLOWERS:
			asFollower = false;
		case FOLLOWINGS:
			queryBuilder.setTables(Follows.TABLE_NAME + " INNER JOIN "
					+ Users.TABLE_NAME + " ON ( " + Follows.FOLLOWINGID + " = "
					+ Users.TABLE_NAME + "." + Users.GUID + " ) INNER JOIN "
					+ Users.TABLE_NAME + " AS Follower ON ( " + Follows.USERID
					+ " = " + "Follower." + Users.GUID + " )");
			selection = (selection == null ? "" : selection + " AND ");
			selection = (asFollower ? Follows.USERID : Follows.FOLLOWINGID)
					+ " = " + uri.getLastPathSegment();
			break;
		case BOOKBORROWREQS:
			queryBuilder.setTables(BookBorrowReqs.TABLE_NAME + " INNER JOIN "
					+ Users.TABLE_NAME + " ON ( " + BookBorrowReqs.REQUESTERID + " = "
					+ Users.TABLE_NAME + "." + Users.GUID + " ) INNER JOIN "
					+ BookOwns.TABLE_NAME + " ON ( " + BookBorrowReqs.BOOKOWNID
					+ " = " + BookOwns.TABLE_NAME + "." + BookOwns.GUID + " ) INNER JOIN "
					+ Books.TABLE_NAME + " ON ( " + BookOwns.TABLE_NAME + "." + BookOwns.BOOKID
					+ " = " + Books.TABLE_NAME + "." + Books.GUID + " )");
			selection = (selection == null ? "" : selection);			
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

		switch (URI_MATCHER.match(uri)) {
		case BOOKS:
			row_id = db.insertWithOnConflict(Books.TABLE_NAME, Books.GUID,
					values, SQLiteDatabase.CONFLICT_IGNORE);
			// to-do: this should return primary key when conflict, but it
			// return -1,
			// so I return null to walk around it
			if (row_id > 0) {
				Uri book_uri = ContentUris.withAppendedId(Books.CONTENT_URI,
						row_id);
				getContext().getContentResolver().notifyChange(book_uri, null);
				return book_uri;
			}
			return null;
		case BOOKOWNS:
			row_id = db.insertWithOnConflict(BookOwns.TABLE_NAME,
					BookOwns.GUID, values, SQLiteDatabase.CONFLICT_IGNORE);
			if (row_id > 0) {
				Uri bookown_uri = ContentUris.withAppendedId(
						BookOwns.CONTENT_URI, row_id);
				getContext().getContentResolver().notifyChange(bookown_uri,
						null);
				return bookown_uri;
			}
			return null;
		case BOOKBORROWS:
			row_id = db.insertWithOnConflict(BookBorrows.TABLE_NAME,
					BookBorrows.GUID, values, SQLiteDatabase.CONFLICT_IGNORE);
			if (row_id > 0) {
				Uri bookborrow_uri = ContentUris.withAppendedId(
						BookBorrows.CONTENT_URI, row_id);
				getContext().getContentResolver().notifyChange(bookborrow_uri,
						null);
				return bookborrow_uri;
			}
			return null;
		case USERS:
			row_id = db.insertWithOnConflict(Users.TABLE_NAME, Users.GUID,
					values, SQLiteDatabase.CONFLICT_IGNORE);
			if (row_id > 0) {
				Uri user_uri = ContentUris.withAppendedId(Users.CONTENT_URI,
						row_id);
				getContext().getContentResolver().notifyChange(user_uri, null);
				return user_uri;
			}
			return null;
		case FOLLOWS:
			row_id = db.insertWithOnConflict(Follows.TABLE_NAME, Follows.GUID,
					values, SQLiteDatabase.CONFLICT_IGNORE);
			if (row_id > 0) {
				Uri follow_uri = ContentUris.withAppendedId(
						Follows.CONTENT_URI, row_id);
				getContext().getContentResolver()
						.notifyChange(follow_uri, null);
				return follow_uri;
			}
			return null;
		case BOOKBORROWREQS:
			row_id = db.insertWithOnConflict(BookBorrowReqs.TABLE_NAME, BookBorrowReqs.GUID,
					values, SQLiteDatabase.CONFLICT_IGNORE);
			if (row_id > 0) {
				Uri bookborrowreq_uri = ContentUris.withAppendedId(
						BookBorrowReqs.CONTENT_URI, row_id);
				getContext().getContentResolver()
						.notifyChange(bookborrowreq_uri, null);
				return bookborrowreq_uri;
			}
			return null;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = db_helper.getWritableDatabase();
		String table_name;
		switch (URI_MATCHER.match(uri)) {
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
			count = db.update(BookOwns.TABLE_NAME, values, selection,
					selectionArgs);
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
		URI_MATCHER.addURI(AUTHORITY, Books.TABLE_NAME + "/guid/#",
				BOOK_BY_GUID);
		URI_MATCHER.addURI(AUTHORITY, BookOwns.TABLE_NAME + "/guid/#",
				BOOKOWN_BY_GUID);
		URI_MATCHER.addURI(AUTHORITY, BookBorrows.TABLE_NAME, BOOKBORROWS);
		URI_MATCHER.addURI(AUTHORITY, Users.TABLE_NAME, USERS);
		URI_MATCHER.addURI(AUTHORITY, BookBorrows.TABLE_NAME + "/owner/#",
				BOOKBORROWS_AS_OWNER);
		URI_MATCHER.addURI(AUTHORITY, BookBorrows.TABLE_NAME + "/borrower/#",
				BOOKBORROWS_AS_BORROWER);
		URI_MATCHER.addURI(AUTHORITY, Follows.TABLE_NAME, FOLLOWS);
		URI_MATCHER.addURI(AUTHORITY, Follows.TABLE_NAME + "/user/#",
				FOLLOWINGS);
		URI_MATCHER.addURI(AUTHORITY, Follows.TABLE_NAME + "/following/#",
				FOLLOWERS);
		URI_MATCHER.addURI(AUTHORITY, BookBorrowReqs.TABLE_NAME, BOOKBORROWREQS);

		bookownsWithBookProjectionMap = new HashMap<String, String>();
		bookownsWithBookProjectionMap.put(BookOwns.TABLE_NAME + "."
				+ BookOwns.GUID, BookOwns.TABLE_NAME + "." + BookOwns.GUID);
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

		bookborrowsProjectionMap = new HashMap<String, String>();
		bookborrowsProjectionMap.put(BookBorrows.TABLE_NAME + "."
				+ BookBorrows.GUID, BookBorrows.TABLE_NAME + "."
				+ BookBorrows.GUID);
		bookborrowsProjectionMap.put(BookBorrows.BOOKOWNID,
				BookBorrows.BOOKOWNID);
		bookborrowsProjectionMap.put(BookBorrows.BORROWERID,
				BookBorrows.BORROWERID);
		bookborrowsProjectionMap.put(BookBorrows.BORROW_DATE,
				BookBorrows.BORROW_DATE);
		bookborrowsProjectionMap.put(BookBorrows.PLANED_RETURN_DATE,
				BookBorrows.PLANED_RETURN_DATE);
		bookborrowsProjectionMap.put(BookBorrows.RETURNED_DATE,
				BookBorrows.RETURNED_DATE);
		bookborrowsProjectionMap.put("owner", Users.TABLE_NAME + "."
				+ Users.USERNAME + " AS owner");
		bookborrowsProjectionMap.put(Books.TITLE, Books.TITLE);
		bookborrowsProjectionMap.put(Books.COVER, Books.COVER);
		bookborrowsProjectionMap.put("borrower", "Borrower." + Users.USERNAME
				+ " AS borrower");
	}
}

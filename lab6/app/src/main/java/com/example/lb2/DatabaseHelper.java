package com.example.lb2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "reminders.db";
    private static final String TABLE_NAME = "reminders";
    private static final String COL_1 = "ID";
    private static final String COL_2 = "TITLE";
    private static final String COL_3 = "MESSAGE";
    private static final String COL_4 = "DATE";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, TITLE TEXT, MESSAGE TEXT, DATE TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public Reminder addReminder(String title, String message, long triggerTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, title);
        contentValues.put(COL_3, message);
        String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(triggerTime));
        contentValues.put(COL_4, dateString);

        long newId = db.insert(TABLE_NAME, null, contentValues);
        db.close();

        if (newId == -1) {
            Log.e("DatabaseHelper", "Ошибка при добавлении напоминания в базу данных");
            return null; // Возвращаем null в случае ошибки
        }

        // Получаем данные только что добавленного напоминания
        Reminder newReminder = getReminderById((int) newId);

        Log.d("DatabaseHelper", "Напоминание добавлено с ID: " + newId);
        return newReminder;
    }

    // Новый метод для получения напоминания по ID
    public Reminder getReminderById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COL_1 + " = ?", new String[]{String.valueOf(id)}, null, null, null);

        Reminder reminder = null;
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(COL_1);
            int titleIndex = cursor.getColumnIndex(COL_2);
            int messageIndex = cursor.getColumnIndex(COL_3);
            int dateIndex = cursor.getColumnIndex(COL_4);
            if (idIndex != -1 && titleIndex != -1 && messageIndex != -1 && dateIndex != -1) {
                reminder = new Reminder(cursor.getInt(idIndex), cursor.getString(titleIndex), cursor.getString(messageIndex), cursor.getString(dateIndex));
            }
        }
        cursor.close();
        db.close();
        return reminder;
    }



    // Метод для удаления напоминания по ID
    public void deleteReminder(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COL_1 + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Метод для получения всех напоминаний
    public List<Reminder> getAllReminders() {
        List<Reminder> reminderList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                int reminderId = cursor.getColumnIndex(COL_1);
                int titleIndex = cursor.getColumnIndex(COL_2);
                int messageIndex = cursor.getColumnIndex(COL_3);
                int dateIndex = cursor.getColumnIndex(COL_4);

                // Проверяем, что индексы не равны -1
                if (reminderId != -1 && titleIndex != -1 && messageIndex != -1 && dateIndex != -1) {
                    int id = cursor.getInt(reminderId);
                    String title = cursor.getString(titleIndex);
                    String message = cursor.getString(messageIndex);
                    String date = cursor.getString(dateIndex);
                    Reminder reminder = new Reminder(id, title, message, date);
                    reminderList.add(reminder);
                } else {
                    // Логируем предупреждение, если какой-то из индексов не найден
                    Log.w("DatabaseHelper", "Один или несколько столбцов не найдены в результате запроса.");
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return reminderList;
    }
}
package com.example.lb2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.app.DatePickerDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.widget.Toast;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Locale;
import android.content.Context;
import android.os.Build;
import java.util.Date;
import android.view.View;

import com.example.lb2.R;


public class MainActivity extends AppCompatActivity implements ReminderAdapter.OnDeleteClickListener {
    private static final int NOTIFICATION_PERMISSION_CODE = 1; // Код для запроса разрешения
    private EditText editTextTitle, editTextMessage;
    private Button buttonSetDate, buttonSetTime, buttonSave;
    private int hour, minute, year, month, day;
    private RecyclerView recyclerView;
    private ReminderAdapter reminderAdapter;
    private List<Reminder> reminderList;
    private DatabaseHelper dbHelper; // Добавляем переменную для работы с базой данных

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSetDate = findViewById(R.id.buttonSetDate);
        buttonSetTime = findViewById(R.id.buttonSetTime);
        buttonSave = findViewById(R.id.buttonSave);
        recyclerView = findViewById(R.id.recyclerView);

        dbHelper = new DatabaseHelper(this); // Инициализация базы данных
        reminderList = dbHelper.getAllReminders(); // Загружаем напоминания из базы данных
        reminderAdapter = new ReminderAdapter(this, reminderList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(reminderAdapter);

        // Запрос разрешения на уведомления
        requestNotificationPermission();

        buttonSetDate.setOnClickListener(v -> showDatePickerDialog());
        buttonSetTime.setOnClickListener(v -> showTimePickerDialog());
        buttonSave.setOnClickListener(v -> saveReminder());

        // Установка обработчика нажатия на кнопку
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Вызов метода при нажатии на кнопку
                onSetAlarmButtonClick(v);
            }
        });
    }


    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение на уведомления получено", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Разрешение на уведомления отклонено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedYear = year; // Сохраняем выбранный год
            selectedMonth = month; // Сохраняем выбранный месяц
            selectedDay = dayOfMonth; // Сохраняем выбранный день
            Toast.makeText(this, "Дата выбрана: " + selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear, Toast.LENGTH_SHORT).show();
        }, selectedYear, selectedMonth, selectedDay);

        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedHour = hourOfDay; // Сохраняем выбранный час
            selectedMinute = minute; // Сохраняем выбранную минуту
            Toast.makeText(this, "Время выбрано: " + selectedHour + ":" + String.format("%02d", selectedMinute), Toast.LENGTH_SHORT).show();
        }, selectedHour, selectedMinute, true);

        timePickerDialog.show();
    }

    private void saveReminder() {
        try {
            // Проверка на null для EditText
            if (editTextTitle == null || editTextMessage == null) {
                Toast.makeText(this, "Ошибка: поля ввода не инициализированы.", Toast.LENGTH_SHORT).show();
                return;
            }

            String title = editTextTitle.getText().toString().trim();
            String message = editTextMessage.getText().toString().trim();

            // Проверка на пустые поля
            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Установка времени для будильника
            Calendar calendar = Calendar.getInstance();
            calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0);
            long triggerTime = calendar.getTimeInMillis();

            Log.d("MainActivity", "Установлено время: " + calendar.getTime());

            // Сохраняем напоминание в базе данных
            Reminder newReminder = dbHelper.addReminder(title, message, triggerTime);
            if (newReminder != null) {
                reminderList.add(newReminder);
                // Сортировка списка по времени (измените Comparator, если дата хранится иначе)
                Collections.sort(reminderList, Comparator.comparingLong(r -> Long.parseLong(r.getDate().replace("-", "").replace(":", "").replace(" ", ""))));
                int index = reminderList.indexOf(newReminder);
                reminderAdapter.notifyItemInserted(index);
                Toast.makeText(this, "Напоминание установлено на " + calendar.getTime(), Toast.LENGTH_SHORT).show();
                setAlarm(title, message, triggerTime);
            } else {
                Toast.makeText(this, "Ошибка при сохранении напоминания. Проверьте данные и повторите попытку.", Toast.LENGTH_LONG).show();
                Log.e("MainActivity", "Ошибка при сохранении напоминания в базе данных");
            }

            // Очистка полей ввода
            editTextTitle.setText("");
            editTextMessage.setText("");

        } catch (Exception e) {
            Log.e("MainActivity", "Ошибка при сохранении напоминания", e);
            Toast.makeText(this, "Ошибка при сохранении напоминания: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



    public void onSetAlarmButtonClick(View view) {

        saveReminder(); // Теперь просто вызываем saveReminder, чтобы сохранить напоминание
    }

    private void setAlarm(String title, String message, long triggerTime) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("TITLE", title);
        intent.putExtra("MESSAGE", message);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Проверка разрешения на установку точных будильников
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                Toast.makeText(this, "Необходимо разрешение на установку точных будильников", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Для более ранних версий Android
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    @Override
    public void onDeleteClick(Reminder reminder) {
        // Удаляем напоминание из базы данных
        dbHelper.deleteReminder(reminder.getId()); // Убедитесь, что у Reminder есть метод getId()

        int position = reminderList.indexOf(reminder);
        reminderList.remove(reminder);
        reminderAdapter.notifyItemRemoved(position);
    }
}
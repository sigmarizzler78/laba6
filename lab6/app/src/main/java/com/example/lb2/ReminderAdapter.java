package com.example.lb2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lb2.R;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    private List<Reminder> reminderList;
    private Context context;
    private OnDeleteClickListener onDeleteClickListener;

    public ReminderAdapter(Context context, List<Reminder> reminderList, OnDeleteClickListener onDeleteClickListener) {
        this.context = context;
        this.reminderList = reminderList;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reminder_item, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminderList.get(position);
        holder.titleTextView.setText(reminder.getTitle());
        holder.messageTextView.setText(reminder.getMessage());

        // Устанавливаем обработчик нажатия на кнопку удаления
        holder.deleteButton.setOnClickListener(v -> {
            // Вызываем метод удаления через интерфейс
            onDeleteClickListener.onDeleteClick(reminder);
        });
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView messageTextView;
        Button deleteButton;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textViewTitle);
            messageTextView = itemView.findViewById(R.id.textViewMessage);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
        }
    }

    // Метод для добавления нового напоминания
    public void addReminder(Reminder reminder) {
        reminderList.add(reminder);
        notifyItemInserted(reminderList.size() - 1); // Уведомляем адаптер о добавлении нового элемента
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Reminder reminder);
    }
}
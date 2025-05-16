package com.example.eventlogin1;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentEventAdapter extends RecyclerView.Adapter<StudentEventAdapter.EventViewHolder> {
    private List<Event> events;
    private Context context;

    public StudentEventAdapter(List<Event> events, Context context) {
        this.events = events;
        this.context = context;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_item_student, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        // Parse the date string to set only the day and month separately
        try {
            String[] dateParts = event.getDate().split(" "); // Assuming date format like "15 OCT"
            holder.textViewDate.setText(dateParts[0]); // Display day
            holder.textViewMonth.setText(dateParts[1]); // Display month
        } catch (Exception e) {
            // Fallback if date parsing fails
            holder.textViewDate.setText(event.getDate());
            holder.textViewMonth.setText("");
        }

        holder.textViewTitle.setText(event.getTitle());
        holder.textViewTime.setText(event.getTime());

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddEvent.class);
            intent.putExtra("eventId", event.getEventId());
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (context instanceof AdminDashboard) {
                ((AdminDashboard) context).confirmDeleteEvent(event);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("title", event.getTitle());
            intent.putExtra("date", event.getDate());
            intent.putExtra("time", event.getTime());
            intent.putExtra("guestSpeaker", event.getGuestSpeaker());
            intent.putExtra("localimagepath", event.getLocalImagePath());
            intent.putExtra("description", event.getDescription());
            intent.putExtra("registerLink", event.getRegisterLink());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate;
        TextView textViewMonth;
        TextView textViewTitle;
        TextView textViewTime;
        ImageButton editButton;
        ImageButton deleteButton;

        EventViewHolder(View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewMonth = itemView.findViewById(R.id.textViewMonth);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
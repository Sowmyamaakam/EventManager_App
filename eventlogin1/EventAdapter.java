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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> events;
    private Context context;

    public EventAdapter(List<Event> events, Context context) {
        this.events = events;
        this.context = context;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        // Parse the date string (assuming the format is "6/11/2024")
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("d/MM/yyyy", Locale.ENGLISH);
            Date date = inputFormat.parse(event.getDate()); // Parse the date

            // Desired format: "November 6"
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d", Locale.ENGLISH);
            String formattedDate = outputFormat.format(date); // Format the date

            // Split into month and day
            String[] dateParts = formattedDate.split(" ");
            holder.textViewMonth.setText(dateParts[0]); // Display full month name (e.g., November)
            holder.textViewDate.setText(dateParts[1]); // Display the day (e.g., 6)

        } catch (Exception e) {
            // Fallback if date parsing fails
            holder.textViewDate.setText(event.getDate());
            holder.textViewMonth.setText("");
        }

        // Set title and time for the event
        holder.textViewTitle.setText(event.getTitle());
        holder.textViewTime.setText(event.getTime());

        // Check if context is StudentDashboard (or any student view) to hide edit/delete options
        if (context instanceof StudentDashboard) {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        } else {
            // Make sure to show these buttons for Admin/other users
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);

            // Set listeners for edit and delete actions
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
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("title", event.getTitle());
            intent.putExtra("date", event.getDate());
            intent.putExtra("time", event.getTime());
            intent.putExtra("guestSpeaker", event.getGuestSpeaker());
            intent.putExtra("description", event.getDescription());
            intent.putExtra("localimagepath",event.getLocalImagePath());
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

package com.example.eventlogin1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class RegisteredEventsAdapter extends RecyclerView.Adapter<RegisteredEventsAdapter.EventViewHolder> {

    private List<Registration> eventList;
    private Context context;

    public RegisteredEventsAdapter(Context context, List<Registration> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_registered_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Registration event = eventList.get(position);

        holder.eventTitleTextView.setText(event.getEventTitle());
        holder.eventDateTextView.setText(event.getSelectedDate() != null ? event.getSelectedDate() : "No date available");

        holder.unregisterButton.setOnClickListener(v -> {
            // Handle unregister logic here
            // For instance, remove the event from Firebase and update the list
            eventList.remove(position);
            notifyItemRemoved(position);
            Toast.makeText(context, "Unregistered from event.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView eventTitleTextView;
        TextView eventDateTextView;
        Button unregisterButton;

        public EventViewHolder(View itemView) {
            super(itemView);
            eventTitleTextView = itemView.findViewById(R.id.eventTitleTextView);
            eventDateTextView = itemView.findViewById(R.id.eventDateTextView);
            unregisterButton = itemView.findViewById(R.id.unregisterButton);
        }
    }
}

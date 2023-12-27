package com.example.taskandconsequence.views.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskandconsequence.R;
import com.example.taskandconsequence.model.ProgramOccurrence;
import com.example.taskandconsequence.databinding.ItemProgramOccurrenceBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProgramOccurrenceAdapter extends RecyclerView.Adapter<ProgramOccurrenceAdapter.ProgramOccurrenceViewHolder> {

    private List<ProgramOccurrence> programOccurrences = new ArrayList<>();
    private OnItemClickListener itemClickListener;

    private boolean active = false;
    // Constructor
    public ProgramOccurrenceAdapter(OnItemClickListener itemClickListener,boolean isActive) {
        this.itemClickListener = itemClickListener;
        this.active = isActive;
    }



    // Interface for click listener
    public interface OnItemClickListener {
        void onItemClick(ProgramOccurrence programOccurrence);
    }

    // Set new data to the adapter
    public void setProgramOccurrences(List<ProgramOccurrence> programOccurrences) {
        this.programOccurrences = programOccurrences;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProgramOccurrenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProgramOccurrenceBinding binding = ItemProgramOccurrenceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProgramOccurrenceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramOccurrenceViewHolder holder, int position) {
        ProgramOccurrence programOccurrence = programOccurrences.get(position);
        holder.bind(programOccurrence, itemClickListener, active);
    }

    @Override
    public int getItemCount() {
        return programOccurrences.size();
    }

    static class ProgramOccurrenceViewHolder extends RecyclerView.ViewHolder {
        private ItemProgramOccurrenceBinding binding;

        ProgramOccurrenceViewHolder(ItemProgramOccurrenceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }


        // Inside the ProgramOccurrenceViewHolder class
        void bind(ProgramOccurrence programOccurrence, OnItemClickListener clickListener, boolean active) {
            // Formatting the date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            if (active)
                binding.textViewStartDate.setText(programOccurrence.getProgram().getName());
            else
                binding.textViewStartDate.setText(dateFormat.format(programOccurrence.getDate()));

            binding.textViewStartTime.setText(timeFormat.format(programOccurrence.getDate()));

            // Setting the status text

            // Setting the drawable and color for the status icon
            int statusIconResId;
            int statusIconColor;
            switch (programOccurrence.getStatus()) {
                case PENDING_PUNISHMENT:
                    statusIconResId = R.drawable.ic_punishments; // Replace with actual drawable resource ID
                    statusIconColor = androidx.appcompat.R.attr.colorError; // Replace with actual color resource ID
                    break;
                case SUCCEED:
                    statusIconResId = R.drawable.baseline_check_circle_outline_24; // Replace with actual drawable resource ID
                    statusIconColor = com.google.android.material.R.attr.colorSecondary; // Replace with actual color resource ID
                    break;
                case FAIL:
                    statusIconResId = R.drawable.baseline_close_24; // Replace with actual drawable resource ID
                    statusIconColor = com.google.android.material.R.attr.colorError; // Replace with actual color resource ID
                    break;
                case SUCCEED_PUNISHMENT:
                    statusIconResId = R.drawable.baseline_done_24; // Replace with actual drawable resource ID
                    statusIconColor = com.google.android.material.R.attr.colorSecondary; // Replace with actual color resource ID
                    break;
                default:
                    statusIconResId = R.drawable.baseline_pending_24; // Replace with actual drawable resource ID
                    statusIconColor = com.google.android.material.R.attr.colorPrimary; // Replace with actual color resource ID
                    break;
            }
            binding.statusIcon.setImageResource(statusIconResId);
//            binding.statusIcon.setColorFilter(ContextCompat.getColor(com.google.android.material.R.attr.colorPrimarySurface));
            int color = getColorFromAttr(statusIconColor);
            binding.statusIcon.setColorFilter(color);

            // Setting the reward points
            binding.textViewRewardPoints.setText("Reward Points: "+String.valueOf(programOccurrence.getRewards()));

            // Setting the click listener
            itemView.setOnClickListener(v -> clickListener.onItemClick(programOccurrence));
        }

        private int getColorFromAttr(int statusIconColor) {
            TypedValue typedValue = new TypedValue();
            Context context = itemView.getContext();
            context.getTheme().resolveAttribute(statusIconColor, typedValue, true);
            int colorRes = typedValue.resourceId;
            int color = ContextCompat.getColor(context,colorRes);
            return color;
        }
    }
}

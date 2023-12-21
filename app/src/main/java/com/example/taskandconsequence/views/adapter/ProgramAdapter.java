package com.example.taskandconsequence.views.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskandconsequence.R;
import com.example.taskandconsequence.databinding.ItemProgramBinding;
import com.example.taskandconsequence.model.Program;
import com.example.taskandconsequence.model.Task;
import com.google.android.material.color.MaterialColors;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ProgramAdapter extends RecyclerView.Adapter<ProgramAdapter.ProgramViewHolder> {

    private List<Program> programs;
    private Set<Long> selectedItems;

    public interface OnItemClickListener {
        void onItemClick(Program program);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Program program);
    }

    private OnItemClickListener itemClickListener;
    private OnItemLongClickListener itemLongClickListener;

    public ProgramAdapter(List<Program> programs, OnItemClickListener itemClickListener, OnItemLongClickListener itemLongClickListener, Set<Long> selectedItems) {
        this.programs = programs;
        this.itemClickListener = itemClickListener;
        this.selectedItems = selectedItems;
        this.itemLongClickListener = itemLongClickListener;
    }

    public void setPrograms(List<Program> programs){
        this.programs = programs;
        notifyDataSetChanged();
    }
    public List<Program> getProgram(){
        return this.programs;
    }

    @NonNull
    @Override
    public ProgramViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProgramBinding binding = ItemProgramBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProgramViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramViewHolder holder, int position) {
        Program program = programs.get(position);
        boolean isSelected = selectedItems.contains(program.getId());
        holder.bind(program, itemClickListener, itemLongClickListener, isSelected);
    }

    @Override
    public int getItemCount() {
        return programs.size();
    }

    static class ProgramViewHolder extends RecyclerView.ViewHolder {
        private ItemProgramBinding binding;

        ProgramViewHolder(ItemProgramBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Program program, OnItemClickListener clickListener, OnItemLongClickListener longClickListener, boolean isSelected) {
            binding.tvProgramName.setText(program.getName());
            binding.tvFrequency.setText("Frequency: " + program.getFrequency());

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            binding.tvStartDate.setText("Start Date: " + dateFormat.format(program.getStartDate()));

            binding.tvNumberOfPeriods.setText("Periods: " + program.getNumberOfPeriods());
            binding.tvTaskCount.setText("Tasks: " + (program.getTasks() != null ? program.getTasks().size() : 0));

            binding.tvRewardPoints.setText("Rewards: " + program.getRewards());

            itemView.setOnClickListener(v -> clickListener.onItemClick(program));
            itemView.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(program);
                return true;
            });

            // Set selected or default background color
            binding.itemProgramLayout.setBackgroundColor(isSelected ?
                    ContextCompat.getColor(itemView.getContext(), R.color.selectedItemBackground) :
                    MaterialColors.getColor(itemView.getContext(), com.google.android.material.R.attr.colorSurface, Color.WHITE) );
        }
    }
}

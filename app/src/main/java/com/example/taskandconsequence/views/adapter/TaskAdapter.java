package com.example.taskandconsequence.views.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.taskandconsequence.R;
import com.example.taskandconsequence.databinding.ItemTaskBinding;
import com.example.taskandconsequence.model.Task;
import com.google.android.material.color.MaterialColors;
import java.util.List;
import java.util.Set;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;
    private OnItemClickListener itemClickListener;
    private OnItemLongClickListener itemLongClickListener;
    private OnDeleteIconClickListener onDeleteIconClickListener;

    private boolean showDeleteIcon;
    private Set<Long> selectedItems;

    public interface OnItemClickListener {
        void onItemClick(Task task);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Task task);
    }
    public interface OnDeleteIconClickListener {
        void OnDeleteIconClick(Task task);
    }

    public TaskAdapter(List<Task> tasks, OnItemClickListener itemClickListener, OnItemLongClickListener itemLongClickListener,
                       Set<Long> selectedItems, OnDeleteIconClickListener onDeleteIconClickListener,
                       boolean showDeleteIcon) {
        this.tasks = tasks;
        this.itemClickListener = itemClickListener;
        this.itemLongClickListener = itemLongClickListener;
        this.selectedItems = selectedItems;
        this.onDeleteIconClickListener = onDeleteIconClickListener;
        this.showDeleteIcon = showDeleteIcon;
    }

    public void setTasks(List<Task> tasks){
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        boolean isSelected = selectedItems.contains(task.getId());
        holder.bind(task, itemClickListener, itemLongClickListener, isSelected, onDeleteIconClickListener,showDeleteIcon);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private ItemTaskBinding binding;

        TaskViewHolder(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Task task, OnItemClickListener clickListener, OnItemLongClickListener longClickListener, boolean isSelected, OnDeleteIconClickListener onDeleteIconClickListener, boolean showDeleteIcon) {
            binding.tvTaskName.setText(task.getName());
            binding.tvTaskDescription.setText(task.getDescription());
            binding.tvRewardPoints.setText("Rewards Points: " + String.valueOf(task.getRewards()));
            itemView.setOnClickListener(v -> clickListener.onItemClick(task));
            itemView.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(task);
                return true;
            });

            int backgroundColor = isSelected ? ContextCompat.getColor(itemView.getContext(), R.color.selectedItemBackground) : MaterialColors.getColor(itemView.getContext(), com.google.android.material.R.attr.colorSurface, Color.WHITE);
            int textColor = isSelected ? ContextCompat.getColor(itemView.getContext(), R.color.selectedItemText) : MaterialColors.getColor(itemView.getContext(), com.google.android.material.R.attr.colorOnSurface, Color.BLACK);

            binding.taskItemContainer.setBackgroundColor(backgroundColor);
            binding.tvTaskDescription.setTextColor(textColor);
            binding.tvRewardPoints.setTextColor(textColor);

            if ( showDeleteIcon){
                binding.deleteTaskIcon.setVisibility(View.VISIBLE);
                binding.deleteTaskIcon.setOnClickListener(view -> {
                    onDeleteIconClickListener.OnDeleteIconClick(task);

                });
            }else{
                binding.deleteTaskIcon.setVisibility(View.GONE);

            }

        }
    }
}

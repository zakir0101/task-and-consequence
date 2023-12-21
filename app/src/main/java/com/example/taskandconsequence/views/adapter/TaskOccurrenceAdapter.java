package com.example.taskandconsequence.views.adapter;

import static com.example.taskandconsequence.model.Status.*;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskandconsequence.Helper;
import com.example.taskandconsequence.R;
import com.example.taskandconsequence.databinding.ItemTaskOccurrence2Binding;
import com.example.taskandconsequence.model.Status;
import com.example.taskandconsequence.model.Task;
import com.example.taskandconsequence.model.TaskOccurrence;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskOccurrenceAdapter extends RecyclerView.Adapter<TaskOccurrenceAdapter.TaskOccurrenceViewHolder> {

    private final OnGetProgramFrequencyListener getProgramFrequencyListener ;
    private  OnGetTaskListener onGetTaskListener;
    private List<TaskOccurrence> taskOccurrences  = new ArrayList<>();
    private OnStatusChangeListener statusChangeListener;

    public TaskOccurrenceAdapter(OnStatusChangeListener statusChangeListener,
                                 OnGetTaskListener onGetTaskListener,
                                 OnGetProgramFrequencyListener getProgramFrequencyListener) {
        this.statusChangeListener = statusChangeListener;
        this.onGetTaskListener = onGetTaskListener;
        this.getProgramFrequencyListener = getProgramFrequencyListener ;
    }

    public void setTaskOccurrences(List<TaskOccurrence> taskOccurrences) {
        this.taskOccurrences = taskOccurrences;

    }

    @NonNull
    @Override
    public TaskOccurrenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskOccurrence2Binding binding = ItemTaskOccurrence2Binding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TaskOccurrenceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskOccurrenceViewHolder holder, int position) {
        TaskOccurrence taskOccurrence = taskOccurrences.get(position);
        holder.bind(taskOccurrence);
    }

    @Override
    public int getItemCount() {
        return taskOccurrences.size();
    }

    public interface OnStatusChangeListener {
        void onStatusChanged(TaskOccurrence taskOccurrence, Status newStatus);
    }
    public interface OnGetTaskListener {
        void getTaskById(Long taskId,OnUpdateViewHolderListener onUpdateViewHolderListener);
    }
     public interface OnUpdateViewHolderListener {
        void onUpdate(Task task );
        }

    public interface OnGetProgramFrequencyListener {
        String getFrequency();
    }

    class TaskOccurrenceViewHolder extends RecyclerView.ViewHolder {

        private ItemTaskOccurrence2Binding binding;
        private Status currentStatus;
        TaskOccurrenceViewHolder(ItemTaskOccurrence2Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TaskOccurrence taskOccurrence) {
            // Setting the drawable and color for the status icon
            int statusIconResId;
            int statusIconColor;
            switch (taskOccurrence.getStatus()) {
                case SUCCEED:
                    statusIconResId = R.drawable.baseline_check_circle_outline_24; // Replace with actual drawable resource ID
                    statusIconColor = com.google.android.material.R.attr.colorSecondary; // Replace with actual color resource ID
                    break;
                case FAIL:
                    statusIconResId = R.drawable.baseline_close_24; // Replace with actual drawable resource ID
                    statusIconColor = com.google.android.material.R.attr.colorError; // Replace with actual color resource ID
                    break;
                default:
                    statusIconResId = R.drawable.baseline_pending_24; // Replace with actual drawable resource ID
                    statusIconColor = com.google.android.material.R.attr.colorPrimary; // Replace with actual color resource ID
                    break;
            }
            binding.imageViewStatusIndicator.setImageResource(statusIconResId);
            int color = getColorFromAttr(statusIconColor);
            binding.imageViewStatusIndicator.setColorFilter(color);

            // Set other fields like deadline, reward points, etc.
            this.currentStatus = taskOccurrence.getStatus();
            onGetTaskListener.getTaskById(taskOccurrence.getTaskId(),this::updateDescriptionAndReward);
            // Assuming you have a method to get the deadline and reward points
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            int numOfDays = Helper. getNumOfDays( getProgramFrequencyListener.getFrequency());

            binding.textViewDeadline.setText("Deadline: "+ dateFormat.format(Helper.addDays(taskOccurrence.getDate(), numOfDays  )));


            // Set initial state of MaterialButtonToggleGroup based on TaskOccurrence status
            binding.toggleGroupStatus.clearOnButtonCheckedListeners(); // Clear old listeners

            int buttonIdToCheck = getButtonIdFromStatus(taskOccurrence.getStatus());

            if (buttonIdToCheck != -1) {
                binding.toggleGroupStatus.check(buttonIdToCheck);
            }

            binding.toggleGroupStatus.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    Status newStatus = getStatusFromCheckedId(checkedId);
                    if(newStatus != taskOccurrence.getStatus())
                        statusChangeListener.onStatusChanged(taskOccurrence, newStatus);
                }
            });

        }

        private void updateDescriptionAndReward(Task task){
                binding.textViewTaskDescription.setText(task.getDescription());
                binding.textViewTaskName.setText(task.getName());
                if (this.currentStatus == SUCCEED)
                    binding.textViewRewardPoints.setText("Reward Points: " + String.valueOf(task.getRewards()));
                else
                    binding.textViewRewardPoints.setText("Reward Points: " + String.valueOf(0));
        }
        private int getColorFromAttr(int statusIconColor) {
            TypedValue typedValue = new TypedValue();
            Context context = itemView.getContext();
            context.getTheme().resolveAttribute(statusIconColor, typedValue, true);
            int colorRes = typedValue.resourceId;
            int color = ContextCompat.getColor(context,colorRes);
            return color;
        }

        private int getButtonIdFromStatus(Status status) {
            switch (status) {
                case SUCCEED:
                    return R.id.buttonSucceed;
                case FAIL:
                    return R.id.buttonFail;
                case PENDING:
                    return R.id.buttonPending;
                default:
                    return -1; // No button to check for other statuses
            }
        }

        private Status getStatusFromCheckedId(int checkedId) {
            switch (checkedId) {
                case R.id.buttonSucceed:
                    return Status.SUCCEED;
                case R.id.buttonFail:
                    return Status.FAIL;
                case R.id.buttonPending:
                default:
                    return Status.PENDING; // Default case
            }
        }

    }
}

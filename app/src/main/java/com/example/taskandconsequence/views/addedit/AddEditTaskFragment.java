package com.example.taskandconsequence.views.addedit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.taskandconsequence.MainActivity;
import com.example.taskandconsequence.R;

import com.example.taskandconsequence.viewmodel.SharedViewModel;

// ... [Other import statements]

import com.example.taskandconsequence.databinding.FragmentAddEditTaskBinding;
import com.example.taskandconsequence.model.Task;

import java.util.Random;

public class AddEditTaskFragment extends Fragment {

    private FragmentAddEditTaskBinding binding;
    private SharedViewModel sharedViewModel;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddEditTaskBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        navController = NavHostFragment.findNavController(this);

        setupUI();
        setupEventHandlers();

        return binding.getRoot();
    }

    private void setupUI() {
        // Check if in edit mode and populate fields
        if (sharedViewModel.taskEditMode && sharedViewModel.activeTask instanceof Task) {
            Task activeTask = (Task) sharedViewModel.activeTask;
            binding.nameEditText.setText(activeTask.getName());
            binding.descriptionEditText.setText(activeTask.getDescription());
            binding.rewardEditText.setText(String.valueOf(activeTask.getRewards()));
            getActivity().setTitle("Edit Task");
        }else{
            getActivity().setTitle("Add Task");
        }
    }

    private void setupEventHandlers() {
        binding.saveButton.setOnClickListener(v -> saveTask());
        binding.cancelButton.setOnClickListener(v -> cancelEditing());
    }

    private void saveTask() {
        String name = binding.nameEditText.getText().toString();
        String description = binding.descriptionEditText.getText().toString();
        String rewardStr = binding.rewardEditText.getText().toString();
        int reward = 0;
        boolean isValid = true;

        // Validate name
        if (name.isEmpty()) {
            binding.nameInputLayout.setError("Name cannot be empty");
            isValid = false;
        } else {
            binding.nameInputLayout.setError(null);
        }    // Validate description
        if (description.isEmpty()) {
            binding.descriptionInputLayout.setError("Description cannot be empty");
            isValid = false;
        } else {
            binding.descriptionInputLayout.setError(null);
        }

        // Validate reward
        try {
            reward = Integer.parseInt(rewardStr);
            if (reward < 0) {
                throw new NumberFormatException("Reward can't be negative");
            }
            binding.rewardInputLayout.setError(null);
        } catch (NumberFormatException e) {
            binding.rewardInputLayout.setError("Invalid reward");
            isValid = false;
        }

        // Proceed only if all validations pass
        if (isValid) {
            Task task = sharedViewModel.taskEditMode ? (Task) sharedViewModel.activeTask : new Task();
            task.setName(name);
            task.setDescription(description);
            task.setRewards(reward);
            task.setProgramId(-1L);
            if (!sharedViewModel.programMode) {
                if (sharedViewModel.taskEditMode)
                    sharedViewModel.updateTask(task);
                else
                    sharedViewModel.addTask(task);

            }else {
                if(!sharedViewModel.taskEditMode) {
                    sharedViewModel.addProgramTasks(task);
                }
                else
                    sharedViewModel.addProgramTasks(task);
            }
            navController.navigateUp();
        }
    }

    private void cancelEditing() {
        navController.navigateUp();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getBottomNavigationView().setVisibility(View.GONE);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

    }
    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) getActivity()).getBottomNavigationView().setVisibility(View.VISIBLE);
    }
}

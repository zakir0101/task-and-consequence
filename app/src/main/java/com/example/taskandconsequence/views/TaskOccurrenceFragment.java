package com.example.taskandconsequence.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.taskandconsequence.MainActivity;
import com.example.taskandconsequence.R;
import com.example.taskandconsequence.databinding.FragmentTaskOccurrenceBinding;
import com.example.taskandconsequence.model.Program;
import com.example.taskandconsequence.model.ProgramOccurrence;
import com.example.taskandconsequence.model.Status;
import com.example.taskandconsequence.model.Task;
import com.example.taskandconsequence.model.TaskOccurrence;
import com.example.taskandconsequence.viewmodel.SharedViewModel;
import com.example.taskandconsequence.views.adapter.TaskOccurrenceAdapter;

import java.util.List;

public class TaskOccurrenceFragment extends Fragment {

    private FragmentTaskOccurrenceBinding binding;
    private SharedViewModel viewModel;
    private TaskOccurrenceAdapter adapter;
    private NavController navController;

    private ProgramOccurrence activeProgramOccurrence;
    private boolean isInitialCall = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTaskOccurrenceBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        navController = NavHostFragment.findNavController(this);
        Program p = (Program) viewModel.activeObject;
        getActivity().setTitle(p.getName());

        setupRecyclerView();
//        setupButtons();
        setupPunishmentStatusToggleGroup();
        observeTaskOccurrences();

        return binding.getRoot();
    }
    private void setupButtons() {
        // Show 'Mark as Succeed' button only if all occurrences have status SUCCEED or SUCCEED_PUNISHMENT
        binding.buttonMarkProgramOccurrenceSucceed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                markProgramOccurrenceAsSucceed(checked);
            }
        } );


    }

    private void markProgramOccurrenceAsSucceed(boolean succeed) {
        if (succeed){
            activeProgramOccurrence.setStatus(Status.SUCCEED);
        }else{
            activeProgramOccurrence.setStatus(Status.PENDING);
        }
        viewModel.updateProgramOccurrence(activeProgramOccurrence);
        // Update UI or navigate as needed
    }


    private void setupPunishmentStatusToggleGroup() {

        // Add listener to handle status changes
        binding.toggleGroupPunishmentStatus.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                Status newStatus = getStatusFromCheckedId(checkedId);
                activeProgramOccurrence.setStatus(newStatus);
                viewModel.updateProgramOccurrenceOnly(activeProgramOccurrence);
                // following code should only run if the the call was initiated by the UI ( by the user )
                if (!isInitialCall) {
                    if (newStatus != Status.PENDING) {
                        navController.navigateUp();
                    }
                } else {
                    isInitialCall = false;
                }
            }
        });
    }

    private int getStatusButtonId(Status status) {
        switch (status) {
            case SUCCEED_PUNISHMENT:
                return R.id.buttonSucceed;
            case FAIL:
                return R.id.buttonFail;
            case PENDING_PUNISHMENT:
            default:
                return R.id.buttonPending;
        }
    }

    private Status getStatusFromCheckedId(int checkedId) {
        switch (checkedId) {
            case R.id.buttonSucceed:
                return Status.SUCCEED_PUNISHMENT;
            case R.id.buttonFail:
                return Status.FAIL;
            case R.id.buttonPending:
                return Status.PENDING_PUNISHMENT;
            default:
                return Status.PENDING;
        }
    }
    private void setupRecyclerView() {
        adapter = new TaskOccurrenceAdapter(this::onTaskStatusChange,this::getTaskById,this::getProgramFrequency);
        binding.recyclerViewTaskOccurrences.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewTaskOccurrences.setAdapter(adapter);
    }



    private void observeTaskOccurrences() {
        // Assuming activeProgramOccurrence is a LiveData in SharedViewModel
        viewModel.getActiveOccurrence().observe(getViewLifecycleOwner(), programOccurrence -> {
            activeProgramOccurrence = programOccurrence;
            List<TaskOccurrence> taskOccurrences = programOccurrence.getTaskOccurrences();
            adapter.setTaskOccurrences(taskOccurrences);
            adapter.notifyDataSetChanged();
            updateButtonVisibility( taskOccurrences);
        });
    }

    private void updateButtonVisibility(List<TaskOccurrence> occurrences) {
        boolean allSucceed = true;
        boolean anyFail = false;

        for (TaskOccurrence occurrence : occurrences) {
            if (occurrence.getStatus() != Status.SUCCEED &&
                    occurrence.getStatus() != Status.SUCCEED_PUNISHMENT) {
                allSucceed = false;
            }
            if (occurrence.getStatus() == Status.FAIL) {
                anyFail = true;
            }
        }

        binding.buttonMarkProgramOccurrenceSucceed.setVisibility(View.GONE);
        if (allSucceed){
            activeProgramOccurrence.setStatus(Status.SUCCEED);
            viewModel.updateProgramOccurrenceOnly(activeProgramOccurrence);
        }else if (!anyFail){
            activeProgramOccurrence.setStatus(Status.PENDING);
            viewModel.updateProgramOccurrenceOnly(activeProgramOccurrence);

        }
        updatePunishmentSection(anyFail);
    }


    private void updatePunishmentSection(boolean hasFailedTasks) {
        // Check if there are any failed tasks

        if (hasFailedTasks) {
            // Show the punishment section

            binding.punishmentSection.setVisibility(View.VISIBLE);

            // Get the punishment details from the active program
            Program activeProgram = (Program) viewModel.activeObject;
            binding.textViewPunishmentName.setText(activeProgram.getSmallPunishment().getName());
            binding.textViewPunishmentDescription.setText(activeProgram.getSmallPunishment().getDescription());
            // Set the initial state based on the current punishment status
            if (activeProgramOccurrence.getStatus() != Status.PENDING_PUNISHMENT &&
                    activeProgramOccurrence.getStatus() != Status.SUCCEED_PUNISHMENT &&
                    activeProgramOccurrence.getStatus() != Status.FAIL) {
                activeProgramOccurrence.setStatus(Status.PENDING_PUNISHMENT);
                viewModel.updateProgramOccurrenceOnly(activeProgramOccurrence);
            }

            int buttonId = getStatusButtonId(activeProgramOccurrence.getStatus());
            isInitialCall = true;
            binding.toggleGroupPunishmentStatus.check(buttonId);

            // Set up the MaterialButtonToggleGroup for the punishment status
        } else {
            // Hide the punishment section
            if (activeProgramOccurrence.getStatus() != Status.PENDING && activeProgramOccurrence.getStatus() != Status.SUCCEED) {
                activeProgramOccurrence.setStatus(Status.PENDING);
                viewModel.updateProgramOccurrenceOnly(activeProgramOccurrence);
            }
            binding.punishmentSection.setVisibility(View.GONE);
        }
    }

    private void onTaskStatusChange(TaskOccurrence taskOccurrence, Status newStatus) {
        // Handle task occurrence item click
        // ...
        taskOccurrence.setStatus(newStatus);
        viewModel.updateProgramOccurrence(activeProgramOccurrence);

    }

    private void getTaskById(Long taskId ,TaskOccurrenceAdapter.OnUpdateViewHolderListener onUpdateViewHolderListener){
         viewModel.getTask(taskId).observe(getViewLifecycleOwner(),task -> {
                if (task != null )
             onUpdateViewHolderListener.onUpdate(task);
        });
    }

    private String getProgramFrequency(){
        Program p = (Program) viewModel.activeObject;
        return p.getFrequency();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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


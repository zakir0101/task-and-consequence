package com.example.taskandconsequence.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.taskandconsequence.MainActivity;
import com.example.taskandconsequence.R;
import com.example.taskandconsequence.databinding.FragmentProgramOccurrenceBinding;
import com.example.taskandconsequence.model.Program;
import com.example.taskandconsequence.model.Status;
import com.example.taskandconsequence.viewmodel.SharedViewModel;
import com.example.taskandconsequence.model.ProgramOccurrence;
import com.example.taskandconsequence.views.adapter.ProgramOccurrenceAdapter;

import java.util.List;

public class ProgramOccurrenceFragment extends Fragment {

    private FragmentProgramOccurrenceBinding binding;
    private SharedViewModel viewModel;
    private ProgramOccurrenceAdapter adapter;
    private NavController navController;
    private Program activeProgram;
    private boolean isInitialCall = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProgramOccurrenceBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        navController = NavHostFragment.findNavController(this);
        Program p = (Program) viewModel.activeObject;
        getActivity().setTitle(p.getName());
        setupRecyclerView();
        setupButtons();
        observeProgramOccurrences();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new ProgramOccurrenceAdapter(this::onProgramOccurrenceClick);
        binding.recyclerViewProgramOccurrences.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewProgramOccurrences.setAdapter(adapter);
    }

    private void setupButtons() {
        // Show 'Mark as Succeed' button only if all occurrences have status SUCCEED or SUCCEED_PUNISHMENT
        binding.btnMarkAsSucceed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                markProgramAsSucceed(checked);
            }
        });

        // Punishment section visibility and button setup
        binding.togglePunishmentStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                markPunishmentAsDone(checked);
            }
        });
    }

    private void updateButtonVisibility(List<ProgramOccurrence> occurrences) {
        boolean allSucceed = true;
        boolean anyFail = false;

        for (ProgramOccurrence occurrence : occurrences) {
            if (occurrence.getStatus() != Status.SUCCEED &&
                    occurrence.getStatus() != Status.SUCCEED_PUNISHMENT) {
                allSucceed = false;
            }
            if (occurrence.getStatus() == Status.FAIL) {
                anyFail = true;
            }
        }

        binding.btnMarkAsSucceed.setVisibility(View.GONE);
        if (allSucceed) {
            activeProgram.setStatus(Status.SUCCEED);
            viewModel.updateProgramOnly(activeProgram);
        } else if (!anyFail) {
            activeProgram.setStatus(Status.PENDING);
            viewModel.updateProgramOnly(activeProgram);

        }
        if (anyFail) {
            binding.punishmentSection.setVisibility(View.VISIBLE);
            if (activeProgram.getStatus() != Status.PENDING_PUNISHMENT &&
                    activeProgram.getStatus() != Status.FAIL) {
                activeProgram.setStatus(Status.PENDING_PUNISHMENT);
                viewModel.updateProgramOnly(activeProgram);
            }

            updatePunishmentSection();
        } else {
            binding.punishmentSection.setVisibility(View.GONE);

            if (activeProgram.getStatus() != Status.PENDING && activeProgram.getStatus() != Status.SUCCEED) {
                activeProgram.setStatus(Status.PENDING);
                viewModel.updateProgramOnly(activeProgram);
            }
        }
    }

    private void updatePunishmentSection() {
        Program activeObject = (Program) viewModel.activeObject;
        binding.tvBigPunishmentName.setText(activeObject.getBigPunishment().getName());
        binding.tvBigPunishmentDescription.setText(activeObject.getBigPunishment().getDescription());
        boolean newIsChecked = activeProgram.getStatus() == Status.FAIL ;
        if (newIsChecked != binding.togglePunishmentStatus.isChecked())
            isInitialCall = true;
        else
            isInitialCall = false;
        binding.togglePunishmentStatus.setChecked(newIsChecked);
    }

    private void markProgramAsSucceed(boolean succeed) {
        // Logic to mark the active program as succeeded
        if (succeed) {
            activeProgram.setStatus(Status.SUCCEED);
        } else {
            activeProgram.setStatus(Status.PENDING);
        }
        viewModel.updateProgram(activeProgram);
        // Update UI or navigate as needed
    }

    private void markPunishmentAsDone(boolean isPunishmentDone) {
        // Logic to mark the active program's punishment as done
        if (isPunishmentDone) {
            activeProgram.setStatus(Status.FAIL);
            if (isInitialCall) {
                isInitialCall = false;
            } else {
                navController.navigateUp();
            }
        } else {
            activeProgram.setStatus(Status.PENDING_PUNISHMENT);
            if (isInitialCall) {
                isInitialCall = false;
            }
        }
        viewModel.updateProgramOnly(activeProgram);

    }


    private void observeProgramOccurrences() {
        // Assuming activeProgram is a LiveData in SharedViewModel
        Program p = (Program) viewModel.activeObject;

        viewModel.getActiveProgram(p.getId()).observe(getViewLifecycleOwner(), active -> {
            activeProgram = active;

            List<ProgramOccurrence> occurrences = active.getProgramOccurrences();
            adapter.setProgramOccurrences(occurrences);

            // Update the visibility and functionality of buttons based on the statuses of occurrences
            updateButtonVisibility(occurrences);
        });
    }


    // Additional methods for button click actions, etc.

    private void onProgramOccurrenceClick(ProgramOccurrence programOccurrence) {
        viewModel.activeOccurrenceId = programOccurrence.getId();
        navController.navigate(R.id.action_programOccurrenceFragment_to_taskOccurrenceFragment);
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

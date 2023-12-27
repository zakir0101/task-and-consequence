package com.example.taskandconsequence.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.taskandconsequence.MainActivity;
import com.example.taskandconsequence.R;
import com.example.taskandconsequence.databinding.FragmentProgramsBinding;
import com.example.taskandconsequence.model.Backup;
import com.example.taskandconsequence.model.Program;
import com.example.taskandconsequence.viewmodel.SharedViewModel;
import com.example.taskandconsequence.views.adapter.ProgramAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ProgramsFragment extends Fragment {

    private FragmentProgramsBinding binding;
    private ProgramAdapter activeAdapter, pendingPunishmentAdapter, succeedAdapter, failedAdapter;

    private SharedViewModel viewModel;
    private NavController navController;
    private List<Program> programs;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProgramsBinding.inflate(inflater, container, false);
        navController = NavHostFragment.findNavController(this);
        setHasOptionsMenu(true);
        setupTitle();
        setupRecyclerViews();
        setupFloatingActionButton();

        return binding.getRoot();
    }

    private void setupTitle() {
        requireActivity().setTitle("Programs");
//        viewModel.getAllReward().observe(getViewLifecycleOwner(),reward -> {
//            requireActivity().setTitle(String.format("Programs [ %d ]", reward));
//
//        });
    }

    private void setupRecyclerViews() {
        activeAdapter = new ProgramAdapter(new ArrayList<>(), this::onProgramClick, this::onProgramLongClick, viewModel.selectedItems);
        pendingPunishmentAdapter = new ProgramAdapter(new ArrayList<>(), this::onProgramClick, this::onProgramLongClick, viewModel.selectedItems);
        succeedAdapter = new ProgramAdapter(new ArrayList<>(), this::onProgramClick, this::onProgramLongClick, viewModel.selectedItems);
        failedAdapter = new ProgramAdapter(new ArrayList<>(), this::onProgramClick, this::onProgramLongClick, viewModel.selectedItems);

        setupRecyclerView(binding.rvActivePrograms, activeAdapter);
        setupRecyclerView(binding.rvPendingPunishmentPrograms, pendingPunishmentAdapter);
        setupRecyclerView(binding.rvSucceedPrograms, succeedAdapter);
        setupRecyclerView(binding.rvFailedPrograms, failedAdapter);
        viewModel.getAllPrograms().observe(getViewLifecycleOwner(), programs -> {
            this.programs = programs;
            viewModel.isSelectionModeActive = false;
            viewModel.selectedItems.clear();
            requireActivity().invalidateOptionsMenu();
            List<Program> activePrograms = new ArrayList<>();
            List<Program> pendingPunishmentPrograms = new ArrayList<>();
            List<Program> succeedPrograms = new ArrayList<>();
            List<Program> failedPrograms = new ArrayList<>();

            for (Program program : programs) {
                switch (program.getStatus()) {
                    case PENDING:
                        activePrograms.add(program);
                        break;
                    case PENDING_PUNISHMENT:
                        pendingPunishmentPrograms.add(program);
                        break;
                    case SUCCEED:
                        succeedPrograms.add(program);
                        break;
                    case FAIL:
                        failedPrograms.add(program);
                        break;
                }
            }
            binding.textActivePrograms.setVisibility(activePrograms.size() == 0 ? View.GONE : View.VISIBLE);
            binding.textPendingPunishmentPrograms.setVisibility(pendingPunishmentPrograms.size() == 0 ? View.GONE : View.VISIBLE);
            binding.textSucceedPrograms.setVisibility(succeedPrograms.size() == 0 ? View.GONE : View.VISIBLE);
            binding.textFailedPrograms.setVisibility(failedPrograms.size() == 0 ? View.GONE : View.VISIBLE);
            activeAdapter.setPrograms(activePrograms);
            pendingPunishmentAdapter.setPrograms(pendingPunishmentPrograms);
            succeedAdapter.setPrograms(succeedPrograms);
            failedAdapter.setPrograms(failedPrograms);
        });
    }

    private void setupRecyclerView(RecyclerView recyclerView, ProgramAdapter adapter) {
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    private void setupFloatingActionButton() {
        binding.fabAddProgram.setOnClickListener(view -> {
            viewModel.editMode = false;
            setSelectionMode(false);
            viewModel.clearProgramTasks();
            navController.navigate(R.id.action_programsFragment_to_addEditProgramFragment);
        });
    }

    private void onProgramClick(Program program) {
        if (viewModel.isSelectionModeActive) {
            Set<Long> selectedItems = viewModel.selectedItems;
            // Toggle selection
            if (selectedItems.contains(program.getId())) {
                selectedItems.remove(program.getId());
            } else {
                selectedItems.add(program.getId());
            }
            if (selectedItems.isEmpty()) {
                setSelectionMode(false);
                // Update UI accordingly
            }
            requireActivity().invalidateOptionsMenu();
            notifyDataSetChanged();
        } else {
            // navigate to ProgramOccurrenceFragment
            viewModel.activeObject = program;
            setSelectionMode(false);
            navController.navigate(R.id.action_programsFragment_to_programOccurrenceFragment);
        }
    }

    private void onProgramLongClick(Program program) {
        Set<Long> selectedItems = viewModel.selectedItems;
        // Toggle selection
        if (viewModel.isSelectionModeActive) {
            onProgramClick(program);
        } else {
            setSelectionMode(true);
            viewModel.selectedItems.add(program.getId());
            viewModel.activeObject = program;
            notifyDataSetChanged();
        }


    }

    private void notifyDataSetChanged() {
        activeAdapter.notifyDataSetChanged();
        pendingPunishmentAdapter.notifyDataSetChanged();
        succeedAdapter.notifyDataSetChanged();
        failedAdapter.notifyDataSetChanged();
    }

    private void setSelectionMode(boolean isSelectionModeActive) {
        if (viewModel.isSelectionModeActive != isSelectionModeActive)
            this.requireActivity().invalidateOptionsMenu();

        viewModel.isSelectionModeActive = isSelectionModeActive;
        viewModel.selectedItems.clear();

    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);

    }

    // Add any additional required methods...

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_select_all:
                viewModel.selectedItems.clear();
                for (Program program : programs) {
                    viewModel.selectedItems.add(program.getId());
                }
                notifyDataSetChanged();
                requireActivity().invalidateOptionsMenu();
                return true;
            // Handle other items
            case R.id.menu_item_edit:
                viewModel.editMode = true;
                viewModel.clearProgramTasks();
                setSelectionMode(false);
                navController.navigate(R.id.action_programsFragment_to_addEditProgramFragment);
                return true;
            case R.id.menu_item_export_selected:
                onExportSelected(false);
                return true;
            case R.id.menu_item_share_selected:
                onExportSelected(true);
                return true;

            case R.id.menu_item_delete:
                showDeleteConfirmationDialog();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onExportSelected(boolean shareMode) {
        Backup backup = new Backup();
        List<Program> programList = new ArrayList<>();
        for (Program program : programs) {
            if (viewModel.selectedItems.contains(program.getId()))
                programList.add(program);
        }
        backup.setPrograms(programList);
        ((MainActivity) requireActivity()).onExportSelected(backup, shareMode);
    }

    private void showDeleteConfirmationDialog() {

        new AlertDialog.Builder(getContext())
                .setTitle("Delete Confirmation")
                .setMessage("By deleting the selected programs , all the information associated with them will be lost. You will not be able to restore it.")
                .setPositiveButton("Delete Anyway", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (Long id : viewModel.selectedItems) {
                            viewModel.deleteProgram(id); // Update to deleteTask method
                        }
                        setSelectionMode(false);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }


}


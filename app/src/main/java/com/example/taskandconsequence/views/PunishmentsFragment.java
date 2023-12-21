package com.example.taskandconsequence.views;

import android.content.Context;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.taskandconsequence.MainActivity;
import com.example.taskandconsequence.R;
import com.example.taskandconsequence.databinding.FragmentPunishmentsBinding;
import com.example.taskandconsequence.model.Backup;
import com.example.taskandconsequence.model.Punishment;
import com.example.taskandconsequence.viewmodel.SharedViewModel;
import com.example.taskandconsequence.views.adapter.PunishmentAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PunishmentsFragment extends Fragment {

    private FragmentPunishmentsBinding binding;
    // Assuming PunishmentAdapter is your custom RecyclerView adapter
    private PunishmentAdapter adapter;

    private SharedViewModel viewModel;
    private NavController navController;

    private List<Punishment> musterPunishments;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Use the activity to get the same instance provided to the activity
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPunishmentsBinding.inflate(inflater, container, false);
        navController = NavHostFragment.findNavController(this);
        getActivity().setTitle("Punishments");
        setupRecyclerView();
        setHasOptionsMenu(true);

        setupFloatingActionButton();
        setSelectionMode(viewModel.isSelectionModeActive);
        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new PunishmentAdapter(new ArrayList<>(),
                this::onPunishmentClick, this::onPunishmentLongClick,viewModel.selectedItems);

        binding.recyclerView.setAdapter(adapter);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        binding.recyclerView.addItemDecoration(dividerItemDecoration);
        viewModel.getMusterPunishments().observe(this,punishments -> {
            musterPunishments = punishments;
            adapter.setPunishments(punishments);
            adapter.notifyDataSetChanged();
        });

    }

    private void setupFloatingActionButton() {
        binding.fabAddPunishment.setOnClickListener(view -> {
            viewModel.activeObject = null ;
            viewModel.editMode = false;
            setSelectionMode(false);
            navController.navigate(R.id.action_punishmentsFragment_to_addEditPunishmentFragment);
        });
        binding.fabDeletePunishment.setOnClickListener(view -> {
            for (Long id : viewModel.selectedItems){
                viewModel.deletePunishment(id);
            }
            setSelectionMode(false);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Implement methods for handling selection, deletion, and navigation


    private void onPunishmentClick(Punishment punishment) {
        if (viewModel.isSelectionModeActive) {
            Set<Long> selectedItems = viewModel.selectedItems;
            // Toggle selection
            if (selectedItems.contains(punishment.getId())) {
                selectedItems.remove(punishment.getId());
            } else {
                selectedItems.add(punishment.getId());
            }
            if (selectedItems.isEmpty()) {
                setSelectionMode(false);
            }
            requireActivity().invalidateOptionsMenu();

            adapter.notifyDataSetChanged(); // Notify the adapter to update the UI
        } else {
            // Navigate to AddEditPunishmentFragment in edit mode
            // TODO: Implement navigation logic
            viewModel.activeObject = punishment ;
            viewModel.editMode = true;
            navController.navigate(R.id.action_punishmentsFragment_to_addEditPunishmentFragment);

        }
    }


    private void onPunishmentLongClick(Punishment punishment) {
        // Handle long click: Select item and show/hide buttons accordingly
        if (viewModel.isSelectionModeActive) {
            onPunishmentClick(punishment);
        } else {
            setSelectionMode(true);
            viewModel.selectedItems.add(punishment.getId());
            adapter.notifyDataSetChanged();
        }

    }


    private void setSelectionMode(boolean isSelectionModeActive){
        if ( viewModel.isSelectionModeActive != isSelectionModeActive)
            requireActivity().invalidateOptionsMenu();
        viewModel.isSelectionModeActive = isSelectionModeActive;
//        binding.fabAddPunishment.setVisibility(isSelectionModeActive?View.INVISIBLE:View.VISIBLE);
//        binding.fabDeletePunishment.setVisibility(isSelectionModeActive?View.VISIBLE:View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_select_all:
                viewModel.selectedItems.clear();
                for (Punishment punishment : musterPunishments){
                    viewModel.selectedItems.add(punishment.getId());
                }
                binding.recyclerView.getAdapter().notifyDataSetChanged();
                return true;
            case R.id.menu_item_export_selected:
                onExportSelected(false);
                return true;
            case R.id.menu_item_share_selected:
                onExportSelected(true);
                return true;

            case R.id.menu_item_delete:
                for (Long id : viewModel.selectedItems){
                    viewModel.deletePunishment(id);
                }
                setSelectionMode(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onExportSelected(boolean shareMode) {
        Backup backup = new Backup();
        List<Punishment> punishmentList = new ArrayList<>();
        for (Punishment punishment : musterPunishments){
            if (viewModel.selectedItems.contains(punishment.getId()) )
                punishmentList.add(punishment);
        }
        backup.setPunishments(punishmentList);
        ((MainActivity) requireActivity() ).onExportSelected(backup,shareMode);
    }
}

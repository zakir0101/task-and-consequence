package com.example.taskandconsequence.views.addedit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.taskandconsequence.MainActivity;
import com.example.taskandconsequence.R;
import com.example.taskandconsequence.databinding.FragmentAddEditPunishmentBinding;
import com.example.taskandconsequence.model.Punishment;
import com.example.taskandconsequence.viewmodel.SharedViewModel;

public class AddEditPunishmentFragment extends Fragment {

    private FragmentAddEditPunishmentBinding binding;
    private SharedViewModel sharedViewModel;
    NavController navController;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddEditPunishmentBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
         navController = NavHostFragment.findNavController(this);


        setupUI();
        setupEventHandlers();

        return binding.getRoot();
    }

    private void setupUI() {
        // Setup dropdown for severity

        ArrayAdapter<CharSequence> severityAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.severity_levels, android.R.layout.simple_dropdown_item_1line);
        binding.severityDropdown.setAdapter(severityAdapter);
        binding.severityDropdown.setThreshold(0);

        // Set up the dropdown menu
        binding.severityDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.severityDropdown.showDropDown();
            }
        });
        binding.severityDropdown.setOnClickListener(v -> {
            // Show the dropdown menu
            binding.severityDropdown.showDropDown();
        });
        // Check if in edit mode and populate fields
        if (sharedViewModel.editMode && sharedViewModel.activeObject instanceof Punishment) {

            Punishment activePunishment = (Punishment) sharedViewModel.activeObject;
            binding.nameEditText.setText(activePunishment.getName());
            binding.descriptionEditText.setText(activePunishment.getDescription());
            String severityLevel = activePunishment.getSeverityLevel();
            int spinnerPosition = severityAdapter.getPosition(severityLevel);
            binding.severityDropdown.setText(severityLevel, false); // The second argument is false to prevent filtering on setting text

            getActivity().setTitle("Edit Punishment");
        }else{
            getActivity().setTitle("Add Punishment");
        }
    }


    private void setupEventHandlers() {
        binding.saveButton.setOnClickListener(v -> savePunishment());
        binding.cancelButton.setOnClickListener(v -> cancelEditing());
    }

    private void savePunishment() {
        String name = binding.nameEditText.getText().toString();
        String severity = binding.severityDropdown.getText().toString().toLowerCase();
        String description = binding.descriptionEditText.getText().toString();

        // Validate inputs
        if (name.isEmpty()) {
            binding.nameInputLayout.setError("Name cannot be empty");
        } else {
            binding.nameInputLayout.setError(null); // Clear error
        }

        if (severity.isEmpty()) {
            binding.severityInputLayout.setError("Severity level must be selected");
        } else {
            binding.severityInputLayout.setError(null); // Clear error
        }

        if (description.isEmpty()) {
            binding.descriptionInputLayout.setError("Description cannot be empty");
        } else {
            binding.descriptionInputLayout.setError(null); // Clear error
        }

        if (!name.isEmpty() && !severity.isEmpty() && !description.isEmpty()) {

            // Create or update Punishment object
            Punishment punishment = sharedViewModel.editMode ? (Punishment) sharedViewModel.activeObject : new Punishment();
            punishment.setName(name);
            punishment.setSeverityLevel(severity);
            punishment.setDescription(description);
            punishment.setDeadline(-1);
            // Call ViewModel to save the punishment
            if (sharedViewModel.editMode)
                sharedViewModel.updatePunishment(punishment);
            else
                sharedViewModel.addPunishment(punishment);

            // Optionally, navigate back or give user feedback
            navController.navigateUp();
        }
    }

    private void cancelEditing() {
        // Handle cancel operation, possibly navigate back or clear fields
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

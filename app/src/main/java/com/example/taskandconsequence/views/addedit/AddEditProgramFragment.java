package com.example.taskandconsequence.views.addedit;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.taskandconsequence.MainActivity;
import com.example.taskandconsequence.R;
import com.example.taskandconsequence.databinding.FragmentAddEditProgramBinding;
import com.example.taskandconsequence.model.Program;
import com.example.taskandconsequence.model.Punishment;
import com.example.taskandconsequence.model.Status;
import com.example.taskandconsequence.model.Task;
import com.example.taskandconsequence.viewmodel.SharedViewModel;
import com.example.taskandconsequence.views.adapter.TaskAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

public class AddEditProgramFragment extends Fragment {

    private FragmentAddEditProgramBinding binding;
    private SharedViewModel sharedViewModel;
    private NavController navController;
    private Calendar startDateCalendar = Calendar.getInstance();

    private Punishment selectedSmallPunishment;
    private Punishment selectedBigPunishment;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddEditProgramBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        navController = NavHostFragment.findNavController(this);
        requireActivity().invalidateOptionsMenu();

        setupUI();
        setupEventHandlers();

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (selectedSmallPunishment != null )
            binding.smallPunishmentDescription.setText(selectedSmallPunishment.getDescription());
        if (selectedBigPunishment != null)
            binding.bigPunishmentDescription.setText(selectedBigPunishment.getDescription());
    }

    private void setupUI() {
        // Setup dropdowns and pickers
        setupFrequencyDropdown();
        sharedViewModel.getMusterPunishments().observe(getViewLifecycleOwner(), punishments -> {
            setupPunishmentDropdowns(punishments);
        });
        setupDateAndTimePickers();
        setUpTaskRecyclerView();
        // Check if in edit mode and populate fields
        if (sharedViewModel.editMode && sharedViewModel.activeObject instanceof Program) {
            Program activeProgram = (Program) sharedViewModel.activeObject;
            populateFieldsForEdit(activeProgram);
            getActivity().setTitle("Edit Program");
            binding.btnAddTask.setVisibility(View.GONE);
            binding.btnCreateTask.setVisibility(View.GONE);
        } else {
            getActivity().setTitle("Add Program");
        }
    }

    private void setUpTaskRecyclerView() {
        TaskAdapter adapter = new TaskAdapter(new ArrayList<>(),
                this::onTaskClick, this::onTaskLongClick, new HashSet<>() , this::onRemoveTask,!sharedViewModel.editMode);

        binding.tasksRecyclerView.setAdapter(adapter);

        binding.tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        sharedViewModel.getProgramTasks().observe(getViewLifecycleOwner(), tasks -> {
            if ( !tasks.isEmpty())
                binding.taskErrorText.setVisibility(View.GONE);
            adapter.setTasks(tasks);
            adapter.notifyDataSetChanged();
        });
    }

    private void onTaskClick(Task task) {
            // Navigate to AddEditTaskFragment in edit mode
            sharedViewModel.activeTask = task;
            sharedViewModel.taskEditMode = true;
            navController.navigate(R.id.action_addEditProgramFragment_to_addEditTaskFragment);
    }

    private void onTaskLongClick(Task task) {
    }

    private void onRemoveTask(Task task) {
        sharedViewModel.removeProgramTasks(task);

    }

    private void setupFrequencyDropdown() {
        ArrayAdapter<CharSequence> severityAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.frequencies, android.R.layout.simple_dropdown_item_1line);
        binding.frequencyDropdown.setAdapter(severityAdapter);
        binding.frequencyDropdown.setThreshold(0);

        // Set up the dropdown menu
        binding.frequencyDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.frequencyDropdown.showDropDown();
            }
        });
        binding.frequencyDropdown.setOnClickListener(v -> {
            binding.frequencyDropdown.showDropDown();
        });
    }

    private void setupPunishmentDropdowns(List<Punishment> punishments) {
        // Code to setup punishment dropdowns
        List<Punishment> small = new ArrayList<>();
        List<Punishment> big = new ArrayList<>();
        for (Punishment p : punishments) {
            if (p.getSeverityLevel().equals("big"))
                big.add(p);
            else
                small.add(p);
        }

        ArrayAdapter<Punishment> bigAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, big);
        bigAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        ArrayAdapter<Punishment> smallAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, small);
        smallAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.smallPunishmentDropdown.setAdapter(smallAdapter);
        binding.smallPunishmentDropdown.setOnItemClickListener((parent, view, position, id) -> {
             selectedSmallPunishment = (Punishment) parent.getItemAtPosition(position);
            binding.smallPunishmentDescription.setText(selectedSmallPunishment.getDescription());
        });

//        if (!binding.smallPunishmentDropdown.getText().toString().isEmpty())
//            binding.smallPunishmentDropdown.setSelection(binding.smallPunishmentDropdown.getSelectionStart(),binding.smallPunishmentDropdown.getSelectionEnd());
        // Setup Big Punishment Dropdown
        binding.bigPunishmentDropdown.setAdapter(bigAdapter);
        binding.bigPunishmentDropdown.setOnItemClickListener((parent, view, position, id) -> {
             selectedBigPunishment = (Punishment) parent.getItemAtPosition(position);
            binding.bigPunishmentDescription.setText(selectedBigPunishment.getDescription());
        });

//        if (!binding.bigPunishmentDropdown.getText().toString().isEmpty())
//            binding.bigPunishmentDropdown.setSelection(binding.bigPunishmentDropdown.getSelectionStart(),binding.bigPunishmentDropdown.getSelectionEnd());

        binding.bigPunishmentDropdown.setOnClickListener(v -> {
            binding.bigPunishmentDropdown.showDropDown();
        });

        binding.smallPunishmentDropdown.setOnClickListener(v -> {
            binding.smallPunishmentDropdown.showDropDown();
        });

    }

    private void setupDateAndTimePickers() {

        // Date Picker for Start Date
        if ( sharedViewModel.editMode){
            binding.startDateEditText.setEnabled(false);
            binding.timeEditText.setEnabled(false);
        }else {
            binding.startDateEditText.setOnClickListener(view -> {
                int year = startDateCalendar.get(Calendar.YEAR);
                int month = startDateCalendar.get(Calendar.MONTH);
                int day = startDateCalendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                    startDateCalendar.set(Calendar.YEAR, selectedYear);
                    startDateCalendar.set(Calendar.MONTH, selectedMonth);
                    startDateCalendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                    updateStartDateDisplay();
                }, year, month, day);

                datePickerDialog.show();
            });

            // Time Picker for Start Time
            binding.timeEditText.setOnClickListener(view -> {
                int hour = startDateCalendar.get(Calendar.HOUR_OF_DAY);
                int minute = startDateCalendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (timePicker, selectedHour, selectedMinute) -> {
                    startDateCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                    startDateCalendar.set(Calendar.MINUTE, selectedMinute);
                    updateStartTimeDisplay();
                }, hour, minute, DateFormat.is24HourFormat(getContext()));

                timePickerDialog.show();
            });
        }
    }

    private void updateStartDateDisplay() {
        String formattedDate = DateFormat.getDateFormat(getContext()).format(startDateCalendar.getTime());
        binding.startDateEditText.setText(formattedDate);
    }

    private void updateStartTimeDisplay() {
        String formattedTime = DateFormat.getTimeFormat(getContext()).format(startDateCalendar.getTime());
        binding.timeEditText.setText(formattedTime);
    }


    private void populateFieldsForEdit(Program program) {
        // Set the name of the program
        binding.nameEditText.setText(program.getName());

        // Set the frequency
        String frequency = program.getFrequency(); // Assuming getFrequency returns a String
        if (frequency != null) {
            int spinnerPosition = ((ArrayAdapter<String>)binding.frequencyDropdown.getAdapter()).getPosition(frequency);
            binding.frequencyDropdown.setText(binding.frequencyDropdown.getAdapter().getItem(spinnerPosition).toString(), false);
        }

        // Set the number of periods
        binding.numberOfPeriodsEditText.setText(String.valueOf(program.getNumberOfPeriods()));

        // Set the small and big punishments
        // Assuming getPunishmentNameById or a similar method returns the name of the punishment by its ID
         selectedSmallPunishment = program.getSmallPunishment();
        binding.smallPunishmentDropdown.setText(selectedSmallPunishment.getName(), false);
        binding.smallPunishmentDescription.setText(selectedSmallPunishment.getDescription());

        selectedBigPunishment = program.getBigPunishment();
        binding.bigPunishmentDropdown.setText(selectedBigPunishment.getName(), false);
        binding.bigPunishmentDescription.setText(selectedBigPunishment.getDescription());

        // Set the start date and time
        if (program.getStartDate() != null) {
            startDateCalendar.setTime(program.getStartDate());
            updateStartDateDisplay();
            updateStartTimeDisplay();
        }

        // Populate the RecyclerView with tasks
        // Assuming you have a method to set tasks in your adapter
//        sharedViewModel.clearProgramTasks();
        if(sharedViewModel.getProgramTasks().getValue().isEmpty())
            sharedViewModel.addAllProgramTasks(program.getTasks());
    }


    private void setupEventHandlers() {
        binding.btnAddTask.setOnClickListener(view -> {
            sharedViewModel.programMode = true ;
            navController.navigate(R.id.action_addEditProgramFragment_to_tasksFragment);
        });
        binding.btnCreateTask.setOnClickListener(view -> {
            sharedViewModel.activeTask = null;
            sharedViewModel.taskEditMode = false;
            sharedViewModel.programMode = true;
            navController.navigate(R.id.action_addEditProgramFragment_to_addEditTaskFragment); // Update the navigation action ID
        });
        binding.saveButton.setOnClickListener(v -> saveProgram());
        binding.cancelButton.setOnClickListener(v -> cancelEditing());
    }

    private void saveProgram() {
        String name = binding.nameEditText.getText().toString();
        String frequency = binding.frequencyDropdown.getText().toString();
        String numberOfPeriodsString = binding.numberOfPeriodsEditText.getText().toString();
//        String smallPunishment = binding.smallPunishmentDropdown.getText().toString();
//        String bigPunishment = binding.bigPunishmentDropdown.getText().toString();
        String startDate = binding.startDateEditText.getText().toString();
        String startTime = binding.timeEditText.getText().toString();
        List<Task> selectedTasks = sharedViewModel.getProgramTasks().getValue();

        // Validation checks
        boolean isValid = true;
        if (name.isEmpty()) {
            binding.nameInputLayout.setError("Name cannot be empty");
            isValid = false;
        } else {
            binding.nameInputLayout.setError(null);
        }

        if (frequency.isEmpty()) {
            binding.frequencyInputLayout.setError("Frequency must be selected");
            isValid = false;
        } else {
            binding.frequencyInputLayout.setError(null);
        }

        int numberOfPeriods = 0;
        try {
            numberOfPeriods = Integer.parseInt(numberOfPeriodsString);
        } catch (NumberFormatException e) {
            binding.numberOfPeriodsInputLayout.setError("Invalid number");
            isValid = false;
        }

        if (selectedSmallPunishment == null) {
            binding.smallPunishmentInputLayout.setError("Small Punishment cannot be empty");
            isValid = false;
        } else {
            binding.smallPunishmentInputLayout.setError(null);
        }

        if (selectedBigPunishment == null) {
            binding.bigPunishmentInputLayout.setError("Big Punishment cannot be empty");
            isValid = false;
        } else {
            binding.bigPunishmentInputLayout.setError(null);
        }
        if (startDate.isEmpty()) {
            binding.startDateInputLayout.setError("Start Date cannot be empty");
            isValid = false;
        } else {
            binding.startDateInputLayout.setError(null);
        }
        if (startTime.isEmpty()) {
            binding.timeInputLayout.setError("Start Time cannot be empty");
            isValid = false;
        } else {
            binding.timeInputLayout.setError(null);
        }
        if (selectedTasks.isEmpty()) {
            binding.taskErrorText.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            binding.taskErrorText.setVisibility(View.GONE);
        }
        if (isValid) {
            // Create or update Program object
            Program program = sharedViewModel.editMode ? (Program) sharedViewModel.activeObject : new Program();
            program.setName(name);
            program.setFrequency(frequency);
            program.setNumberOfPeriods(numberOfPeriods);
            // Assuming getSelectedPunishmentId returns the ID of the selected punishment
            selectedSmallPunishment.setDeadline(1);
            program.setSmallPunishment(selectedSmallPunishment);
            selectedBigPunishment.setDeadline(1);
            program.setBigPunishment(selectedBigPunishment);
            program.setStartDate(startDateCalendar.getTime());
            program.setTasks(selectedTasks);
            if(!sharedViewModel.editMode)
                program.setStatus(Status.PENDING);
            // Save the program using SharedViewModel
            if (sharedViewModel.editMode) {
                sharedViewModel.updateProgram(program);
            } else {
                sharedViewModel.addProgram(program);
            }
            sharedViewModel.isSelectionModeActive = false;
            sharedViewModel.selectedItems.clear();
            sharedViewModel.clearProgramTasks();
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

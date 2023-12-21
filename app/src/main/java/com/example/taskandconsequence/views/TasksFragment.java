package com.example.taskandconsequence.views;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.taskandconsequence.MainActivity;
import com.example.taskandconsequence.R;
import com.example.taskandconsequence.databinding.FragmentTasksBinding;
import com.example.taskandconsequence.model.Backup;
import com.example.taskandconsequence.model.Task;
import com.example.taskandconsequence.viewmodel.SharedViewModel;
import com.example.taskandconsequence.views.adapter.TaskAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TasksFragment extends Fragment {

    private FragmentTasksBinding binding;
    private TaskAdapter adapter;
    private SharedViewModel viewModel;
    private NavController navController;

    private List<Task> musterTasks;
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Use the activity to get the same instance provided to the activity
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        navController = NavHostFragment.findNavController(this);
        setHasOptionsMenu(true);
        if (viewModel.programMode)
            getActivity().setTitle("Select Tasks ");
        else {
            getActivity().setTitle("Tasks");
            setSelectionMode(viewModel.isSelectionModeActive);

        }
        setupRecyclerView();
        setupFloatingActionButton();

        return binding.getRoot();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void setupRecyclerView() {
        adapter = new TaskAdapter(new ArrayList<>(),
                this::onTaskClick, this::onTaskLongClick,
                viewModel.selectedItems, this::onRemoveTask,false);

        binding.recyclerViewTasks.setAdapter(adapter);

        binding.recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.recyclerViewTasks.getContext(), DividerItemDecoration.VERTICAL);
//        binding.recyclerViewTasks.addItemDecoration(dividerItemDecoration);
        viewModel.getMusterTasks().observe(getViewLifecycleOwner(), tasks -> {
            musterTasks = tasks;
            if (!viewModel.programMode) {
                adapter.setTasks(tasks);
                adapter.notifyDataSetChanged();

            }
            else
            {
                viewModel.getProgramTasks().observe(getViewLifecycleOwner(), programTasks -> {
                    tasks.removeAll(programTasks);
                    adapter.setTasks(tasks);

                });
            }
        });
    }

    private void setupFloatingActionButton() {
        if (viewModel.programMode){
            binding.fabAddTask.setVisibility(View.INVISIBLE);
            binding.fabDeleteTask.setVisibility(View.INVISIBLE);
            binding.fabBack.setVisibility(View.VISIBLE);
            binding.fabBack.setOnClickListener(view -> {
                navController.navigateUp();
            });
        }
        else {
            binding.fabAddTask.setVisibility(View.VISIBLE);
            binding.fabDeleteTask.setVisibility(View.INVISIBLE);
            binding.fabBack.setVisibility(View.GONE);
            binding.fabAddTask.setOnClickListener(view -> {
                viewModel.activeTask = null;
                viewModel.taskEditMode = false;
                setSelectionMode(false);
                navController.navigate(R.id.action_tasksFragment_to_addEditTaskFragment); // Update the navigation action ID
            });
            binding.fabDeleteTask.setOnClickListener(view -> {
                for (Long id : viewModel.selectedItems) {
                    viewModel.deleteTask(id); // Update to deleteTask method
                }
                setSelectionMode(false);
            });

        }
    }


    private void onTaskClick(Task task) {
       if (!viewModel.programMode) {
           if (viewModel.isSelectionModeActive) {
               Set<Long> selectedItems = viewModel.selectedItems;
               // Toggle selection
               if (selectedItems.contains(task.getId())) {
                   selectedItems.remove(task.getId());
               } else {
                   selectedItems.add(task.getId());
               }
               if (selectedItems.isEmpty()) {
                   setSelectionMode(false);
                   // Update UI accordingly
               }
               this.requireActivity().invalidateOptionsMenu();
               adapter.notifyDataSetChanged(); // Update the UI
           } else {
               // Navigate to AddEditTaskFragment in edit mode
               viewModel.activeTask = task;
               viewModel.taskEditMode = true;
               navController.navigate(R.id.action_tasksFragment_to_addEditTaskFragment);
           }
       }
       else{
           viewModel.addProgramTasks(task);
           Toast.makeText(getContext(),"Task added Successfully", Toast.LENGTH_SHORT).show();
       }
    }

    private void onTaskLongClick(Task task) {
        if (!viewModel.programMode) {
            if (viewModel.isSelectionModeActive) {
                onTaskClick(task);
            } else {
                setSelectionMode(true);
                viewModel.selectedItems.add(task.getId());
                adapter.notifyDataSetChanged();
            }
        }
        else{

        }

    }
    private void onRemoveTask(Task task) {

    }

    private void setSelectionMode(boolean isSelectionModeActive){
        if ( viewModel.isSelectionModeActive != isSelectionModeActive )
            this.requireActivity().invalidateOptionsMenu();

        viewModel.isSelectionModeActive = isSelectionModeActive;
//        binding.fabAddTask.setVisibility(isSelectionModeActive?View.INVISIBLE:View.VISIBLE);
//        binding.fabDeleteTask.setVisibility(isSelectionModeActive?View.VISIBLE:View.INVISIBLE);
        viewModel.selectedItems.clear();
    }

    // Other necessary methods...


    @Override
    public void onResume() {
        super.onResume();
        if (viewModel.programMode) {
            ((MainActivity) getActivity()).getBottomNavigationView().setVisibility(View.GONE);
            ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }else {
            ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (viewModel.programMode)
            ((MainActivity) getActivity()).getBottomNavigationView().setVisibility(View.VISIBLE);
    }



//    @Override
//    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.toolbar_menu_task,menu);
//    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_select_all:
                viewModel.selectedItems.clear();
                for (Task task : musterTasks){
                    viewModel.selectedItems.add(task.getId());
                }
                binding.recyclerViewTasks.getAdapter().notifyDataSetChanged();
                return true;
            case R.id.menu_item_export_selected:
                 onExportSelected(false);
                return true;
            case R.id.menu_item_share_selected:
                 onExportSelected(true);
                return true;
            case R.id.menu_item_delete:
                for (Long id : viewModel.selectedItems) {
                    viewModel.deleteTask(id); // Update to deleteTask method
                }
                setSelectionMode(false);
                return true;
        }
            return super.onOptionsItemSelected(item);
    }

    private void onExportSelected(boolean shareMode) {
        Backup backup = new Backup();
        List<Task> taskList = new ArrayList<>();
        for (Task task : musterTasks){
            if (viewModel.selectedItems.contains(task.getId()) )
                taskList.add(task);
        }
        backup.setTasks(taskList);
        ((MainActivity) requireActivity() ).onExportSelected(backup,shareMode);
    }
}

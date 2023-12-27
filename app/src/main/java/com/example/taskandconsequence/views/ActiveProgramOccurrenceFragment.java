package com.example.taskandconsequence.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskandconsequence.MainActivity;
import com.example.taskandconsequence.R;
import com.example.taskandconsequence.databinding.FragmentActiveProgramOccurrenceBinding;
import com.example.taskandconsequence.model.Program;
import com.example.taskandconsequence.model.ProgramOccurrence;
import com.example.taskandconsequence.viewmodel.SharedViewModel;
import com.example.taskandconsequence.views.adapter.ProgramOccurrenceAdapter;

import java.util.List;

public class ActiveProgramOccurrenceFragment extends Fragment {

    private FragmentActiveProgramOccurrenceBinding binding;
    private SharedViewModel viewModel;
    private ProgramOccurrenceAdapter adapter;
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentActiveProgramOccurrenceBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        navController = NavHostFragment.findNavController(this);
        getActivity().setTitle("Todos");
        setupRecyclerViews();
        return binding.getRoot();
    }
    private void setupRecyclerViews() {
        ProgramOccurrenceAdapter todayAdapter  = new ProgramOccurrenceAdapter( this::onProgramOccurrenceClick, true);
        ProgramOccurrenceAdapter thisWeekAdapter  = new ProgramOccurrenceAdapter( this::onProgramOccurrenceClick, true);
        ProgramOccurrenceAdapter thisMonthAdapter  = new ProgramOccurrenceAdapter( this::onProgramOccurrenceClick, true);

        setupRecyclerView(binding.rvToday, todayAdapter,viewModel.getProgramOccurrenceToday(),binding.textToday);
        setupRecyclerView(binding.rvThisWeek, thisWeekAdapter,viewModel.getProgramOccurrenceThisWeek(),binding.textThisWeek);
        setupRecyclerView(binding.rvThisMonth, thisMonthAdapter,viewModel.getProgramOccurrenceThisMonth(),binding.textThisMonth);

    }
    private void setupRecyclerView(RecyclerView recyclerView, ProgramOccurrenceAdapter adapter ,
                                   LiveData<List<ProgramOccurrence>> programOccurrenceList,
                                   TextView textView) {
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        programOccurrenceList.observe(getViewLifecycleOwner(), occurrences -> {
            textView.setVisibility(occurrences.size() == 0 ? View.GONE : View.VISIBLE);
            adapter.setProgramOccurrences(occurrences);
        });
    }


    private void onProgramOccurrenceClick(ProgramOccurrence programOccurrence) {
        viewModel.activeOccurrenceId = programOccurrence.getId();
        navController.navigate(R.id.action_activeProgramOccurrenceFragment_to_taskOccurrenceFragment);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity) requireActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

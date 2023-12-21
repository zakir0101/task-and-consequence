package com.example.taskandconsequence.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.example.taskandconsequence.databinding.FragmentActiveProgramOccurrenceBinding;

public class ActiveProgramOccurrenceFragment extends Fragment {

    private FragmentActiveProgramOccurrenceBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentActiveProgramOccurrenceBinding.inflate(inflater, container, false);
        binding.textView.setText("Active Program Occurrence Fragment");
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

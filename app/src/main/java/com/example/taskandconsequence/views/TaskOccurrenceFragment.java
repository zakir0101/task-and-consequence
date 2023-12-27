package com.example.taskandconsequence.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.taskandconsequence.Helper;
import com.example.taskandconsequence.MainActivity;
import com.example.taskandconsequence.R;
import com.example.taskandconsequence.databinding.FragmentTaskOccurrenceBinding;
import com.example.taskandconsequence.model.Program;
import com.example.taskandconsequence.model.ProgramOccurrence;
import com.example.taskandconsequence.model.Status;
import com.example.taskandconsequence.model.TaskOccurrence;
import com.example.taskandconsequence.viewmodel.SharedViewModel;
import com.example.taskandconsequence.views.adapter.TaskOccurrenceAdapter;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TaskOccurrenceFragment extends Fragment {

    private FragmentTaskOccurrenceBinding binding;
    private SharedViewModel viewModel;
    private TaskOccurrenceAdapter adapter;
    private NavController navController;

    private ProgramOccurrence activeProgramOccurrence;
    private boolean isInitialCall = true;
    private boolean isFirstCall;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTaskOccurrenceBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        navController = NavHostFragment.findNavController(this);
        Program p = (Program) viewModel.activeObject;
        if (p != null)
            getActivity().setTitle(p.getName());
        else
            getActivity().setTitle("");
        isFirstCall = true;

        setupRecyclerView();
//        setupButtons();
        setupPunishmentStatusToggleGroup();
        observeTaskOccurrences();
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        isFirstCall = true;
    }

    private void setupButtons() {
        // Show 'Mark as Succeed' button only if all occurrences have status SUCCEED or SUCCEED_PUNISHMENT
        binding.buttonMarkProgramOccurrenceSucceed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                markProgramOccurrenceAsSucceed(checked);
            }
        });


    }

    private void markProgramOccurrenceAsSucceed(boolean succeed) {
        if (succeed) {
            activeProgramOccurrence.setStatus(Status.SUCCEED);
        } else {
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
                    if (newStatus == Status.SUCCEED_PUNISHMENT || newStatus == Status.FAIL) {
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
        adapter = new TaskOccurrenceAdapter(this::onTaskStatusChange, this::getTaskById, this::getProgramFrequency);
        binding.recyclerViewTaskOccurrences.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewTaskOccurrences.setAdapter(adapter);
    }


    private void observeTaskOccurrences() {
        // Assuming activeProgramOccurrence is a LiveData in SharedViewModel
        viewModel.getActiveOccurrence().observe(getViewLifecycleOwner(), programOccurrence -> {
            if (programOccurrence.getId() .equals(viewModel.activeOccurrenceId)) {
                activeProgramOccurrence = programOccurrence;
                if (getActivity().getTitle().toString().isEmpty())
                    getActivity().setTitle(programOccurrence.getProgram().getName());
                List<TaskOccurrence> taskOccurrences = programOccurrence.getTaskOccurrences();
                adapter.setTaskOccurrences(taskOccurrences);
                updateButtonVisibility(taskOccurrences);
                adapter.notifyDataSetChanged();
            }
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
        if (allSucceed) {
            activeProgramOccurrence.setStatus(Status.SUCCEED);
            viewModel.updateProgramOccurrenceOnly(activeProgramOccurrence);
        } else if (!anyFail) {
            activeProgramOccurrence.setStatus(Status.PENDING);
            viewModel.updateProgramOccurrenceOnly(activeProgramOccurrence);

        }
        updatePunishmentSection(anyFail);
    }


    private void updatePunishmentSection(boolean hasFailedTasks) {
        // Check if there are any failed tasks

        boolean isVisible = binding.punishmentScrollView.getLayoutParams().height > 10;
        binding.textViewPunishmentName.setText(activeProgramOccurrence.getProgram().getSmallPunishment().getName());
        binding.textViewPunishmentDescription.setText(activeProgramOccurrence.getProgram().getSmallPunishment().getDescription());
        // Set the initial state based on the current punishment status
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        int numOfDays = Helper.getNumOfDays(getProgramFrequency());
        binding.textViewPunishmentDeadline.setText("Deadline: " + dateFormat.format(Helper.addDays(activeProgramOccurrence.getDate(), numOfDays * 2)));

        if (hasFailedTasks) {
            // Show the punishment section
            if (!isVisible) {

                if (activeProgramOccurrence.getStatus() != Status.PENDING_PUNISHMENT &&
                        activeProgramOccurrence.getStatus() != Status.SUCCEED_PUNISHMENT &&
                        activeProgramOccurrence.getStatus() != Status.FAIL) {
                    activeProgramOccurrence.setStatus(Status.PENDING_PUNISHMENT);
                    viewModel.updateProgramOccurrenceOnly(activeProgramOccurrence);
                }

                if (isFirstCall) {
                    binding.punishmentScrollView.post(() -> {
                      binding.punishmentScrollView.getLayoutParams().height = getTargetHeightForPunishmentSection();
                    });
                 } else
                    animatePunishmentSection(isVisible);
                // Get the punishment details from the active program



            }
            int buttonId = getStatusButtonId(activeProgramOccurrence.getStatus());
//            isInitialCall = false;
            binding.toggleGroupPunishmentStatus.check(buttonId);
            // Set up the MaterialButtonToggleGroup for the punishment status
        } else {
            if (isVisible ) {
                // Hide the punishment section
                if (activeProgramOccurrence.getStatus() != Status.PENDING && activeProgramOccurrence.getStatus() != Status.SUCCEED) {
                    activeProgramOccurrence.setStatus(Status.PENDING);
                    viewModel.updateProgramOccurrenceOnly(activeProgramOccurrence);
                }

                if (isFirstCall) {
                    binding.punishmentScrollView.post(() ->  {
                        binding.punishmentScrollView.getLayoutParams().height = 0;
                        binding.punishmentScrollView.requestLayout();
                    });
                } else
                    animatePunishmentSection(isVisible);
            }
        }
        isFirstCall = false;
    }

    public static int getHeight(Context context, CharSequence text, int textSize, int deviceWidth, int padding) {

        TextView textView = new TextView(context);
        textView.setPadding(padding,0,padding,padding);
        textView.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        textView.setText(text, TextView.BufferType.SPANNABLE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        return textView.getMeasuredHeight();
    }

    public  int getDeviceWidth (){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        return width;
    }
    private void animatePunishmentSection(Boolean isVisible) {
        // Schedule a runnable to be executed after layout pass
//            binding.punishmentSection.setVisibility(View.VISIBLE);

        binding.punishmentSectionIn.post(new Runnable() {
            @Override
            public void run() {
                // Measure the punishmentSection with updated content
                int targetHeight = getTargetHeightForPunishmentSection();
                ValueAnimator valueAnimator;
                if (!isVisible) {
//                    punishmentSection.setVisibility(View.VISIBLE);
                    valueAnimator = ValueAnimator.ofInt(0, targetHeight);
                } else {
                    valueAnimator = ValueAnimator.ofInt(targetHeight, 0);
                    valueAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
//                           binding. punishmentSection.setVisibility(View.GONE);
                        }
                    });
                }

                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                       binding. punishmentScrollView.getLayoutParams().height = (int) animator.getAnimatedValue();
                        binding. punishmentScrollView.requestLayout();
                    }
                });
                valueAnimator.setDuration(500); // Duration in milliseconds
                valueAnimator.start();
            }
        });
    }

    private int getTargetHeightForPunishmentSection() {
        binding.textViewPunishmentDescription.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int wrongTextHeight = binding.textViewPunishmentDescription.getMeasuredHeight();
        int wrongTextWidth = binding.textViewPunishmentDescription.getMeasuredWidth();
        binding.punishmentSectionIn.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int targetHeight = binding.punishmentSectionIn.getMeasuredHeight();

        int deviceWidth = getDeviceWidth();
        int textHeight = TaskOccurrenceFragment.getHeight(getContext(),
                activeProgramOccurrence.getProgram().getSmallPunishment().getDescription(),
                20,deviceWidth,0);
        targetHeight += textHeight;
        targetHeight -= wrongTextHeight;
        return Math.min( targetHeight , 800 );
    }


//    private void animatePunishmentSection(final LinearLayout punishmentSection) {
//        // Determine the end height
//        punishmentSection.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//         int targetHeight = punishmentSection.getMeasuredHeight();
//        binding.toggleGroupPunishmentStatus.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        targetHeight = targetHeight + binding.toggleGroupPunishmentStatus.getMeasuredHeight();
//        // Start the animation
//        ValueAnimator valueAnimator;
//        if (punishmentSection.getVisibility() == View.GONE) {
//            punishmentSection.setVisibility(View.VISIBLE);
//            valueAnimator = ValueAnimator.ofInt(0, targetHeight);
//        } else {
//            valueAnimator = ValueAnimator.ofInt(targetHeight, 0);
//            valueAnimator.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    punishmentSection.setVisibility(View.GONE);
//                }
//            });
//        }
//
//        valueAnimator.addUpdateListener(animator -> {
//            punishmentSection.getLayoutParams().height = (int) animator.getAnimatedValue();
//            punishmentSection.requestLayout();
//        });
//        valueAnimator.setDuration(500); // Duration in milliseconds
//        valueAnimator.start();
//    }

    private void onTaskStatusChange(TaskOccurrence taskOccurrence, Status newStatus) {
        // Handle task occurrence item click
        // ...
        taskOccurrence.setStatus(newStatus);
        viewModel.updateProgramOccurrence(activeProgramOccurrence);

    }

    private void getTaskById(Long taskId, TaskOccurrenceAdapter.OnUpdateViewHolderListener onUpdateViewHolderListener) {
        viewModel.getTask(taskId).observe(getViewLifecycleOwner(), task -> {
            if (task != null)
                onUpdateViewHolderListener.onUpdate(task);
        });
    }

    private String getProgramFrequency() {
        return activeProgramOccurrence.getProgram().getFrequency();
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


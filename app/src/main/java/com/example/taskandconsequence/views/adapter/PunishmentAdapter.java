package com.example.taskandconsequence.views.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Set;

import com.example.taskandconsequence.Helper;
import com.example.taskandconsequence.R;
import com.example.taskandconsequence.databinding.ItemPunishmentBinding;
import com.example.taskandconsequence.model.Punishment;
import com.google.android.material.color.MaterialColors;

public class PunishmentAdapter extends RecyclerView.Adapter<PunishmentAdapter.PunishmentViewHolder> {

    private List<Punishment> punishments; // Your Punishment model list
    // Listener for item click events (if needed)

    // Listener interfaces
    public interface OnItemClickListener {
        void onItemClick(Punishment punishment);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Punishment punishment);
    }

    private OnItemClickListener itemClickListener;
    private OnItemLongClickListener itemLongClickListener;
    private Set<Long> selectedItems ;

    // Constructor
    public PunishmentAdapter(List<Punishment> punishments, OnItemClickListener itemClickListener, OnItemLongClickListener itemLongClickListener, Set<Long> selectedItems) {
        this.punishments = punishments;
        this.itemClickListener = itemClickListener;
        this.selectedItems = selectedItems;
        this.itemLongClickListener = itemLongClickListener;
    }

    public void setPunishments(List<Punishment> punishments){
        this.punishments = punishments;
        this.notifyDataSetChanged();
    }
    @NonNull
    @Override
    public PunishmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPunishmentBinding binding = ItemPunishmentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PunishmentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PunishmentViewHolder holder, int position) {
        Punishment punishment = punishments.get(position);
        boolean isSelected = selectedItems.contains(punishment.getId());

        holder.bind(punishment, itemClickListener, itemLongClickListener, isSelected);
    }

    @Override
    public int getItemCount() {
        return punishments.size();
    }

    static class PunishmentViewHolder extends RecyclerView.ViewHolder {
        private ItemPunishmentBinding binding;

        PunishmentViewHolder(ItemPunishmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Punishment punishment, OnItemClickListener clickListener, OnItemLongClickListener longClickListener, boolean isSelected) {
            binding.tvPunishmentName.setText(punishment.getName());
            binding.tvSeverityLevel.setText("Severity: " + Helper.capitalizeFirstLetter(punishment.getSeverityLevel()) );
            binding.tvDescription.setText(punishment.getDescription());
            // Add any additional bindings here
            itemView.setOnClickListener(v -> clickListener.onItemClick(punishment));
            itemView.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(punishment);
                return true;
            });

            int backgroundColor = isSelected ?
                    ContextCompat.getColor(itemView.getContext(), R.color.selectedItemBackground) :
                    MaterialColors.getColor(itemView.getContext(), com.google.android.material.R.attr.colorSurface, Color.WHITE); // Default surface color
            int textColor = isSelected ?
                    ContextCompat.getColor(itemView.getContext(), R.color.selectedItemText) :
                    MaterialColors.getColor(itemView.getContext(), com.google.android.material.R.attr.colorOnSurface, Color.BLACK); // Default onSurface color

            itemView.setBackgroundColor(backgroundColor);
            binding.tvPunishmentName.setTextColor(textColor); // Assuming textViewName is the TextView showing the name
            binding.tvSeverityLevel.setTextColor(textColor); // Assuming textViewDescription shows the description
            binding.tvDescription.setTextColor(textColor); // Assuming textViewDescription shows the description

        }
    }
}


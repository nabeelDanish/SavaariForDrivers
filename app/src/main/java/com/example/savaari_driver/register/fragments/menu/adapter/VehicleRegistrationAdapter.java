package com.example.savaari_driver.register.fragments.menu.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.savaari_driver.R;
import com.example.savaari_driver.ride.adapter.OnItemClickListener;
import com.example.savaari_driver.ride.adapter.VehicleTypeItem;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class VehicleRegistrationAdapter extends RecyclerView.Adapter<VehicleRegistrationAdapter.ItemViewHolder> {

    // Main Attributes
    private ArrayList<VehicleTypeItem> mItemList;
    private OnItemClickListener listener;

    // Constructor
    public VehicleRegistrationAdapter(ArrayList<VehicleTypeItem> itemList, OnItemClickListener listener) {
        mItemList = itemList;
        this.listener = listener;
    }

    // Static Class for Holding Items
    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        // Main Attributes
        private WeakReference<OnItemClickListener> listenerRef;
        private CardView vehicleCard;
        private ImageView vehicleIcon;
        private TextView vehicleTextPrimary, vehicleTextSecondary;

        // Constructor
        public ItemViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            // Assigning UI
            vehicleCard = itemView.findViewById(R.id.vehicleCard);
            vehicleIcon = itemView.findViewById(R.id.vehicleIcon);
            vehicleTextPrimary = itemView.findViewById(R.id.vehicle_textPrimary);
            vehicleTextSecondary = itemView.findViewById(R.id.vehicle_textSecondary);

            listenerRef = new WeakReference<>(listener);
            itemView.setOnClickListener(this);
        }

        // Main OnClick Action
        @Override
        public void onClick(View v) {
            listenerRef.get().OnClick(getAdapterPosition());
        }
    } // End of Class: ItemViewHolder

    // Overriding Functions
    @NonNull
    @Override
    public VehicleRegistrationAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.vehicle_card, parent, false);
        return new ItemViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleRegistrationAdapter.ItemViewHolder holder, int position)
    {
        VehicleTypeItem currentItem = mItemList.get(position);

        // Assigning Holder the layout vars
        holder.vehicleTextPrimary.setText(currentItem.getVehicleMakeModel());
        holder.vehicleTextSecondary.setText(currentItem.getStatus());
        holder.vehicleIcon.setImageResource(currentItem.getVehicleImage());
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }
}

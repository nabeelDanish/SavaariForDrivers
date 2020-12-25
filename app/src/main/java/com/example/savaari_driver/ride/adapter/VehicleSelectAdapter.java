package com.example.savaari_driver.ride.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.savaari_driver.R;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class VehicleSelectAdapter extends RecyclerView.Adapter<VehicleSelectAdapter.ItemViewHolder> {

    // Main Attributes
    private ArrayList<VehicleTypeItem> mItemList;
    private OnItemClickListener listener;

    // Constructor
    public VehicleSelectAdapter(ArrayList<VehicleTypeItem> itemList, OnItemClickListener listener) {
        mItemList = itemList;
        this.listener = listener;
    }

    // Static Class for Holding Items
    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        // Main Attributes
        private WeakReference<OnItemClickListener> listenerRef;
        public LinearLayout rideTypeConfig;
        private ImageView rideTypeImage;
        private TextView rideTypeName, rideTypePrice;

        // Constructor
        public ItemViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            rideTypeConfig = itemView.findViewById(R.id.ride_type_config);
            rideTypeImage = itemView.findViewById(R.id.ride_type_img);
            rideTypeName = itemView.findViewById(R.id.ride_type_name);
            rideTypePrice = itemView.findViewById(R.id.ride_type_price);

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
    public VehicleSelectAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_type_card, parent, false);
        return new ItemViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleSelectAdapter.ItemViewHolder holder, int position)
    {
        VehicleTypeItem currentItem = mItemList.get(position);

        // Assigning Holder the layout vars
        holder.rideTypeImage.setImageResource(currentItem.getVehicleImage());
        holder.rideTypeName.setText(currentItem.getVehicleMakeModel());
        holder.rideTypePrice.setText(currentItem.getVehicleRideType());
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }
}

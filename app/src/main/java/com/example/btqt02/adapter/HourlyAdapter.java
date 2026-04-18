package com.example.btqt02.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.btqt02.R;
import com.example.btqt02.model.HourlyWeather;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.ViewHolder> {
    private List<HourlyWeather> hourlyList;
    private Context context;

    public HourlyAdapter(List<HourlyWeather> hourlyList) {
        this.hourlyList = hourlyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_hourly_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HourlyWeather weather = hourlyList.get(position);

        // Chuyển Unix Timestamp sang định dạng giờ (VD: 14:00)
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String time = sdf.format(new Date(weather.getTimestamp() * 1000));
        holder.tvTime.setText(position == 0 ? "Now" : time);

        // Hiển thị nhiệt độ
        holder.tvTemp.setText(Math.round(weather.getTemperature()) + "°");

        // Load Icon bằng Glide
        String iconCode = weather.getWeatherDescriptions().get(0).getIconCode();
        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        Glide.with(context).load(iconUrl).into(holder.ivIcon);
    }

    @Override
    public int getItemCount() {
        return hourlyList != null ? hourlyList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTemp;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime); // Đảm bảo ID này khớp trong XML
            tvTemp = itemView.findViewById(R.id.tvTemp);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
}
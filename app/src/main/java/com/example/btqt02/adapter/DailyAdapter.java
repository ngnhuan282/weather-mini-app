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
import com.example.btqt02.model.DailyWeather;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyAdapter extends RecyclerView.Adapter<DailyAdapter.ViewHolder> {
    private List<DailyWeather> dailyList;
    private Context context;

    public DailyAdapter(List<DailyWeather> dailyList) {
        this.dailyList = dailyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_daily_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyWeather day = dailyList.get(position);

        // Chuyển Unix Timestamp sang tên Thứ (VD: Monday)
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        String dayName = sdf.format(new Date(day.getTimestamp() * 1000));
        holder.tvDay.setText(dayName);

        // Hiển thị xác suất mưa (pop)
        int rainPercent = (int) (day.getRainChance() * 100);
        holder.tvRainChance.setText(rainPercent + "%");

        // Hiển thị nhiệt độ Max/Min
        holder.tvMaxTemp.setText(Math.round(day.getTempRange().maxTemp) + "°");
        holder.tvMinTemp.setText(Math.round(day.getTempRange().minTemp) + "°");

        // Load Icon
        String iconCode = day.getWeatherDescriptions().get(0).getIconCode();
        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        Glide.with(context).load(iconUrl).into(holder.ivIcon);
    }

    @Override
    public int getItemCount() {
        return dailyList != null ? dailyList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvRainChance, tvMaxTemp, tvMinTemp;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvRainChance = itemView.findViewById(R.id.tvRainChance);
            tvMaxTemp = itemView.findViewById(R.id.tvMaxTemp);
            tvMinTemp = itemView.findViewById(R.id.tvMinTemp);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
}
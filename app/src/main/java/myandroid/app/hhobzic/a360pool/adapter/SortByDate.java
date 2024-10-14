package myandroid.app.hhobzic.a360pool.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import myandroid.app.hhobzic.pool360.R;

public interface SortByDate {
    void sortByDateDescending();

    public static class IssueViewHolder extends RecyclerView.ViewHolder {
        TextView dateTimeTextView;
        TextView sensorNameTextView;
        TextView sensorValueTextView;
        TextView sensorTitleTextView;
        ImageView humanImage;
        TextView more;

        public IssueViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTimeTextView = itemView.findViewById(R.id.dateTimeTextView);
            sensorNameTextView = itemView.findViewById(R.id.sensorNameTextView);
            sensorValueTextView = itemView.findViewById(R.id.sensorValueTextView);
            sensorTitleTextView = itemView.findViewById(R.id.sensorTitleTextView);
            humanImage = itemView.findViewById(R.id.humanImage);
            more = itemView.findViewById(R.id.more);
        }
    }
}

package myandroid.app.hhobzic.a360pool.adapter;

// IssueAdapter.java
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import myandroid.app.hhobzic.a360pool.classes.Issue;
import myandroid.app.hhobzic.pool360.R;

public class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.IssueViewHolder> implements SortByDate {
    private List<Issue> issueList;
    private Context context;

    public IssueAdapter(Context context, List<Issue> issueList) {
        this.context = context;
        this.issueList = issueList;
    }

    @NonNull
    @Override
    public IssueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_issue, parent, false);
        return new IssueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IssueViewHolder holder, int position) {
        Issue issue = issueList.get(position);
        holder.dateTimeTextView.setText(issue.getCurrentDateTime());
        holder.sensorNameTextView.setText(issue.getSensor_name());
        holder.sensorValueTextView.setText(issue.getSensor_value());
        holder.sensorTitleTextView.setText(issue.getIssue_Title());
        if ("Status: Safety Alert!".equals(issue.getIssue_Title())) {
            holder.sensorValueTextView.setVisibility(View.GONE);
        } else {
            holder.sensorValueTextView.setVisibility(View.VISIBLE);
        }

        holder.more.setOnClickListener(v -> {
            showIssueDetailsDialog(issue);
        });

    }

    @Override
    public void sortByDateDescending() {
        Collections.sort(issueList, (issue1, issue2) -> {
            SimpleDateFormat format = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            try {
                Date date1 = format.parse(issue1.getCurrentDateTime());
                Date date2 = format.parse(issue2.getCurrentDateTime());
                return date2.compareTo(date1);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });
    }
    private void showIssueDetailsDialog(Issue issue) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_issue_details, null);

        TextView dialogDateTimeTextView = dialogView.findViewById(R.id.dialog_dateTimeTextView);
        TextView dialogSensorNameTextView = dialogView.findViewById(R.id.dialog_sensorNameTextView);
        TextView dialogSensorValueTextView = dialogView.findViewById(R.id.dialog_sensorValueTextView);
        ImageView humanImage = dialogView.findViewById(R.id.humanImage);
        Button dialogOkButton = dialogView.findViewById(R.id.dialog_ok_button);


        dialogDateTimeTextView.setText(issue.getCurrentDateTime());
        dialogSensorNameTextView.setText(issue.getSensor_name());

        String sensorValue = issue.getSensor_value();
        String sensorCategory = sensorValue.split(" ", 2)[0];
        if (sensorCategory.equals("High")) {
            dialogSensorValueTextView.setVisibility(View.VISIBLE);
            if (issue.getSensor_name().equals("pH Level")){
                dialogSensorValueTextView.setText("Adding pool acid: Add pool acid (such as hydrochloric acid) according to the instructions on the \n" +
                        "package. Start with a small amount, then check the pH again after a few hours.");
            } else if (issue.getSensor_name().equals("Water Level")) {
                dialogSensorValueTextView.setText("Water pumping: If the water level is too high, use a pump to pump the excess water and release \n" +
                        "it into a suitable drainage system.\n" +
                        " Drainage System Check: Make sure the pool's drainage system is working properly.");
            }else if (issue.getSensor_name().equals("Temperature")) {
                dialogSensorValueTextView.setText("Shading the pool: install canopies or shade nets over the pool to reduce the temperature.\n" +
                        "Reducing use of the pool heater: If you have a pool heater, set it to a lower temperature or turn \n" +
                        "it off if not needed.\n" +
                        "Adding cold water: Add cold water to the pool carefully to lower the temperature.");
            }else if (issue.getSensor_name().equals("Water Clarity")) {
                dialogSensorValueTextView.setText("Checking filters: check the condition of the filters and clean them if necessary.\n" +
                        " Adding chlorine: Add chlorine to the pool according to the instructions, making sure the chlorine \n" +
                        "level remains between 1 and 3 ppm (parts per million).\n" +
                        "Pumping and Flushing: Pump the bottom of the pool and flush the pool if necessary to remove \n" +
                        "dirt and particles");
            }

        }else if (sensorCategory.equals("Low")){
            dialogSensorValueTextView.setVisibility(View.VISIBLE);
            if (issue.getSensor_name().equals("pH Level")){
                dialogSensorValueTextView.setText("Adding basic material: Add basic material to the pool (such as sodium bicarbonate) according to \n" +
                        "the instructions on the package. Start with a small amount, then check the pH again after a few \n" +
                        "hours.\n" +
                        " Daily test: Continue to test the pH every two days until the level stabilizes between 7.2 and 7.6.");
            } else if (issue.getSensor_name().equals("Water Level")) {
                dialogSensorValueTextView.setText("Adding water: Add water to the pool until the level stabilizes at the correct height (usually in the \n" +
                        "middle of the opening of the skimmer system).\n" +
                        "Checking the filling system: Make sure the automatic filling system (if present) is working \n" +
                        "properly");
            }else if (issue.getSensor_name().equals("Temperature")) {
                dialogSensorValueTextView.setText("Heating the pool: use the pool heater or heat pump to raise the temperature to a comfortable \n" +
                        "level.\n" +
                        "Covering the pool at night: Covering the pool at night can help retain heat and reduce heat loss.");
            }else if (issue.getSensor_name().equals("Water Clarity")) {
                dialogSensorValueTextView.setText("Checking the chlorine level: Make sure the chlorine level remains between 1 and 3 ppm.\n" +
                        "Keeping the filters clean: Even if the water looks clear, keeping the filters clean is critical to \n" +
                        "preventing dirt build-up in the future");
            }
        }else {

            Bitmap bitmap =convertBase64ToBitmap(sensorValue);
            humanImage.setVisibility(View.VISIBLE);
            dialogSensorValueTextView.setVisibility(View.GONE);
            humanImage.setImageBitmap(bitmap);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        dialogOkButton.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }

    private Bitmap convertBase64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }


    @Override
    public int getItemCount() {
        return issueList.size();
    }

}
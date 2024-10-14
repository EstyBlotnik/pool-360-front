package myandroid.app.hhobzic.a360pool.fragments;

// HistoryFragment.java
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import myandroid.app.hhobzic.a360pool.adapter.IssueAdapter;
import myandroid.app.hhobzic.a360pool.classes.GetUsersData;
import myandroid.app.hhobzic.a360pool.classes.Issue;
import myandroid.app.hhobzic.a360pool.classes.LoadingDialog;
import myandroid.app.hhobzic.a360pool.retrofit.ApiClient;
import myandroid.app.hhobzic.a360pool.retrofit.ApiService;
import myandroid.app.hhobzic.pool360.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private IssueAdapter issueAdapter;
    private LoadingDialog loadingDialog;
    private List<Issue> issueList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        issueAdapter = new IssueAdapter(requireContext(),issueList);
        recyclerView.setAdapter(issueAdapter);

        fetchIssues();

        return view;
    }

    private void fetchIssues() {
        loadingDialog = new LoadingDialog(requireContext(), "Loading...");
        loadingDialog.show();

        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        Call<Map<String, Issue>> call = apiService.getIssues();
        call.enqueue(new Callback<Map<String, Issue>>() {
            @Override
            public void onResponse(Call<Map<String, Issue>> call, Response<Map<String, Issue>> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Issue> responseData = response.body();
                    issueList.clear();
                    for (Map.Entry<String, Issue> entry : responseData.entrySet()) {
                        issueList.add(entry.getValue());
                    }
                    issueAdapter.sortByDateDescending();
                    issueAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to fetch issues", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Issue>> call, Throwable t) {

            }
        });
    }
    public void refreshData() {
        fetchIssues();
    }
}

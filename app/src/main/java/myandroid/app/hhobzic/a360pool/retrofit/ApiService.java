package myandroid.app.hhobzic.a360pool.retrofit;

import java.util.Map;

import myandroid.app.hhobzic.a360pool.classes.ApiResponse;
import myandroid.app.hhobzic.a360pool.classes.CheckMailStatus;
import myandroid.app.hhobzic.a360pool.classes.DetectedPersons;
import myandroid.app.hhobzic.a360pool.classes.EmailRequest;
import myandroid.app.hhobzic.a360pool.classes.GetUsersData;
import myandroid.app.hhobzic.a360pool.classes.Issue;
import myandroid.app.hhobzic.a360pool.classes.IssueResponse;
import myandroid.app.hhobzic.a360pool.classes.LoginRequest;
import myandroid.app.hhobzic.a360pool.classes.LoginResponse;
import myandroid.app.hhobzic.a360pool.classes.OtpRequest;
import myandroid.app.hhobzic.a360pool.classes.PasswordModelClass;
import myandroid.app.hhobzic.a360pool.classes.RequestAlerts;
import myandroid.app.hhobzic.a360pool.classes.RequestWaterLevel;
import myandroid.app.hhobzic.a360pool.classes.SensorData;
import myandroid.app.hhobzic.a360pool.classes.SignupRequest;
import myandroid.app.hhobzic.a360pool.classes.SignupResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @POST("signup")
    Call<SignupResponse> signup(@Body SignupRequest signupRequest);

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("get_user/{email}")
    Call<GetUsersData> getUserData(@Path("email") String email);

    @POST("verify_email")
    Call<CheckMailStatus> mailStatus(@Body LoginRequest loginRequest);

    @POST("update_password")
    Call<PasswordModelClass> updatePassword(@Body PasswordModelClass loginRequest);

    @GET("sensor_data")
    Call<SensorData> getSensorData();

    @POST("save_issue")
    Call<Void> saveIssue(@Body IssueResponse issueResponse);

    @GET("get_issue")
    Call<Map<String, Issue>> getIssues();

    @POST("update_username")
    Call<Void> updateUserName(@Body GetUsersData GetUsersData);

    @POST("update_water_level")
    Call<Void> saveWaterLevel(@Body RequestWaterLevel RequestWaterLevel);

    @POST("update_alerts")
    Call<Void> updateAlerts(@Body RequestAlerts requestAlerts);

    @POST("detect")
    Call<DetectedPersons> detectPerson(@Body DetectedPersons request);

    @GET("/status")
    Call<DetectedPersons> getStatus();

    @POST("send_otp")
    Call<ApiResponse> sendOtp(@Body EmailRequest emailRequest);

    @POST("verify_otp")
    Call<ApiResponse> verifyOtp(@Body OtpRequest otpRequest);




}

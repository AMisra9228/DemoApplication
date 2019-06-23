package app.demo.demoapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.util.Arrays;

public class UserLogin extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    // Defining views
    private EditText editTextUId;
    private EditText editTextPassword;
    private AppCompatButton btnILogin;
    private ImageButton btn_fb,btn_twitter,btn_google;

    private GoogleApiClient googleApiClient;
    private static final int REQ_CODE = 9001;

    private LinearLayout base1,base2,home,baseLayout,btn_fbb,btn_googlee;

    private TextView unam,uemail,fb_data;
    private Button btn_signout,btn_fb_signout;

    private CheckBox remember;
    private SharedPreferences loginPreferences;

    public static String username = "";

//    private Boolean saveLogin;

    private static final String PREFS_NAME = "myPreference";
    private static final String PREF_USERNAME = "username";
    private static String PREF_PASSWORD = "password";

    private Toolbar mToolbar;

    CallbackManager callbackManager;
    LoginButton btn_fbb_login;
    private static final String EMAIL = "email";
    private static final String PROFILE = "public_profile";

    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        //Initializing views
        editTextUId = (EditText) findViewById(R.id.ed_cname);
        editTextPassword = (EditText) findViewById(R.id.ed_cpassword);

        btnILogin = (AppCompatButton) findViewById(R.id.btn_clog);

//        btn_fb = (ImageButton) findViewById(R.id.btn_fb);
//        btn_twitter = (ImageButton) findViewById(R.id.btn_twitter);
//        btn_google = (ImageButton) findViewById(R.id.btn_google);

        btn_fbb = (LinearLayout) findViewById(R.id.btn_fbb);
        btn_googlee = (LinearLayout) findViewById(R.id.btn_googlee);

        base1 = (LinearLayout) findViewById(R.id.base1);
        base2 = (LinearLayout) findViewById(R.id.base2);
        home = (LinearLayout) findViewById(R.id.uhome);
        baseLayout = (LinearLayout) findViewById(R.id.base_layout);

        unam = (TextView) findViewById(R.id.unam);
        uemail = (TextView) findViewById(R.id.uemail);

        fb_data = (TextView) findViewById(R.id.fb_data);

        btn_signout = (Button) findViewById(R.id.btn_signout);
        btn_fb_signout = (Button) findViewById(R.id.btn_fb_signout);

        remember = (CheckBox) findViewById(R.id.remember);

        btn_fbb.setOnClickListener(this);

        // Generate HashKey
        computePakageHash();

//        saveLogin = loginPreferences.getBoolean("saveLogin", true);
//        if ((saveLogin == true)){
//            editTextUId.setText(loginPreferences.getString("username", ""));
//            remember.setChecked(true);
//        }

        // Checking for retrieve UserDetails checked or not
        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        username = pref.getString(PREF_USERNAME, null);
        if(username != "") {

            getUser();
        }
        else {
            editTextUId.setText("");
            editTextPassword.setText("");
        }

        // User Login click listener
        btnILogin.setOnClickListener(this);

        // User GLogin click listener
        btn_googlee.setOnClickListener(this);

        // User GLogout click listener
        btn_signout.setOnClickListener(this);

        // User FbLogout click listener
        btn_fb_signout.setOnClickListener(this);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();

    }

    @Override
    public void onClick(View v) {

        if(!isConnected(UserLogin.this)) buildDialog(UserLogin.this).show();
        else if(v == btnILogin) {
            login();
        }
        else if (v == btn_fbb){
            fblogin();
        }
//        else if (v == btn_twitter){
//            /*Twitter login*/
//        }
        else if (v == btn_googlee){
            glogin();
        }
        else if(v == btn_signout){
            gsignout();
        }
        else if(v == btn_fb_signout){
            fbsignout();
        }

    }

    private void getFbData(JSONObject object){
        try {

            base1.setVisibility(View.GONE);
            base2.setVisibility(View.GONE);
            baseLayout.setBackgroundColor(Color.parseColor("#e3e3e3"));
            home.setVisibility(View.VISIBLE);
            btn_signout.setVisibility(View.GONE);
//            btn_fb_signout.setVisibility(View.VISIBLE);

            unam.setText(object.getString(EMAIL));
            uemail.setVisibility(View.GONE);
            Log.d("Fb_Data",EMAIL);

//            String data = object.getString(EMAIL);
//            Toast.makeText(this,data,Toast.LENGTH_SHORT).show();
//            Toast.makeText(this,EMAIL,Toast.LENGTH_SHORT);

        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    private void fblogin(){

        btn_fbb_login = (LoginButton)findViewById(R.id.btn_fbb_login);

        btn_fbb_login.performClick();
        callbackManager = CallbackManager.Factory.create();
        btn_fbb_login.setReadPermissions(Arrays.asList(EMAIL));

        btn_fbb_login.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mDialog = new ProgressDialog(UserLogin.this);
                mDialog.setMessage("Retrieving data...");
                mDialog.show();

                String accesstoken = loginResult.getAccessToken().getToken();

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        mDialog.dismiss();
//                        Log.d("response",response.toString());
                        getFbData(object);
                    }
                });

                //Request Graph request
                Bundle parameters = new Bundle();
                parameters.putString("fields","id,email");
                request.setParameters(parameters);
                request.executeAsync();
            }
            @Override
            public void onCancel() {
                Toast.makeText(UserLogin.this,"Login Cancel",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

        //If already login
        if(AccessToken.getCurrentAccessToken() != null){
            fb_data.setText(AccessToken.getCurrentAccessToken().getUserId());
//            String data = AccessToken.getCurrentAccessToken().getUserId();
//            Toast.makeText(this,data,Toast.LENGTH_SHORT).show();
//            Log.d("Fb_Data",AccessToken.getCurrentAccessToken().getUserId());
        }
    }

    private void login(){

        //Getting values from edit texts
        final String UId = editTextUId.getText().toString().trim();
        final String Pwd = editTextPassword.getText().toString().trim();

        //Check Id or Password field is empty or not
        if (UId.isEmpty()) {
//            editTextUId.setError("Field can't be empty");
            Toast.makeText(UserLogin.this,"Enter valid User ID!",Toast.LENGTH_SHORT).show();
        } else if (Pwd.isEmpty()) {
//            editTextPassword.setError("Field can't be empty");
            Toast.makeText(UserLogin.this,"Enter valid Password!",Toast.LENGTH_SHORT).show();
        }
        else {
            if(remember.isChecked()){
                Boolean boolChecked = remember.isChecked();
                loginPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = loginPreferences.edit();
                editor.putBoolean("saveLogin", boolChecked);
                editor.putString(PREF_USERNAME,UId);
                editor.apply();
            }
            else {
                loginPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = loginPreferences.edit();
                editor.clear();
                editor.apply();
            }

            //save username
//            rememberMe(UId);

            Toast.makeText(UserLogin.this,"Under Construction...",Toast.LENGTH_SHORT).show();
        }
    }

    private void getUser(){
        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        username = pref.getString(PREF_USERNAME, null);

//        String flag = pref.getString("flag",null);
//        String password = pref.getString(PREF_PASSWORD, null);
//        if (pref.contains(PREFS_NAME)) {
            editTextUId.setText(username);

//            Toast.makeText(this,username,Toast.LENGTH_SHORT).show();

            Boolean saveLogin = pref.getBoolean("saveLogin",false);
            remember.setChecked(saveLogin);

//        }
//        else {
//            remember.setChecked(false);
//            editTextUId.setText("");
//        }
    }

    public void rememberMe(String user){
        if(remember.isChecked()) {
            String flag = "1";
            Boolean saveLogin = true;

            //save username in SharedPreferences
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString(PREF_USERNAME, user)
                    .putBoolean("saveLogin",saveLogin)
                    .apply();
        }
        else{
            String flag = "0";
            Boolean saveLogin = false;

            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString(PREF_USERNAME, "")
                    .apply();


        }
    }

    // Google SignIn
    private void glogin(){

        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent,REQ_CODE);
    }

    // Google Signout
    private void gsignout(){
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                updateUI(false);
            }
        });
    }

    // Facebook Signout
    private void fbsignout(){

//        Toast.makeText(UserLogin.this,"Clicked",Toast.LENGTH_SHORT).show();
        LoginManager.getInstance().logOut();
//        Toast.makeText(UserLogin.this,"Clicked",Toast.LENGTH_SHORT).show();
//        LoginManager mLoginManager = LoginManager.getInstance();
//        mLoginManager.logOut();

        AccessToken.setCurrentAccessToken(null);

        Intent intent = new Intent(UserLogin.this,UserLogin.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();

//        disconnectFromFacebook();

//        baseLayout.setBackgroundResource(R.drawable.nature);
//        base1.setVisibility(View.VISIBLE);
//        base2.setVisibility(View.VISIBLE);
//        home.setVisibility(View.GONE);

    }

    public void disconnectFromFacebook() {

        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {

                LoginManager.getInstance().logOut();

            }
        }).executeAsync();
    }

    private void handleResult(GoogleSignInResult result){

        if (result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            String name = account.getDisplayName();
            String email = account.getEmail();

//            Intent intent = new Intent(this,UserHome.class);
//            intent.putExtra("name",name);
//            intent.putExtra("email",email);
//            startActivity(intent);

            unam.setText(name);
            uemail.setText(email);
            baseLayout.setBackgroundColor(Color.parseColor("#e3e3e3"));

            updateUI(true);

        }
        else{
            updateUI(false);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private  void updateUI(boolean isLogin){

        if(isLogin){

            // Set Actionbar
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(mToolbar);

            if(getSupportActionBar() != null) {
                mToolbar.setVisibility(View.VISIBLE);
//                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Home");

            }

            base1.setVisibility(View.GONE);
            base2.setVisibility(View.GONE);
            btn_fb_signout.setVisibility(View.GONE);
            home.setVisibility(View.VISIBLE);
        }
        else {
//            baseLayout.setBackgroundColor(Color.parseColor("#BEE6FF"));
            if (getSupportActionBar() != null) {
                mToolbar.setVisibility(View.GONE);
            }

            baseLayout.setBackgroundResource(R.drawable.nature);
            base1.setVisibility(View.VISIBLE);
            base2.setVisibility(View.VISIBLE);
            home.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_CODE){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
        else if(requestCode != REQ_CODE){
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Check Internet Connection
    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if((mobile != null &&mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) return true;
            else return false;
        } else
            return false;
    }

    // Show message in dialog
    public AlertDialog.Builder buildDialog(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("No Internet Connection!");
        builder.setMessage("Enable Mobile Data or WIFI connection.");

//        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//                finish();
//            }
//        });

        return builder;
    }

    //Generate HashKey
    private void computePakageHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "app.demo.demoapplication",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }

    }
}

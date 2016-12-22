package com.junyenhuang.birdhouse;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.junyenhuang.birdhouse.http.WebRequest;
import com.junyenhuang.birdhouse.items.Credentials;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    //Keep track of the login task to ensure we can cancel it if requested.
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mLoginView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE);
        int user_id = prefs.getInt(Constants.USER_ID, 0);

        if(user_id > 0) {
            startActivity(new Intent(MainActivity.this, EntryActivity.class));
            finish();
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);

        // Set up the login form.
        ImageView urlImage = (ImageView)findViewById(R.id.url_image);
        Picasso.with(this).load(R.drawable.login).into(urlImage);
        urlImage.setLongClickable(true);
        urlImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                View viewInflated = LayoutInflater.from(v.getContext())
                        .inflate(R.layout.dialog_url, (ViewGroup)findViewById(android.R.id.content), false);
                final EditText url = (EditText)viewInflated.findViewById(R.id.input_url);
                String inputUrl = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE)
                        .getString(Constants.BASE_URL_TAG, "");
                url.setText(inputUrl);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setView(viewInflated);
                builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!url.getText().toString().isEmpty()) {
                            SharedPreferences.Editor editor =
                                    getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE).edit();
                            String inputUrl = url.getText().toString();
                            inputUrl = inputUrl.replace("", "");
                            String lastChar = inputUrl.substring(inputUrl.length() - 1);
                            if(lastChar.equals("")) {
                                inputUrl = inputUrl.substring(0, inputUrl.length() - 1 - 1);
                            }
                            editor.putString(Constants.BASE_URL_TAG, inputUrl);
                            editor.apply();
                        }
                    }
                });
                builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                return true;
            }
        });
        mLoginView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        ImageButton mEmailSignInButton = (ImageButton) findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkAvailable()) {
                    attemptLogin();
                } else {
                    mLoginView.setError(getString(R.string.error_network));
                    mLoginView.requestFocus();
                }
            }
        });
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    // Callback received when a permissions request has been completed.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mLoginView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mLoginView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mLoginView.setError(getString(R.string.error_field_required));
            focusView = mLoginView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 1;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private final String mLogin;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mLogin = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            boolean DEBUG = false;
            if(!DEBUG) {
                SharedPreferences prefs = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE);
                String savedUrl = prefs.getString(Constants.BASE_URL_TAG, "");
                StringBuilder loginURL = new StringBuilder();
                loginURL.append("").append(savedUrl).append("");
                //loginURL.append(Constants.BASE_URL).append("loginStatus?");
                loginURL.append("");
                loginURL.append("").append(mLogin);
                loginURL.append("").append(mPassword);
                String jsonStr = new WebRequest().makeWebServiceCall(loginURL.toString(), WebRequest.GETRequest);
                Credentials credentials = parseCredentials(jsonStr);
                if(credentials == null) {
                    return false;
                }
                if(credentials.getStatus() && credentials.getUserId() > 0) {
                    SharedPreferences.Editor editor = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE).edit();
                    editor.putInt(Constants.USER_ID, credentials.getUserId());
                    editor.putString(Constants.USER_TYPE, credentials.getCredentialType());
                    editor.putString(Constants.USER_NAME, credentials.getUsername());
                    editor.putString(Constants.USER_PWD, mPassword);
                    editor.apply();
                    return true;
                }
            } else {
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                startActivity(new Intent(MainActivity.this, EntryActivity.class));
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private Credentials parseCredentials(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        try{
            JSONObject userObj = new JSONObject(jsonString);
            Credentials credentials = new Credentials();
            credentials.setStatus(userObj.getBoolean(Constants.JSON_STATUS));
            credentials.setCredentialType(userObj.getString(Constants.JSON_TYPE));
            credentials.setUserId(userObj.getInt(Constants.USER_ID));
            credentials.setUsername(userObj.getString(Constants.JSON_USERNAME));
            return credentials;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}

package com.junyenhuang.birdhouse;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.junyenhuang.birdhouse.http.WebRequest;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class PasswordActivity extends AppCompatActivity {
    //Keep track of the login task to ensure we can cancel it if requested.
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText oldPasswordView;
    private EditText newPasswordView, confirmPasswordView;
    private EditText usernameView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.action_account));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up the login form.
        Picasso.with(this).load(R.drawable.name).into((ImageView)findViewById(R.id.name_icon));
        Picasso.with(this).load(R.drawable.password).into((ImageView)findViewById(R.id.password_icon));
        String currentName = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE).getString(Constants.USER_NAME, "");
        usernameView = (EditText)findViewById(R.id.new_name);
        usernameView.setText(currentName);
        oldPasswordView = (EditText) findViewById(R.id.old_password);
        newPasswordView = (EditText) findViewById(R.id.new_password);
        confirmPasswordView = (EditText)findViewById(R.id.confirm_password);

        Button mEmailSignInButton = (Button) findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword();
            }
        });
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        Button logoutButton = (Button)findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PasswordActivity.this);
                builder.setTitle(R.string.app_name).setMessage(R.string.logout_message);
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setPositiveButton(R.string.action_logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE).edit().clear().apply();
                        Intent intent = new Intent(PasswordActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
                builder.create().show();
            }
        });
    }

    // Callback received when a permissions request has been completed.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
    }

    private void changePassword() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        oldPasswordView.setError(null);
        usernameView.setError(null);
        newPasswordView.setError(null);
        confirmPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String inputOldPassword = oldPasswordView.getText().toString();
        String username = usernameView.getText().toString();
        String newPassword = newPasswordView.getText().toString();
        String confirmPassword = confirmPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        String oldPassword = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE).getString(Constants.USER_PWD, "");
        // Check for old password
        if (TextUtils.isEmpty(inputOldPassword)) {
            oldPasswordView.setError(getString(R.string.error_field_required));
            focusView = oldPasswordView;
            cancel = true;
        } else if(!oldPassword.equals(inputOldPassword)) {
            oldPasswordView.setError(getString(R.string.pwd_error));
            focusView = oldPasswordView;
            cancel = true;
        } else if(TextUtils.isEmpty(username)) {
            if(TextUtils.isEmpty(newPassword) && TextUtils.isEmpty(confirmPassword)) {
                usernameView.setError(getString(R.string.error_field_required));
                focusView = usernameView;
                cancel = true;
            } else if(TextUtils.isEmpty(newPassword) && !TextUtils.isEmpty(confirmPassword)) {
                newPasswordView.setError(getString(R.string.error_field_required));
                focusView = newPasswordView;
                cancel = true;
            } else if(!TextUtils.isEmpty(newPassword) && TextUtils.isEmpty(confirmPassword)) {
                confirmPasswordView.setError(getString(R.string.error_field_required));
                focusView = confirmPasswordView;
                cancel = true;
            } else if(!newPassword.equals(confirmPassword)) {
                newPasswordView.setError(getString(R.string.pwd_match_error));
                focusView = newPasswordView;
                confirmPasswordView.getText().clear();
                cancel = true;
            }
        } else if(TextUtils.isEmpty(newPassword) && !TextUtils.isEmpty(confirmPassword)) {
            newPasswordView.setError(getString(R.string.error_field_required));
            focusView = newPasswordView;
            cancel = true;
        } else if(!TextUtils.isEmpty(newPassword) && TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordView.setError(getString(R.string.error_field_required));
            focusView = confirmPasswordView;
            cancel = true;
        } else if(!newPassword.equals(confirmPassword)) {
            newPasswordView.setError(getString(R.string.pwd_match_error));
            focusView = newPasswordView;
            confirmPasswordView.getText().clear();
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
            mAuthTask = new UserLoginTask(username, newPassword);
            mAuthTask.execute((Void) null);
        }
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
    public class UserLoginTask extends AsyncTask<Void, Void, Void> {
        private final String mUsername;
        private final String mNewPassword;

        UserLoginTask(String username, String newPassword) {
            mUsername = username;
            mNewPassword = newPassword;
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return null;
            }

            boolean DEBUG = false;
            if(!DEBUG) {
                StringBuilder url = new StringBuilder();
                SharedPreferences prefs = getSharedPreferences(Constants.PREF_LOGIN, MODE_PRIVATE);
                int userId = prefs.getInt(Constants.USER_ID, 0);
                String savedUrl = prefs.getString(Constants.BASE_URL_TAG, "");
                url.append("").append(savedUrl).append("").append(userId);
                //url.append(Constants.BASE_URL).append("users/").append(userId);
                HashMap<String, String> map = new HashMap<>();
                new WebRequest().makeWebServiceCall(url.toString(), WebRequest.POSTRequest, map);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mAuthTask = null;
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

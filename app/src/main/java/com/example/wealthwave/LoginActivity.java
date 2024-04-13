package com.example.wealthwave;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.User;

public class LoginActivity extends AppCompatActivity {

		FirebaseAuth firebaseAuth;
		FirebaseUser firebaseUser;
		FirebaseFirestore firestore;

		//Check if user is logged in then redirect them directly to the dashboard
		@Override
		protected void onStart() {
				super.onStart();

				firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
				if (firebaseUser != null && firebaseUser.isEmailVerified()){
						Intent dashboardIntent = new Intent(this, DashboardActivity.class);
						startActivity(dashboardIntent);
						finish();
				}

		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
				setContentView(R.layout.activity_login);

				Button registerBtn = findViewById(R.id.sign_up);
				Button loginBtn = findViewById(R.id.login);
				Button forgotPasswordBtn = findViewById(R.id.forgot_password);
				TextInputLayout loginEmailField = findViewById(R.id.email_address);
				TextInputLayout loginPasswordField = findViewById(R.id.password);
				TextInputEditText email = findViewById(R.id.login_email);
				TextInputEditText password = findViewById(R.id.login_password);



				forgotPasswordBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
						}
				});

				registerBtn.setOnClickListener(v -> {
						try {
								Intent registerIntent = new Intent(this, RegisterActivity.class);
								startActivity(registerIntent);
								finish();
						} catch (Error e) {
								throw new RuntimeException(e);
						}
				});

				loginBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
								//Remove errors
								loginEmailField.setErrorEnabled(false);
								loginPasswordField.setErrorEnabled(false);

								//Validate inputs
								if (email.getText().toString().isEmpty()) {
										loginEmailField.setErrorEnabled(true);
										loginEmailField.setError("Email is required");
										return;
								}
								if (!ValidateEmail(email.getText().toString().trim())){
										loginEmailField.setErrorEnabled(true);
										loginEmailField.setError("Please enter a valid email address");
										return;
								}
								if (password.getText().toString().isEmpty()) {
										loginPasswordField.setErrorEnabled(true);
										loginPasswordField.setError("Password is required");
										return;
								}
								if (!isPasswordValid(password.getText().toString().trim())) {
										loginPasswordField.setErrorEnabled(true);
										loginPasswordField.setError("Your password must be at least 9 characters long, include at least one uppercase letter, one lowercase letter, one digit, and one special character.");
										return;
								}
								try {
										loginBtn.setEnabled(false);
										User user = new User();
										user.setEmailAddress(email.getText().toString().trim().toLowerCase());
										user.setPassword(password.getText().toString().trim());
										LoginUser(user);
								}
								catch(Error e){
										throw new RuntimeException(e);
								}
						}
				});
		}

		private boolean ValidateEmail(String email) {
				String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";

				Pattern pattern = Pattern.compile(emailRegex);
				Matcher matcher = pattern.matcher(email);
				return matcher.matches();
		}

		private boolean isPasswordValid(String password){
				String passwordRegex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

				Pattern pattern = Pattern.compile(passwordRegex);
				Matcher matcher = pattern.matcher(password);
				return matcher.matches();
		}
		//Authenticate and login the user
		private void LoginUser(User user){

				Button loginBtn = findViewById(R.id.login);


				firebaseAuth = FirebaseAuth.getInstance();
				firebaseAuth.signInWithEmailAndPassword(user.getEmailAddress(), user.getPassword())
								.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
										@Override
										public void onComplete(@NonNull Task<AuthResult> task) {
												if (task.isSuccessful()) {
														Snackbar loginConfirm = Snackbar.make(LoginActivity.this.getCurrentFocus(), "Login Successful\nWelcome back", Snackbar.LENGTH_SHORT);
														loginConfirm.show();
														loginBtn.setEnabled(true);
														if (loginBtn.isEnabled()) {
																Intent dashboardIntent = new Intent(LoginActivity.this, DashboardActivity.class);
																startActivity(dashboardIntent);
																finish();
														}
												}
										}
								}).addOnFailureListener(this, new OnFailureListener() {
										@Override
										public void onFailure(@NonNull Exception e) {
												Snackbar.make(loginBtn, "Invalid email or password\nPlease try again", Snackbar.LENGTH_SHORT)
																.setBackgroundTint(getColor(R.color.md_theme_light_error)).show();
												loginBtn.setEnabled(true);
										}
								});
		}

		//Show toast message function
		private void showToast(CharSequence toastMessage){
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(this /* MyActivity */, toastMessage, duration);
				toast.show();
		}
}


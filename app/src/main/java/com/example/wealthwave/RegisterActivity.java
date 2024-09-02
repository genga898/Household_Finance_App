package com.example.wealthwave;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.User;

public class RegisterActivity extends AppCompatActivity {

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
				setContentView(R.layout.activity_register);

				Button loginBtn = findViewById(R.id.login_page);





				loginBtn.setOnClickListener(v -> {
						try {
								Intent loginIntent = new Intent(this, LoginActivity.class);
								startActivity(loginIntent);
								finish();
						} catch (Error e) {
								throw new RuntimeException(e);
						}
				});

				RegisterUser();

		}
		private void RegisterUser(){

				Button registerBtn = findViewById(R.id.register);
				TextInputLayout emailText = findViewById(R.id.reg_email_address);
				TextInputLayout passwordText = findViewById(R.id.reg_password);
				TextInputLayout confirmPasswordText = findViewById(R.id.confirm_password);
				TextInputLayout usernameText = findViewById(R.id.user_name);
				TextInputEditText password = findViewById(R.id.reg_pass_text);
				TextInputEditText email = findViewById(R.id.email_text);
				TextInputEditText confirmPassword = findViewById(R.id.confirm_pass_text);
				TextInputEditText username = findViewById(R.id.users_name);



				registerBtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
								//Removing error state
								usernameText.setErrorEnabled(false);
								emailText.setErrorEnabled(false);
								passwordText.setErrorEnabled(false);
								confirmPasswordText.setErrorEnabled(false);

								//Validate the inputs to ensure we are not entering null data
								if (username.getText().toString().equals("")){
										usernameText.setErrorEnabled(true);
										usernameText.setError("Username is required");
										return;
								}
								if (TextUtils.isEmpty(email.getText().toString())){
										emailText.setErrorEnabled(true);
										emailText.setError("Email is required");
										return;
								}
								if (!ValidateEmail(email.getText().toString().trim())) {
										emailText.setErrorEnabled(true);
										emailText.setError("Please enter a valid email address");
										return;
								}
								if (TextUtils.isEmpty(password.getText().toString())){
										passwordText.setErrorEnabled(true);
										passwordText.setError("Password is required");
										return;
								}
								if (!isPasswordValid(password.getText().toString().trim())) {
										passwordText.setErrorEnabled(true);
										password.setError("Your password must be at least 9 characters long, include at least one uppercase letter, one lowercase letter, one digit, and one special character.");
										return;
								}
								if (!confirmPassword.getText().toString().trim().equals(password.getText().toString().trim())){
										confirmPasswordText.setErrorEnabled(true);
										confirmPasswordText.setError("Passwords do not match");
										return;
								}


								try {
										registerBtn.setEnabled(false);
										User user = new User();

										user.setName(username.getText().toString().trim());
										user.setEmailAddress(email.getText().toString().trim().toLowerCase());
										user.setPassword(password.getText().toString().trim());
										user.setRole("user");
										CreateUser(user);
								} catch (Error error) {
										throw new RuntimeException(error);
								}
						}
				});
		}

		private void CreateUser(User user){
				if (user == null){
						return;
				}

				/* Create user account */
				Button registerBtn = findViewById(R.id.register);

				firebaseAuth = FirebaseAuth.getInstance();
				firebaseAuth.createUserWithEmailAndPassword(user.getEmailAddress(), user.getPassword())
								.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
										@Override
										public void onComplete(@NonNull Task<AuthResult> task) {
												if (task.isSuccessful()) {
														Snackbar.make(registerBtn, "Account created successfully", Snackbar.LENGTH_SHORT)
																		.show();
														registerBtn.setEnabled(true);

														/* Get newly registered user and send a verification email */
														firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
														if (firebaseUser != null){

																//A map to store essential user information in the Firestore database
																//Create a userInfo object
																Map<String, Object> userInfo = new HashMap<>();
																userInfo.put("name",user.getName());
																userInfo.put("email", user.getEmailAddress());

																/* Store extra information about users */
																firestore = FirebaseFirestore.getInstance();

																firestore.collection("user_details")
																				.document(firebaseUser.getUid())
																				.set(userInfo)
																				.addOnSuccessListener(new OnSuccessListener<Void>() {
																						@Override
																						public void onSuccess(Void unused) {
																								Log.d("Success","Information saved");
																						}
																				}).addOnFailureListener(new OnFailureListener() {
																						@Override
																						public void onFailure(@NonNull Exception e) {
																								Log.d("Error", e.getMessage());
																						}
																				});
																firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
																		@Override
																		public void onComplete(@NonNull Task<Void> task) {
																				if (!firebaseUser.isEmailVerified() && !isFinishing()){
																						MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(RegisterActivity.this)
																										.setTitle("Confirm Email")
																										.setMessage(
																														String.format("Registration successful, an email has been sent to %s for verification. Kindly confirm your email address", firebaseUser.getEmail()))
																										.setNegativeButton("Close", new DialogInterface.OnClickListener() {
																												@Override
																												public void onClick(DialogInterface dialog, int which) {
																														dialog.dismiss();
																														Intent registerIntent = new Intent(RegisterActivity.this, LoginActivity.class);
																														startActivity(registerIntent);
																														finish();
																												}
																										});
																						builder.create();
																						builder.show();
																				}
																		}
																}).addOnFailureListener(new OnFailureListener() {
																		@Override
																		public void onFailure(@NonNull Exception e) {
																				Snackbar.make(registerBtn, "An error occurred while sending the email", Snackbar.LENGTH_SHORT).show();
																		}
																});
														}
												}
										}
								}).addOnFailureListener(this, new OnFailureListener() {
										@Override
										public void onFailure(@NonNull Exception e) {
												if (Objects.requireNonNull(e.getMessage()).contains("The email address is already in use by another account")){
														Snackbar.make(registerBtn, "This email address is already in use, kindly use a different email address", Snackbar.LENGTH_LONG)
																		.setBackgroundTint(getResources().getColor(R.color.md_theme_light_error))
																		.show();
												} else {
														Snackbar.make(registerBtn, "A problem occurred during creation of the account.\nPlease try again", Snackbar.LENGTH_SHORT)
																		.setBackgroundTint(getResources().getColor(R.color.md_theme_light_error))
																		.show();
														registerBtn.setEnabled(true);
												}
										}
								});
		}


		// Email regex validation
		private boolean ValidateEmail(String email){

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

		private void showToast(CharSequence toastMessage){
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(this /* MyActivity */, toastMessage, duration);
				toast.show();
		}
}
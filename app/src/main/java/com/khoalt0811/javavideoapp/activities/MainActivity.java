package com.khoalt0811.javavideoapp.activities; // Thay package name nếu khác

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem; // Ensure this is imported

import androidx.annotation.NonNull; // Ensure this is imported
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationBarView;

import com.khoalt0811.javavideoapp.R; // Import R
import com.khoalt0811.javavideoapp.databinding.ActivityMainBinding; // Import Binding
import com.khoalt0811.javavideoapp.fragments.FeedFragment;
import com.khoalt0811.javavideoapp.fragments.ProfileFragment;
import com.khoalt0811.javavideoapp.fragments.UploadFragment;
import com.khoalt0811.javavideoapp.viewmodels.AuthViewModel;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Check login status before setting up UI
        if (!authViewModel.isLoggedIn()) {
            Log.d(TAG, "User not logged in, redirecting to AuthActivity.");
            goToAuthActivity();
            return; // Exit onCreate early if not logged in
        }

        Log.d(TAG, "User logged in, setting up Bottom Navigation.");
        setupBottomNavigation();

        // Load default fragment only if it's the initial creation
        if (savedInstanceState == null) {
            Log.d(TAG, "Loading default fragment (FeedFragment).");
            binding.bottomNavigationView.setSelectedItemId(R.id.navigation_feed);
        }

        // Observe logout event
        observeLogout();
    }

    private void setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_feed) {
                Log.d(TAG, "Feed navigation item selected.");
                selectedFragment = new FeedFragment();
            } else if (itemId == R.id.navigation_upload) {
                Log.d(TAG, "Upload navigation item selected.");
                selectedFragment = new UploadFragment();
            } else if (itemId == R.id.navigation_profile) {
                Log.d(TAG, "Profile navigation item selected.");
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                Log.d(TAG, "Replacing fragment with: " + selectedFragment.getClass().getSimpleName());
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
                return true; // Event handled
            }
            return false; // Event not handled
        });
    }

    private void observeLogout() {
        // Observe authResult, if it becomes null, it means logout happened
        authViewModel.authResult.observe(this, authResponse -> {
            // Check if user is explicitly logged out (authResponse is null)
            // AND ensure the activity is not finishing to prevent issues during configuration changes
            if (authResponse == null && !authViewModel.isLoggedIn() && !isFinishing()) {
                Log.d(TAG, "Logout detected via authResult becoming null, redirecting to AuthActivity.");
                goToAuthActivity();
            }
        });
    }


    private void goToAuthActivity() {
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        // Clear back stack so user cannot go back to MainActivity after logout
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finish MainActivity
    }
}
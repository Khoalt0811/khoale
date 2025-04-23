package com.khoalt0811.javavideoapp.fragments; // Thay package name nếu khác

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.khoalt0811.javavideoapp.R; // Import R
import com.khoalt0811.javavideoapp.data.models.Profile;
import com.khoalt0811.javavideoapp.databinding.FragmentProfileBinding; // Import Binding
import com.khoalt0811.javavideoapp.viewmodels.AuthViewModel;
import com.khoalt0811.javavideoapp.viewmodels.VideoViewModel;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final String ARG_USER_ID = "user_id"; // Key for argument

    private FragmentProfileBinding binding;
    private AuthViewModel authViewModel;
    private VideoViewModel videoViewModel;
    private String profileUserIdToLoad; // User ID whose profile to load

    // Factory method to create instance with arguments (optional but good practice)
    public static ProfileFragment newInstance(String userId) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the user ID from arguments if available
        if (getArguments() != null) {
            profileUserIdToLoad = getArguments().getString(ARG_USER_ID);
            Log.d(TAG, "onCreate: Received User ID from arguments: " + profileUserIdToLoad);
        } else {
            Log.d(TAG, "onCreate: No arguments found.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        Log.d(TAG, "onCreateView: Layout inflated.");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: View created.");

        // Initialize ViewModels
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class); // Shared AuthViewModel
        VideoViewModel.VideoViewModelFactory factory = new VideoViewModel.VideoViewModelFactory(requireActivity().getApplication());
        videoViewModel = new ViewModelProvider(this, factory).get(VideoViewModel.class); // Fragment-specific VideoViewModel

        // Determine which profile to load: argument or current user
        if (profileUserIdToLoad == null) {
            profileUserIdToLoad = authViewModel.getCurrentUserId();
            Log.d(TAG, "onViewCreated: Loading profile for current logged-in user: " + profileUserIdToLoad);
        } else {
            Log.d(TAG, "onViewCreated: Loading profile for specified user ID: " + profileUserIdToLoad);
        }

        // Fetch profile details if user ID is available
        if (profileUserIdToLoad != null) {
            Log.d(TAG, "Fetching profile details for user: " + profileUserIdToLoad);
            videoViewModel.fetchProfileDetails(profileUserIdToLoad);
        } else {
            Log.e(TAG, "Profile User ID is null. Cannot load profile.");
            Toast.makeText(getContext(), R.string.error_loading_profile, Toast.LENGTH_SHORT).show();
            // Handle case where user ID is missing (e.g., show error state)
            showEmptyProfileState();
        }

        // Show/Hide logout button based on whether it's the current user's profile
        String currentLoggedInUserId = authViewModel.getCurrentUserId();
        if (profileUserIdToLoad != null && profileUserIdToLoad.equals(currentLoggedInUserId)) {
            binding.buttonLogout.setVisibility(View.VISIBLE);
            binding.buttonLogout.setOnClickListener(v -> {
                Log.d(TAG, "Logout button clicked.");
                authViewModel.logoutUser(); // ViewModel handles navigation via MainActivity observer
            });
        } else {
            binding.buttonLogout.setVisibility(View.GONE); // Hide logout if viewing someone else's profile
        }

        observeViewModel();
    }

    private void observeViewModel() {
        // Observe loading state (optional, add ProgressBar to layout if needed)
        videoViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG, "isLoading changed: " + isLoading);
            // binding.progressBarProfile.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe errors during profile fetch
        videoViewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                String errorMsg = getString(R.string.error_loading_profile, error);
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                Log.e(TAG, errorMsg);
                // Show empty state on error
                showEmptyProfileState();
                // Optionally clear error in ViewModel
                // videoViewModel.clearError();
            }
        });

        // Observe the fetched profile details
        videoViewModel.profileDetails.observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                Log.d(TAG, "Profile details loaded: " + profile.getEmail());
                updateProfileUI(profile);
            } else {
                Log.w(TAG,"Received null profile details from ViewModel.");
                // This might happen if fetch failed or user doesn't exist
                showEmptyProfileState();
            }
        });
    }

    private void updateProfileUI(Profile profile) {
        binding.textViewProfileEmail.setText(profile.getEmail() != null ? profile.getEmail() : getString(R.string.n_a));
        binding.textViewProfileName.setText(profile.getFullName() != null ? profile.getFullName() : getString(R.string.no_name_set));

        Glide.with(this)
                .load(profile.getAvatarUrl())
                .placeholder(R.drawable.ic_profile) // Placeholder image
                .error(R.drawable.ic_profile)      // Image to show on error
                .circleCrop()                       // Make the avatar circular
                .into(binding.imageViewProfileAvatar);
    }

    private void showEmptyProfileState() {
        // Show default/empty state when profile cannot be loaded
        binding.textViewProfileEmail.setText(R.string.n_a);
        binding.textViewProfileName.setText(R.string.error_loading_profile); // Indicate error
        binding.imageViewProfileAvatar.setImageResource(R.drawable.ic_profile); // Default icon
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up ViewBinding
        binding = null;
        Log.d(TAG, "onDestroyView: Binding set to null.");
    }
}
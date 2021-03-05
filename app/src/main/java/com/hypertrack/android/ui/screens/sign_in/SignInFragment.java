package com.hypertrack.android.ui.screens.sign_in;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidentityprovider.model.UserNotConfirmedException;
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException;
import com.hypertrack.android.cognito.CognitoClient;
import com.hypertrack.android.live.App;
import com.hypertrack.android.live.HTTextWatcher;
import com.hypertrack.android.ui.base.BaseFragment;
import com.hypertrack.android.ui.base.ProgressDialogFragment;
import com.hypertrack.android.ui.screens.sign_up.SignUpFragment;
import com.hypertrack.logistics.android.github.R;

public class SignInFragment extends ProgressDialogFragment implements CognitoClient.Callback {

    private static final String TAG = App.TAG + "SignInFragment";

    private EditText emailAddressEditText;
    private EditText passwordEditText;
    private View passwordClear;
    private TextView incorrect;

    public SignInFragment() {
        super(R.layout.fragment_signin);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emailAddressEditText = view.findViewById(R.id.email_address);
        passwordEditText = view.findViewById(R.id.password);
        passwordClear = view.findViewById(R.id.password_clear);
        incorrect = view.findViewById(R.id.incorrect);

        //todo task
//        emailAddressEditText.setText("ozh14873@cuoly.com");
//        passwordEditText.setText("qwerty123");

        passwordEditText.addTextChangedListener(new HTTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                passwordClear.setVisibility(TextUtils.isEmpty(editable) ? View.INVISIBLE : View.VISIBLE);
            }
        });
        passwordClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordEditText.setText("");
            }
        });

        view.findViewById(R.id.sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incorrect.setText("");

                String email = emailAddressEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    startSignIn(email, password);
                }
            }
        });

        view.findViewById(R.id.sign_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SignInFragment.this)
                        .navigate(SignInFragmentDirections.Companion.actionSignInFragmentToSignUpFragment());
            }
        });
    }

    public void startSignIn(String email, String password) {
        showProgress();
        CognitoClient.getInstance(getContext()).signIn(email, password, SignInFragment.this);
    }

    @Override
    public void onSuccess(CognitoClient mobileClient) {
        if (getActivity() != null) {
            dismissProgress();
            //todo task repo
            NavHostFragment.findNavController(SignInFragment.this)
                    .navigate(SignInFragmentDirections.Companion.actionSignInFragmentToVisitManagementFragment());
//            ((LaunchActivity)getActivity()).onLoginCompleted();
        }
    }

    @Override
    public void onError(String message, Exception e) {
        if (getActivity() != null) {
            dismissProgress();

            if (e instanceof UserNotConfirmedException) {
                NavHostFragment.findNavController(SignInFragment.this)
                        .navigate(SignInFragmentDirections.Companion.actionSignInFragmentToConfirmFragment());
            } else if (e instanceof UserNotFoundException) {
                incorrect.setText(R.string.user_does_not_exist);
            } else if (e instanceof NotAuthorizedException) {
                incorrect.setText(R.string.incorrect_username_or_pass);
            } else {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        }
    }
}

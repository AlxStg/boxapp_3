package com.example.boxapp3.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boxapp3.databinding.FragmentParentalBinding;
import com.example.boxapp3.listeners.KeyboardCustomListener;
import com.example.boxapp3.listeners.activities.OnlyTvActivityListener;
import com.example.boxapp3.listeners.fragments.ParentalFragmentListener;
import com.example.boxapp3.models.fragments.ParentalFragmentModel;
import com.example.iptvsdk.ui.parental.IptvParental;

public class ParentalFragment extends Fragment implements ParentalFragmentListener {

    private FragmentParentalBinding mBinding;
    private ParentalFragmentModel mModel;
    private IptvParental mIptvParental;
    private OnlyTvActivityListener mActivityListener;

    public ParentalFragment(OnlyTvActivityListener activityListener) {
        mActivityListener = activityListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentParentalBinding.inflate(inflater, container, false);
        mModel = new ParentalFragmentModel();
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIptvParental = new IptvParental(getContext());

        mModel.setPasswordSet(mIptvParental.hasPassword());
        mModel.setStep(0);
        mBinding.setModel(mModel);

        mBinding.keyboard.setListener(new KeyboardCustomListener() {
            @Override
            public void onKeyPressed(String key) {
                if (key.equals("back")) {
                    if (!mModel.getPassword().isEmpty())
                        mModel.setPassword(mModel.getPassword().substring(0, mModel.getPassword().length() - 1));
                } else if (key.equals("enter")) {
                    onPasswordSet();
                } else {
                    mModel.setPassword(mModel.getPassword() + key);
                }
            }
        });
        mBinding.keyboard.btnEnter.requestFocus();
    }

    @Override
    public void onPasswordSet() {
        if (!mModel.isPasswordSet()) {
            if (mModel.getStep() == 0) {
                if (mModel.getPassword().length() > 3) {
                    mModel.setStep(1);
                    mModel.setPassword("");
                } else {
                    mModel.setMessage("Senha muito curta");
                }
            } else if (mModel.getStep() == 1) {
                if (mModel.getPassword().equals(mModel.getConfirmPassword())) {
                    boolean setted = mIptvParental.setPassword(mModel.getPassword(), mModel.getConfirmPassword());
                    if (setted)
                        mActivityListener.onAllowAdultContent();
                    else {
                        mModel.setMessage("Erro ao criar senha");
                        mModel.setStep(0);
                    }
                } else {
                    mModel.setMessage("Senhas não conferem");
                    mModel.setStep(0);
                }
            }
        } else {
            if (mIptvParental.checkPassword(mModel.getPassword())) {
                mActivityListener.onAllowAdultContent();
            } else {
                mModel.setMessage("Senha incorreta");
            }
        }
    }

    @Override
    public void onCancel() {

    }
}

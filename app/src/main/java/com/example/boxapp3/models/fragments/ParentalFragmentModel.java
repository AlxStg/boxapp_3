package com.example.boxapp3.models.fragments;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class ParentalFragmentModel extends BaseObservable {
    private boolean isPasswordSet;
    private int step = 0;
    private String message = "";
    private String password = "";
    private String confirmPassword = "";

    @Bindable
    public boolean isPasswordSet() {
        return isPasswordSet;
    }

    public void setPasswordSet(boolean passwordSet) {
        isPasswordSet = passwordSet;
        notifyPropertyChanged(com.example.boxapp3.BR.passwordSet);
    }

    @Bindable
    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
        if(step == 0) {
            setMessage(isPasswordSet() ? "Digite sua senha" : "Crie uma senha");
        }
        else if(step == 1) {
            confirmPassword = getPassword();
            setMessage("Confirme sua senha");
        }
        notifyPropertyChanged(com.example.boxapp3.BR.step);
    }

    @Bindable
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        notifyPropertyChanged(com.example.boxapp3.BR.message);
    }

    @Bindable
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        notifyPropertyChanged(com.example.boxapp3.BR.password);
    }

    @Bindable
    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
        notifyPropertyChanged(com.example.boxapp3.BR.confirmPassword);
    }


}

package com.example.boxapp3.models.fragments;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.boxapp3.listeners.KeyboardCustomListener;

import io.reactivex.subjects.BehaviorSubject;

public class OnlyTvSearchFragmentModel extends BaseObservable {
    private String search = "";
    private BehaviorSubject<String> searchQuery = BehaviorSubject.create();
    private KeyboardCustomListener keyboardCustomListener;

    public OnlyTvSearchFragmentModel() {
        this.keyboardCustomListener = new KeyboardCustomListener() {
            @Override
            public void onKeyPressed(String key) {
                if (key.equals("back")) {
                    if (search.length() > 0) {
                        search = search.substring(0, search.length() - 1);
                        setSearch(search);
                    }
                } else {
                    search += key;
                    setSearch(search);
                }
            }
        };
    }

    public BehaviorSubject<String> getSearchQuery() {
        return searchQuery;
    }

    @Bindable
    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
        searchQuery.onNext(search);
        notifyPropertyChanged(com.example.boxapp3.BR.search);
    }

    public KeyboardCustomListener getKeyboardCustomListener() {
        return keyboardCustomListener;
    }
}

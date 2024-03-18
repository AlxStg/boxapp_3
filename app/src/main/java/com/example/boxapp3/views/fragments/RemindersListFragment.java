package com.example.boxapp3.views.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boxapp3.databinding.FragmentRemindersListBinding;
import com.example.boxapp3.databinding.ItemLembreteBinding;
import com.example.boxapp3.listeners.fragments.RemindersListFragmentListener;
import com.example.iptvsdk.common.generic_adapter.GenericAdapter;
import com.example.iptvsdk.data.models.Reminders;
import com.example.iptvsdk.ui.reminder.IptvReminder;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RemindersListFragment extends Fragment implements RemindersListFragmentListener {

    private FragmentRemindersListBinding  mBinding;
    private IptvReminder mReminder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentRemindersListBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mReminder = new IptvReminder(getContext());
        populateList();
    }

    private void populateList() {
        mReminder.getAllActiveReminders()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(th -> Log.e("RemindersListFragment", "Error getting reminders", th))
                .doOnSuccess(reminders -> {
                    GenericAdapter<Reminders, ItemLembreteBinding> adapter =
                            new GenericAdapter<>(getContext(), new GenericAdapter.GenericAdapterHelper<Reminders, ItemLembreteBinding>() {
                                @Override
                                public ItemLembreteBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                                    return ItemLembreteBinding.inflate(inflater, parent, false);
                                }

                                @Override
                                public Single<Reminders> getItem(int position) {
                                    return Single.just(reminders.get(position));
                                }

                                @Override
                                public Observable<Integer> getTotalItems() {
                                    return Observable.just(reminders.size());
                                }

                                @Override
                                public void setModelToItem(ItemLembreteBinding binding, Reminders item, int bindingAdapterPosition, GenericAdapter<Reminders, ItemLembreteBinding> adapter) {
                                    binding.setModel(item);
                                    binding.setListener(RemindersListFragment.this);
                                }
                            });
                    mBinding.lembretes.setAdapter(adapter);
                })
                .subscribe();
    }

    @Override
    public void onCanceledReminder(Reminders reminder) {
        String title = reminder.getTitle();

        if(reminder.isSportReminder){
            title = reminder.sportName + " X " + reminder.sportName2;
        }

        mReminder.inactiveReminderSync(title, reminder.getStart())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(th -> Log.e("RemindersListFragment", "Error canceling reminder", th))
                .doOnComplete(this::populateList)
                .subscribe();
    }
}

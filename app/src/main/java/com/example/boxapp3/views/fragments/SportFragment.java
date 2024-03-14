package com.example.boxapp3.views.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boxapp3.databinding.FragmentSportBinding;
import com.example.boxapp3.databinding.FutebolDataItemBinding;
import com.example.boxapp3.databinding.FutebolItemLinhaCampeonatosBinding;
import com.example.boxapp3.databinding.LinhaJogoBinding;
import com.example.boxapp3.listeners.fragments.KeyListener;
import com.example.boxapp3.listeners.fragments.MainFragmentListener;
import com.example.iptvsdk.common.generic_adapter.GenericAdapter;
import com.example.iptvsdk.ui.sports.IptvSport;
import com.example.iptvsdk.ui.sports.SportItemModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SportFragment extends Fragment implements MainFragmentListener, KeyListener {

    private IptvSport mIptvSport;
    private FragmentSportBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentSportBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mIptvSport = new IptvSport(getContext());

        populateDates(-1);
        populateChampionships();
    populateGames();
    }

    private void populateChampionships() {
        mIptvSport.getChampionships(false)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(th -> Log.e("SportFragment", "populateChampionships: ", th))
                .doOnSuccess(championships -> {
                    if (championships != null && championships.size() > 0) {
                        GenericAdapter<String, FutebolItemLinhaCampeonatosBinding> adapter =
                                new GenericAdapter<>(getContext(), new GenericAdapter.GenericAdapterHelper<String, FutebolItemLinhaCampeonatosBinding>() {
                                    @Override
                                    public FutebolItemLinhaCampeonatosBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                                        return FutebolItemLinhaCampeonatosBinding.inflate(inflater, parent, false);
                                    }

                                    @Override
                                    public Single<String> getItem(int position) {
                                        return Single.create(emitter -> {
                                           // foreach keyset
                                            int i = 0;
                                            for (String key : championships.values()) {
                                                if (i == position) {
                                                    emitter.onSuccess(key);
                                                    return;
                                                } else i++;
                                            }
                                        });
                                    }

                                    @Override
                                    public Observable<Integer> getTotalItems() {
                                        return Observable.just(championships.size());
                                    }

                                    @Override
                                    public void setModelToItem(FutebolItemLinhaCampeonatosBinding binding, String item, int bindingAdapterPosition, GenericAdapter<String, FutebolItemLinhaCampeonatosBinding> adapter) {
                                        binding.setName(item);
                                        binding.textView34.setOnClickListener(v -> {
                                            Single.<Integer>create(emitter -> {
                                                int i = 0;
                                                for (int key : championships.keySet()) {
                                                    if (i == bindingAdapterPosition) {
                                                        emitter.onSuccess(key);
                                                        return;
                                                    } else i++;
                                                }
                                            })
                                                    .subscribeOn(Schedulers.computation())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .doOnSuccess(id -> {
                                                        mIptvSport.setChampionshipId(id);
                                                        populateDates(id);
                                                    })
                                                    .subscribe();
                                        });
                                    }
                                });
                        mBinding.include.hgListChampionships.setAdapter(adapter);
                    }
                })
                .subscribe();
    }

    private void populateGames() {
        final boolean[] alreadySelected = {false};
        GenericAdapter<SportItemModel, LinhaJogoBinding> adapter =
                new GenericAdapter<>(getContext(), new GenericAdapter.GenericAdapterHelper<SportItemModel, LinhaJogoBinding>() {
                    @Override
                    public LinhaJogoBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                        return LinhaJogoBinding.inflate(inflater, parent, false);
                    }

                    @Override
                    public Single<SportItemModel> getItem(int position) {
                        return mIptvSport.getItem(position);
                    }

                    @Override
                    public Observable<Integer> getTotalItems() {
                        return mIptvSport.getTotalItems();
                    }

                    @Override
                    public void setModelToItem(LinhaJogoBinding binding, SportItemModel item, int bindingAdapterPosition, GenericAdapter<SportItemModel, LinhaJogoBinding> adapter) {
                        binding.setModel(item);
                        binding.textView50.setOnKeyListener((v, keyCode, event) -> {
                            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                                    mBinding.imageView22.requestFocus();
                                    return true;
                                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                                    mBinding.vgListDays.requestFocus();
                                    return true;
                                }
                            }
                            return false;
                        });

                        if(!alreadySelected[0]){
                            alreadySelected[0] = true;
                            selectPositonGamlistDate(new Date());
                        }
                    }
                });
        mBinding.vgListGames.setAdapter(adapter);
    }

    private void selectPositonGamlistDate(Date date) {
        mIptvSport.getPositionByDate(date)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(th -> Log.e("SportFragment", "setModelToItem: ", th))
                .doOnSuccess(position -> {
                    mBinding.vgListGames.setSelectedPosition(position);
                    mBinding.vgListGames.scrollToPosition(position);
                    mBinding.vgListGames.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mBinding.vgListGames.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            mBinding.vgListGames.setSelectedPosition(position);
                            mBinding.vgListGames.scrollToPosition(position);
                        }
                    });
                })
                .subscribe();
    }

    private void populateDates(int championshipId) {
        mIptvSport.getDates(championshipId)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(th -> Log.e("SportFragment", "populateDates: ", th))
                .doOnSuccess(dates -> {
                    if (dates != null && dates.size() > 0) {
                        GenericAdapter<String, FutebolDataItemBinding> adapter =
                                new GenericAdapter<>(getContext(), new GenericAdapter.GenericAdapterHelper<String, FutebolDataItemBinding>() {
                                    @Override
                                    public FutebolDataItemBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                                        return FutebolDataItemBinding.inflate(inflater, parent, false);
                                    }

                                    @Override
                                    public Single<String> getItem(int position) {
                                        String d = dates.get(position);
                                        // tranforma string em data
                                        SimpleDateFormat sdf = new SimpleDateFormat(
                                                "yyyy-MM-dd", Locale.getDefault());
                                        try {
                                            Date date = sdf.parse(d);
                                            sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                                            String formatted = sdf.format(date);
                                            formatted = formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
                                            return Single.just(formatted);
                                        } catch (Exception e) {
                                            Log.e("SportFragment", "getItem: ", e);
                                        }

                                        return Single.just(d);
                                    }

                                    @Override
                                    public Observable<Integer> getTotalItems() {
                                        return Observable.just(dates.size());
                                    }

                                    @Override
                                    public void setModelToItem(FutebolDataItemBinding binding, String item, int bindingAdapterPosition, GenericAdapter<String, FutebolDataItemBinding> adapter) {
                                        binding.setDate(item);
                                        binding.textView35.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Single.<Date>create(emitter -> {
                                                    SimpleDateFormat sdf = new SimpleDateFormat(
                                                            "yyyy-MM-dd", Locale.getDefault());
                                                    try {
                                                        Date date = sdf.parse(dates.get(bindingAdapterPosition));
                                                        emitter.onSuccess(date);
                                                    } catch (Exception e) {
                                                        Log.e("SportFragment", "setModelToItem: ", e);
                                                    }
                                                })
                                                        .subscribeOn(Schedulers.computation())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .doOnSuccess(date -> {
                                                            selectPositonGamlistDate(date);
                                                        })
                                                        .subscribe();
                                            }
                                        });
                                        binding.textView35.setOnKeyListener(new View.OnKeyListener() {
                                            @Override
                                            public boolean onKey(View v, int keyCode, KeyEvent event) {
                                                if(event.getAction() == KeyEvent.ACTION_DOWN) {
                                                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP && bindingAdapterPosition == 0) {
                                                        mBinding.include.hgListChampionships.requestFocus();
                                                        return true;
                                                    }
                                                }
                                                return false;
                                            }
                                        });
                                    }
                                });
                        mBinding.vgListDays.setAdapter(adapter);

                    }
                })
                .subscribe();
    }

    @Override
    public View firstFocus() {
        return mBinding.vgListDays;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (mBinding.vgListDays.hasFocus()) {
                    mBinding.vgListGames.requestFocus();
                    return true;
                }
                if(mBinding.vgListGames.hasFocus()){
                    mBinding.imageView22.requestFocus();
                    return true;
                }
            }
            if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP){
                if(mBinding.imageView22.hasFocus()){
                    mBinding.include.hgListChampionships.requestFocus();
                    return true;
                }
            }
        }
        return false;
    }
}

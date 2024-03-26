package com.example.boxapp3.views.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boxapp3.databinding.FragmentParentalBinding;
import com.example.boxapp3.databinding.ScrollTvCategoryItemBinding;
import com.example.boxapp3.listeners.KeyboardCustomListener;
import com.example.boxapp3.listeners.activities.OnlyTvActivityListener;
import com.example.boxapp3.listeners.fragments.ParentalFragmentListener;
import com.example.boxapp3.models.fragments.ParentalFragmentModel;
import com.example.iptvsdk.common.generic_adapter.GenericAdapter;
import com.example.iptvsdk.data.models.xtream.Category;
import com.example.iptvsdk.ui.live.IptvLive;
import com.example.iptvsdk.ui.parental.IptvParental;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ParentalFragment extends Fragment implements ParentalFragmentListener {

    private FragmentParentalBinding mBinding;
    private ParentalFragmentModel mModel;
    private IptvParental mIptvParental;
    private IptvLive mIptvLive;
    private OnlyTvActivityListener mActivityListener;
    private Handler categoriesHandler = new Handler();
    private Runnable categoriesRunnable;
    private SharedPreferences mSharedPreferences;
    private int mCategoryId;

    public ParentalFragment(OnlyTvActivityListener activityListener, int categoryId) {
        mActivityListener = activityListener;
        mCategoryId = categoryId;
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
        mSharedPreferences = getContext().getSharedPreferences("app", 0);
        mIptvParental = new IptvParental(getContext());
        mIptvLive = new IptvLive(getContext());

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
        setupCategories();
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
                        saveListPostion(mCategoryId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnError(th -> Log.e("TAG", "onPasswordSet: ", th))
                                .doOnComplete(() -> {
                                    mActivityListener.onAllowAdultContent();
                                })
                                .subscribe();
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
                saveListPostion(mCategoryId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError(th -> Log.e("TAG", "onPasswordSet: ", th))
                        .doOnComplete(() -> {
                            mActivityListener.onAllowAdultContent();
                        })
                        .subscribe();
            } else {
                mModel.setMessage("Senha incorreta");
            }
        }
    }

    @Override
    public void onCancel() {

    }

    private int mSelectedCategoryId = -1;

    private void setupCategories() {
        mIptvLive.getAllCategories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(th -> Log.e("TAG", "setupCategories: ", th))
                .doOnSuccess(categories -> {
                    GenericAdapter<Category, ScrollTvCategoryItemBinding> adapter = new GenericAdapter<>(getContext(), new GenericAdapter.GenericAdapterHelper<Category, ScrollTvCategoryItemBinding>() {
                        @Override
                        public ScrollTvCategoryItemBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                            return ScrollTvCategoryItemBinding.inflate(inflater, parent, false);
                        }

                        @Override
                        public Single<Category> getItem(int position) {
                            return Single.just(categories.get(position));
                        }

                        @Override
                        public Observable<Integer> getTotalItems() {
                            return Observable.just(categories.size());
                        }

                        @Override
                        public void setModelToItem(ScrollTvCategoryItemBinding binding, Category item, int bindingAdapterPosition, GenericAdapter<Category, ScrollTvCategoryItemBinding> adapter) {
                            binding.setModel(item);
                            binding.getRoot().setOnFocusChangeListener((v, hasFocus) -> {
                                if (categoriesRunnable != null)
                                    categoriesHandler.removeCallbacks(categoriesRunnable);
                                if (hasFocus) {
                                    categoriesRunnable = () -> {
                                        if(item.isAdult())
                                            return;
                                        mBinding.include2.listCategories.setSelectedPosition(bindingAdapterPosition);
                                        saveListPostion("listCategories", bindingAdapterPosition, item.getCategoryName());
                                        adapter.handleSelection(bindingAdapterPosition);
                                        mActivityListener.onShowChannelPanels();
                                    };
                                    categoriesHandler.postDelayed(categoriesRunnable, 1000);
                                }
                            });
                        }
                    });
                    mBinding.include2.listCategories.setAdapter(adapter);
                    if (getListPosition("listCategories") != -1)
                        mBinding.include2.listCategories.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                mBinding.include2.listCategories.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                mIptvLive.setCategoryByName(getLastItem("listCategories"));
                                mBinding.include2.listCategories.scrollToPosition(getListPosition("listCategories"));
                                mBinding.include2.listCategories.setSelectedPosition(getListPosition("listCategories"));
                                ((GenericAdapter<Category, ScrollTvCategoryItemBinding>) mBinding.include2.listCategories.getAdapter()).handleSelection(getListPosition("listCategories"));

                            }
                        });
                })
                .subscribe();
    }

    private Completable saveListPostion(int categoryId) {
        return Completable.create(emitter -> {
            mIptvLive.getAllCategories()
                    .subscribeOn(Schedulers.io())
                    .doOnError(th -> Log.e("TAG", "setupCategories: ", th))
                    .doOnSuccess(categories -> {
                        for (int i = 0; i < categories.size(); i++) {
                            if (categories.get(i).getCategoryId().equals(String.valueOf(categoryId))) {
                                saveListPostion("listCategories", i, categories.get(i).getCategoryName());
                                emitter.onComplete();
                                break;
                            }
                        }
                    })
                    .subscribe();
        });
    }

    private void saveListPostion(String list, int position, String itemName) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(list, position);
        editor.putString(list + "lastItem", itemName);
        editor.apply();
    }

    private int getListPosition(String list) {
        return mSharedPreferences.getInt(list, -1);
    }

    private String getLastItem(String list) {
        return mSharedPreferences.getString(list + "lastItem", "");
    }

}

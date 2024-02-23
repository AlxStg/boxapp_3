package com.example.boxapp3.views.fragments;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boxapp3.databinding.FragmentSearchResultsBinding;
import com.example.boxapp3.databinding.ItemListStreamBinding;
import com.example.boxapp3.listeners.activities.MainActivityListener;
import com.example.boxapp3.listeners.fragments.SearchFragmentListener;
import com.example.iptvsdk.common.generic_adapter.GenericAdapter;
import com.example.iptvsdk.ui.list_streams_categories.model.ListStreamsModel;
import com.example.iptvsdk.ui.search.IptvSearch;
import com.example.iptvsdk.ui.search.IptvSearchModel;

import io.reactivex.Observable;
import io.reactivex.Single;

public class SearchFragment extends Fragment implements SearchFragmentListener {

    FragmentSearchResultsBinding mBinding;
    IptvSearch mIptvSearch;
    IptvSearchModel mModel;
    MainActivityListener mainActivityListener;
    String search;

    public SearchFragment(String search, MainActivityListener listener) {
        this.search = search;
        this.mainActivityListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentSearchResultsBinding.inflate(inflater, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mIptvSearch = new IptvSearch(getContext());
        mIptvSearch.setSearch(search);
        mBinding.setModel(mIptvSearch.getModel());

        GenericAdapter<ListStreamsModel, ItemListStreamBinding> adapter = new GenericAdapter<>(getContext(),
                new GenericAdapter.GenericAdapterHelper<ListStreamsModel, ItemListStreamBinding>() {
                    @Override
                    public ItemListStreamBinding createBinding(LayoutInflater inflater, ViewGroup parent) {
                        return ItemListStreamBinding.inflate(inflater, parent, false);
                    }

                    @Override
                    public Single<ListStreamsModel> getItem(int position) {
                        return mIptvSearch.getItem(position);
                    }

                    @Override
                    public Observable<Integer> getTotalItems() {
                        return mIptvSearch.totalItems;
                    }

                    @Override
                    public void setModelToItem(ItemListStreamBinding binding, ListStreamsModel item, int bindingAdapterPosition, GenericAdapter<ListStreamsModel, ItemListStreamBinding> adapter) {
                        binding.setModel(item);
                        binding.getRoot().setOnClickListener(v -> mainActivityListener.openDetails(item.getId(), item.getType()));

                        binding.getRoot().setOnKeyListener(new View.OnKeyListener() {
                            @Override
                            public boolean onKey(View v, int keyCode, KeyEvent event) {
                                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP &&
                                            bindingAdapterPosition < 8) {
                                        mainActivityListener.onGoToSearch();
                                        return true;
                                    }
                                    return false;
                                }
                                return false;
                            }
                        });
                    }
                });
        mBinding.rvResultadosPesquisa.setNumColumns(8);
        mBinding.rvResultadosPesquisa.setAdapter(adapter);
    }

    @Override
    public void onFocused() {
        mBinding.rvResultadosPesquisa.requestFocus();
    }
}

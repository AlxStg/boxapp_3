package com.example.boxapp3.common;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.example.boxapp3.R;
import com.example.boxapp3.databinding.ItemListCategoriesBinding;
import com.example.boxapp3.databinding.ItemListStreamBinding;
import com.example.iptvsdk.ui.list_streams_categories.ListStreamCategoriesListener;
import com.example.iptvsdk.ui.list_streams_categories.ListStreamsCategories;
import com.example.iptvsdk.ui.list_streams_categories.model.ListCategoriesModel;
import com.example.iptvsdk.ui.list_streams_categories.model.ListStreamsModel;
import com.example.iptvsdk.ui.movies.IptvMovies;

public class ListCategoriesAndStream {
    public static ListStreamsCategories<ItemListCategoriesBinding, ItemListStreamBinding>
    setupLists(Context context, IptvMovies iptvMovies, RecyclerView listCategories, String type) {
        return new ListStreamsCategories<>(context,
                R.layout.item_list_categories,
                R.layout.item_list_stream,
                listCategories,
                type,
                false,
                new ListStreamCategoriesListener<ListCategoriesModel, ItemListCategoriesBinding>() {
                    @Override
                    public void bind(ItemListCategoriesBinding binding,
                                     ListCategoriesModel item,
                                     int categoryPosition) {
                        binding.setModel(item);
                    }

                    @Override
                    public void attachAdapter(ItemListCategoriesBinding binding,
                                              RecyclerView.Adapter adapter,
                                              int categoryPosition,
                                              ListStreamsCategories listStreamsCategories) {
                        binding.rv.setAdapter(adapter);
                    }
                }, new ListStreamCategoriesListener<ListStreamsModel, ItemListStreamBinding>() {
            @Override
            public void bind(ItemListStreamBinding binding, ListStreamsModel item, int position, int categoryPosition) {
                binding.setModel(item);
            }

            @Override
            public void onFirstItemLoaded(ListStreamsModel item) {
                iptvMovies.loadDetails(item.getId(), false);
            }
        });

        //listStreamsCategories.setOnItemClickListener((item, position) -> {
        //    openDetails(item.getType(), item.getId());
        //});
        //listStreamsCategories.setOnKeyClickListener((keyCode, event, item, position, adapterPosition) -> {
        //    if (event.getAction() == KeyEvent.ACTION_DOWN) {
        //        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
        //            if (position == 0) {
        //                mainActivityListener.goToMenu();
        //                return true;
        //            }
        //        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
        //            if (adapterPosition == 0) {
        //                binding.slider.requestFocus();
        //                return true;
        //            }
        //        }
        //    }
        //    // if (listStreamsCategories.onKey(event, keyCode, adapterPosition))
        //    //     return true;
        //    return false;
        //});

        //listStreamsCategories.setOnItemFocusListener((item, position) -> {
        //    rowFocused = position;
        //    //if (position == 0) {
        //    //    binding.slider.requestFocus();
        //    //}
        //});
    }
}

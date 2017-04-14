package proto.group8.cs465.groceryhelper;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import proto.group8.cs465.groceryhelper.model.Item;

/**
 * Created by edward on 4/11/17.
 */

public class MyApplication extends Application {
    public static final boolean DEBUG = true;

    private List<Item> mItemsList = new ArrayList<>();
    private List<Item> mFavoriteItemsList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        if (DEBUG) {
            // add some example items
            String[] itemNames = new String[]{
                    "apples", "bananas", "frozen pizzas", "pasta", "bread", "frozen veggies",
                    "eggs", "milk", "cheese", "yogurts", "cookies", "pineapple"
            };
            char[] itemSectionIds = new char[]{
                    'p', 'p', 'f', 'g', 'g', 'f',
                    'd', 'd', 'd', 'd', 'g', 'p'
            };
            for (int i = 0; i < itemNames.length && i < itemSectionIds.length; i++) {
                String name = itemNames[i];
                char sectionId = itemSectionIds[i];
                boolean isFavorited = Math.random() < 0.33;
                boolean isChecked = Math.random() < 0.33;
                Item newItem = new Item(name, isFavorited, isChecked);
                newItem.setSectionId(sectionId);
                mItemsList.add(newItem);
                if (newItem.isFavorited()) {
                    mFavoriteItemsList.add(newItem);
                }
            }
        }
    }

    public List<Item> getItemsList() {
        return mItemsList;
    }

    public List<Item> getFavoriteItemsList() {
        return mFavoriteItemsList;
    }

    public List<Item> getItemsForSection(char sectionId) {
        List<Item> res = new ArrayList<>();
        for (Item item : mItemsList) {
            if (item.getSectionId() == sectionId) {
                res.add(item);
            }
        }
        return res;
    }
}

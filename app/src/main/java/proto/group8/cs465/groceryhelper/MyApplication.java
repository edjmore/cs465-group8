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

    private List<Item> mItemsList = new ArrayList<>();
    private List<Item> mFavoriteItemsList = new ArrayList<>();

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

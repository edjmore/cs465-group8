package proto.group8.cs465.groceryhelper;

import android.app.Application;

import java.util.ArrayList;
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
}

package proto.group8.cs465.groceryhelper.model;

/**
 * Created by edward on 4/11/17.
 */

public class Item {

    private String mName;

    public Item(String name) {
        mName = name;
    }

    public static Item blank() {
        return new Item(null);
    }

    public boolean isBlank() {
        return mName == null || mName.isEmpty();
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }
}

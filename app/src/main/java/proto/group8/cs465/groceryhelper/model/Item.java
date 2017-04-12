package proto.group8.cs465.groceryhelper.model;

/**
 * Created by edward on 4/11/17.
 */

public class Item {

    private String mName;
    private boolean mIsFavorited;
    private boolean mIsInCart;

    public Item(String name, boolean isFavorited, boolean isInCart) {
        mName = name;
        mIsFavorited = isFavorited;
        mIsInCart = isInCart;
    }

    public static Item blank(boolean isFavorited) {
        return new Item(null, isFavorited, false);
    }

    public boolean isBlank() {
        return mName == null || mName.isEmpty();
    }

    public String getName() {
        return mName;
    }

    public boolean isFavorited() {
        return mIsFavorited;
    }

    public boolean isInCart() {
        return mIsInCart;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setIsFavorited(boolean isFavorited) {
        mIsFavorited = isFavorited;
    }

    public void setIsInCart(boolean isInCart) {
        mIsInCart = isInCart;
    }
}

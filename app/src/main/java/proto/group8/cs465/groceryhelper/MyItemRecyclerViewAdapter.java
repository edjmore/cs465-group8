package proto.group8.cs465.groceryhelper;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import proto.group8.cs465.groceryhelper.ItemFragment.OnListFragmentInteractionListener;
import proto.group8.cs465.groceryhelper.model.Item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Item} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private List<Item> mValues;
    private int mContentType;
    private final OnListFragmentInteractionListener mListener;
    private boolean mFocusOnNew = false;

    private boolean mIsInEditMode = false;
    private Set<Item> mSelectedItems = new HashSet<>();

    public MyItemRecyclerViewAdapter(List<Item> items, int contentType, OnListFragmentInteractionListener listener) {
        mValues = items;
        mContentType = contentType;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    public void focusOnNew() {
        mFocusOnNew = true;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        // todo: maintain a list of selected items on the favorites screen
        // box is checked if it is in the cart, or is selected during batch edit on the favorites screen
        boolean isChecked = mContentType != ItemFragment.TYPE_FAVORITES_LIST && holder.mItem.isInCart();
        holder.mCheckboxView.setChecked(isChecked);
        holder.mCheckboxView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((CheckBox) v).isChecked();

                holder.mItem.setIsInCart(isChecked);

                // strikethrough text if checked
                if (isChecked) {
                    holder.mContentView.setPaintFlags(
                            holder.mContentView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    holder.mContentView.setPaintFlags(
                            holder.mContentView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                }

                mListener.onListItemChecked(holder.mItem);
            }
        });

        // show edit text if this is the last item, otherwise show immutable text view
        boolean isEditable = position == getItemCount() - 1 && mContentType == ItemFragment.TYPE_LIST && holder.mItem.isBlank();
        holder.mContentView.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        holder.mEditContentView.setVisibility(isEditable ? View.VISIBLE : View.GONE);

        // show item name in text view
        if (isEditable) {
            holder.mEditContentView.setText(holder.mItem.getName());
            holder.mCheckboxView.setVisibility(View.INVISIBLE);
            holder.mFavIconView.setVisibility(View.INVISIBLE);

            // todo: keyboard doesn't pop up as expected
            // focus on edit text (keyboard input)
            if (mFocusOnNew) {
                holder.mEditContentView.requestFocus();
                InputMethodManager imm =
                        (InputMethodManager) holder.mEditContentView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(holder.mEditContentView, InputMethodManager.SHOW_IMPLICIT);
                mFocusOnNew = false;
            }
        }
        else {
            holder.mContentView.setText(mValues.get(position).getName());
            holder.mCheckboxView.setVisibility(View.VISIBLE);
            holder.mFavIconView.setVisibility(View.VISIBLE);

            // strikethrough text if checked
            if (isChecked) {
                holder.mContentView.setPaintFlags(
                        holder.mContentView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.mContentView.setPaintFlags(
                        holder.mContentView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }

        if (mContentType == ItemFragment.TYPE_FAVORITES_LIST) {
            holder.mCheckboxView.setVisibility(View.GONE);
        } else if (mContentType == ItemFragment.TYPE_GMAP_LIST) {
            holder.mFavIconView.setVisibility(View.GONE);
        }

        // setup favorites indicator
        int resId = holder.mItem.isFavorited() ?
                R.drawable.ic_star_black_24dp : R.drawable.ic_star_border_black_24dp;
        holder.mFavIconView.setImageResource(resId);
        holder.mFavIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // clicking on the favorites icon toggles state
                holder.mItem.setIsFavorited(!holder.mItem.isFavorited());

                // update our lists
                mListener.onListItemFavoriteToggled(holder.mItem);

                // if this is the favorites list we need to redraw
                if (mContentType == ItemFragment.TYPE_FAVORITES_LIST) {
                    notifyDataSetChanged();
                } else {
                    // otherwise we can just modify the view
                    int resId = holder.mItem.isFavorited() ?
                            R.drawable.ic_star_black_24dp : R.drawable.ic_star_border_black_24dp;
                    holder.mFavIconView.setImageResource(resId);
                }
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.e("Adapter", "Long click on item: " + holder.mItem.getName());
                mListener.onListItemLongClicked(holder.mItem, getSelf());
                return true;
            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Adapter", "Click on item: " + holder.mItem.getName());
                mListener.onListItemClicked(holder.mItem, getSelf());
            }
        });

        // view will use dark background to indicate selection
        boolean useDarkBackground = mSelectedItems.contains(holder.mItem);
        int colorResId = useDarkBackground ? R.color.colorSelectedListItemBg : R.color.colorListItemBg;
        holder.mView.setBackgroundResource(colorResId);
    }

    private MyItemRecyclerViewAdapter getSelf() {
        return this;
    }

    public boolean isInEditMode() {
        return mIsInEditMode;
    }

    public void engageEditMode() {
        if (mIsInEditMode) return;

        // engage edit mode
        mIsInEditMode = true;
    }

    public void disengageEditMode() {
        if (!mIsInEditMode) return;

        // disengage edit mode
        mIsInEditMode = false;

        // clear the selected set and update UI if necessary
        if (!mSelectedItems.isEmpty()) {
            int[] idxs = new int[mSelectedItems.size()];
            int i = 0;

            for (Item item : mSelectedItems) {
                idxs[i++] = mValues.indexOf(item);
            }

            mSelectedItems.clear();
            for (int idx : idxs) {
                notifyItemChanged(idx);
            }
        }
    }

    /* returns true iff there are no more selected items after this toggle */
    public boolean toggleSelection(Item item) {
        boolean isSelected = mSelectedItems.contains(item);
        if (isSelected) {
            // deselect
            mSelectedItems.remove(item);
        } else {
            // select
            mSelectedItems.add(item);
        }

        // update UI
        int idx = mValues.indexOf(item);
        notifyItemChanged(idx);

        return mSelectedItems.isEmpty();
    }

    public Set<Item> getSelectedItems() {
        return mSelectedItems;
    }

    public void setValues(List<Item> values) {
        mValues = values;
        notifyDataSetChanged();
    }

    public List<Item> getValues() {
        return mValues;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final CheckBox mCheckboxView;
        public final TextView mContentView;
        public final EditText mEditContentView;
        public final ImageView mFavIconView;
        public Item mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mCheckboxView = (CheckBox) view.findViewById(R.id.checkbox);
            mContentView = (TextView) view.findViewById(R.id.content);
            mEditContentView = (EditText) view.findViewById(R.id.edit_content);
            mFavIconView = (ImageView) view.findViewById(R.id.fav_icon);

            mEditContentView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    // update the item name in our model when user changes it
                    mItem.setName(s.toString());
                }
            });

            // capture the 'enter' event to finalize this item
            mEditContentView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_NULL || event != null) { // 'enter' key was pressed

                        // final name adjustment
                        mItem.setName(mEditContentView.getText().toString());
                        if (mItem.isBlank()) return true; // skip

                        // add a new blank item and refresh the list
                        mListener.onListItemFinalized();

                        return true;
                    }
                    return false;
                }
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}

package proto.group8.cs465.groceryhelper;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import proto.group8.cs465.groceryhelper.model.Item;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ItemFragment extends Fragment {

    public static final int TYPE_LIST = 0x00,
                            TYPE_FAVORITES_LIST = 0x01,
                            TYPE_GMAP_LIST = 0x10;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count",
                                ARG_CONTENT_TYPE = "content-type";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private int mContentType = TYPE_LIST;
    private List<Item> mItemsList;
    private OnListFragmentInteractionListener mListener;
    private MyItemRecyclerViewAdapter mAdapter;

    public MyItemRecyclerViewAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ItemFragment newInstance(int columnCount, int contentType) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt(ARG_CONTENT_TYPE, contentType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mContentType = getArguments().getInt(ARG_CONTENT_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            // load the desired item list
            switch (mContentType) {
                case TYPE_FAVORITES_LIST:
                    mItemsList = ((MyApplication) getActivity().getApplication()).getFavoriteItemsList();
                    break;
                case TYPE_LIST:
                default:
                    mItemsList = ((MyApplication) getActivity().getApplication()).getItemsList();
                    break;
            }

            // show dividers between list items
            //DividerItemDecoration div = new DividerItemDecoration(recyclerView.getContext(),
            //        DividerItemDecoration.VERTICAL);
            //recyclerView.addItemDecoration(div);

            recyclerView.setAdapter(mAdapter = new MyItemRecyclerViewAdapter(mItemsList, mContentType, mListener));
            if (mContentType == TYPE_LIST) addBlankItem();
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public boolean hasBlankItem() {
        if (mItemsList.isEmpty()) return false;
        Item lastItem = mItemsList.get(mItemsList.size() - 1);
        return lastItem.isBlank();
    }

    public void addBlankItem() {
        if (hasBlankItem()) return;
        mItemsList.add(Item.blank(mContentType == TYPE_FAVORITES_LIST));
        //if (mItemsList.size() > 1) mAdapter.notifyItemChanged(mItemsList.size() - 1); // previously finalized item
        mAdapter.notifyDataSetChanged(); // new item
        mAdapter.focusOnNew();
    }

    public void refresh() {
        mAdapter.notifyDataSetChanged();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Item item);
        void onListItemFinalized();
        void onListItemFavoriteToggled(Item item);
        void onListItemChecked(Item item);
        void onListItemLongClicked(Item item, MyItemRecyclerViewAdapter adapter);
        void onListItemClicked(Item item, MyItemRecyclerViewAdapter adapter);
    }
}

package proto.group8.cs465.groceryhelper.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import proto.group8.cs465.groceryhelper.R;

/**
 * Created by edward on 4/12/17.
 */

public class GMapView extends View {

    /* Data for the GMapView is loaded from res files at creation. We have a char grid representing the layout
     * of the store, with different chars representing blocks for different food items (e.g. dairy). We have a
     * list of labels mapping the chars to their full names (e.g. d=dairy), and a mapping from char label to
     * color (e.g. d=#ffff00ff). See the county_market* res/raw files for examples. */
    private char[][] mMapGrid;
    private String[] mMapLabels;
    private int[] mMapColors;

    private boolean mIsMapGridLoaded;
    private boolean mIsLabelsAndColorsLoaded;

    // painting objects (alloc'd once and reused)
    private final Paint mPaint;
    private final RectF mCell;

    private OnGMapSectionClickedListener mListener;

    public GMapView(Context context) {
        this(context, null);
    }

    public GMapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        int mapGridResId, mapLabelsAndColorResId;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GMapView, 0, 0);
        try {
            mapGridResId = a.getResourceId(R.styleable.GMapView_mapGridSrc, -1);
            mapLabelsAndColorResId = a.getResourceId(R.styleable.GMapView_mapLabelAndColorSrc, -1);
        } finally {
            if (a != null) a.recycle();
        }

        // try to load info from the resource files
        mIsMapGridLoaded = false;
        mIsLabelsAndColorsLoaded = false;
        if (mapGridResId != -1) {
            mIsMapGridLoaded = loadMapGrid(mapGridResId);
        }
        if (mapLabelsAndColorResId != -1) {
            mIsLabelsAndColorsLoaded = loadMapLabelsAndColors(mapLabelsAndColorResId);
        }

        if (mIsMapGridLoaded) Log.d("GMapView", "Map grid loaded.");
        else Log.e("GMapView", "Failed to load map grid!");
        if (mIsLabelsAndColorsLoaded) Log.d("GMapView", "Map labels and colors loaded.");
        else Log.e("GMapView", "Failed to load map labels and colors!");

        // init all the drawing utils
        mPaint = new Paint();
        mCell = new RectF(0, 0, 0, 0);
    }

    /* allows implementers to react to clicks on different sections of the GMap */
    public interface OnGMapSectionClickedListener {

        void onGMapSectionClicked(char sectionCh, String sectionStr);
    }

    public void setOnGMapSectionClickedListener(OnGMapSectionClickedListener listener) {
        mListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean handled;

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mListener != null) {
                    // figure out which section was tapped and call the listener interface
                    int ptrId = e.getPointerId(0);
                    float x = 1f * e.getX(ptrId) / getWidth(),
                          y = 1f * e.getY(ptrId) / getHeight();
                    char sectionCh = touchCoordsToSection(x, y);
                    
                    if (sectionCh >= 'a' && sectionCh <= 'z') {
                        String sectionStr = mMapLabels[sectionCh - 'a'];

                        mListener.onGMapSectionClicked(sectionCh, sectionStr);
                        handled = true;
                        break;
                    }
                }
            
            default:
                handled = false;
                break;
        }

        return handled;
    }

    private char touchCoordsToSection(float x, float y) {
        int j = (int) (mMapGrid[0].length * x),
            i = (int) (mMapGrid.length * y);
        return mMapGrid[i][j];
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(0xffffffff); // clear canvas

        // if anything is invalid we skip drawing
        if (!mIsMapGridLoaded || !mIsLabelsAndColorsLoaded) {
            return;
        }

        // calculate the cell size for our grid (matching height)
        final float w = getWidth(), h = getHeight();
        final float cellSize = h / mMapGrid.length;
        final float r = cellSize * 0.0f;

        // this is a super inefficient way of doing this, but it's just a prototype and I'm lazy
        for (int i = 0; i < mMapGrid.length; i++) {
            for (int j = 0; j < mMapGrid[0].length; j++) {
                final int key = mMapGrid[i][j] - 'a';

                // non-alphabetic characters are empty space
                if (key < 0 || key > 25) continue;

                // load the correct color into our paint object
                final int color = mMapColors[key];
                mPaint.setColor(color);

                // draw a rectangle at the cell coords
                final float drawX = cellSize * j, drawY = cellSize * i;
                mCell.set(drawX, drawY, drawX + cellSize, drawY + cellSize);
                canvas.drawRoundRect(mCell, r, r, mPaint);
            }
        }
    }

    private boolean loadMapGrid(int resFileId) {
        char[][] newMapGrid = null;
        boolean ok = false;

        try {
            Resources res = getResources();
            InputStream in = res.openRawResource(resFileId);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            List<String[]> rows = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(" ");
                rows.add(tokens);
            }

            if (!rows.isEmpty()) {
                int w = rows.get(0).length, h = rows.size();
                newMapGrid = new char[h][w];

                ok = true;
                for (int i = 0; i < rows.size() && ok; i++) {
                    String[] row = rows.get(i);
                    // ensure consistent row length
                    if (row.length != w) {
                        ok = false;
                        break;
                    }

                    for (int j = 0; j < w; j++) {
                        // ensure one char per token
                        if (row[j].length() != 1) {
                            ok = false;
                            break;
                        }

                        // put char in map grid
                        newMapGrid[i][j] = row[j].charAt(0);
                    }
                }
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ok) mMapGrid = newMapGrid;
        return ok;
    }

    private boolean loadMapLabelsAndColors(int resFileId) {
        String[] mapLabels = new String[26];
        int[] mapColors = new int[26];
        boolean ok = false;

        try {
            Resources res = getResources();
            InputStream in = res.openRawResource(resFileId);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(":");

                // use []s as dictionary mapping char to label, color
                int key = tokens[0].charAt(0) - 'a';
                String label = tokens[1];
                int color = Color.parseColor(tokens[2]);
                mapLabels[key] = label;
                mapColors[key] = color;
            }

            ok = true;
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ok) {
            mMapLabels = mapLabels;
            mMapColors = mapColors;
        }
        return ok;
    }
}

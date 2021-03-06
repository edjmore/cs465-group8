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
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

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

    private List<Coord> mShoppingRoute;
    private float[] mShoppingRouteDrawCoords;

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
        else Log.d("GMapView", "Failed to load map grid!");
        if (mIsLabelsAndColorsLoaded) Log.d("GMapView", "Map labels and colors loaded.");
        else Log.d("GMapView", "Failed to load map labels and colors!");

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

                    String sectionStr = null;
                    if (sectionCh >= 'a' && sectionCh <= 'z') {
                        sectionStr = mMapLabels[sectionCh - 'a'];
                    }

                    mListener.onGMapSectionClicked(sectionCh, sectionStr);
                    handled = sectionStr != null;
                    break;
                }
            
            default:
                handled = false;
                break;
        }

        return handled;
    }

    private char touchCoordsToSection(float x, float y) {
        float cellSize = getCellSize();
        int i = (int) (y * getHeight() / cellSize),
            j = (int) (x * getWidth() / cellSize);

        // may go out of bounds
        if (i >= mMapGrid.length || j >= mMapGrid[0].length || i < 0 || j < 0) {
            return '?';
        }
        return mMapGrid[i][j];
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(0xFAFAFA); // clear canvas

        // if anything is invalid we skip drawing
        if (!mIsMapGridLoaded || !mIsLabelsAndColorsLoaded) {
            return;
        }

        // calculate the cell size for our grid (matching height)
        final float w = getWidth(), h = getHeight();
        final float cellSize = getCellSize();
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

        if (mShoppingRoute.size() < 4) return;

        // draw the shopping route
        mPaint.setStrokeWidth(4);
        int idx = 0;
        for (Coord coord : mShoppingRoute) {
            mShoppingRouteDrawCoords[idx++] = cellSize / 2 + coord.x * cellSize;
            mShoppingRouteDrawCoords[idx++] = cellSize / 2 + coord.y * cellSize;
        }
        mPaint.setColor(0xffff0000);
        for (int i = 0; i < mShoppingRouteDrawCoords.length - 2; i += 2) {
            canvas.drawLine(mShoppingRouteDrawCoords[i], mShoppingRouteDrawCoords[i+1],
                    mShoppingRouteDrawCoords[i+2], mShoppingRouteDrawCoords[i+3], mPaint);
        }
    }

    private float getCellSize() {
        return getHeight() / mMapGrid.length;
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

        if (ok) {
            mMapGrid = newMapGrid;
            loadShoppingRoute(newMapGrid.clone());
        }
        return ok;
    }

    private void loadShoppingRoute(char[][] mapGrid) {
        mShoppingRoute = new LinkedList<>();

        // find the start point
        int w = mapGrid[0].length, h = mapGrid.length;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                char ch = mapGrid[i][j];
                if (ch == '<') {
                    mShoppingRoute.add(new Coord(j, i));
                    mapGrid[i][j] = '-';
                }
            }
        }

        char ch = 0;
        int count = 0;
        do {
            ch = 0;
            Coord last = mShoppingRoute.get(mShoppingRoute.size() - 1);

            // loop thru neighbors
            for (int x = Math.max(0, last.x - 1); x <= Math.min(w - 1, last.x + 1) && ch == 0; x++) {
                for (int y = Math.max(0, last.y - 1); y <= Math.min(h - 1, last.y + 1); y++) {
                    if ((x - last.x) * (y - last.y) != 0 || (x == last.x && y == last.y)) continue;

                    ch = mapGrid[y][x];
                    if (ch == '+' || ch == '>') {

                        // this is the next coord
                        mShoppingRoute.add(new Coord(x, y));
                        mapGrid[y][x] = '-';
                        break;

                    } else {
                        ch = 0;
                    }
                }
            }
            //Log.e("GMapView", ch + " " + last.x + " " + last.y);
        } while (ch != '>' && ++ count < 100);

        mShoppingRouteDrawCoords = new float[(mShoppingRoute.size() - 0) * 2];
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

    static class Coord {
        final int x, y;

        Coord(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}

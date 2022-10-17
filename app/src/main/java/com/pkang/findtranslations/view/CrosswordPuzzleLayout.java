package com.pkang.findtranslations.view;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridLayout;
import android.widget.Toast;

import com.pkang.findtranslations.model.PathDirection;
import com.pkang.findtranslations.model.Point;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Custom layout that represents the crossword puzzle view. Handles view logic
 * - 2-D coordinate system starts at top left and ends at bottom right
 * -- x-coordinate represents columns
 * -- y-coordinate represents rows
 * - user highlight gestures are translated into into a valid selection of letters
 */
public class CrosswordPuzzleLayout extends GridLayout {

    public interface Listener {
        void onPathPressed(List<Point> letters);
        void onPathSelected(List<Point> letters);
    }

    private Listener _listener;
    private List<Point> _targetPath = new ArrayList<>();
    private LinkedHashMap<Point, CrosswordLetterView> _selectedPath = new LinkedHashMap<>();

    private PointF _initialCoordinates;

    private Point _initialPoint;
    private Point _finalPoint;
    private PathDirection _direction;

    private int _pointer;

    public CrosswordPuzzleLayout(Context context) {
        super(context);
    }

    public CrosswordPuzzleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CrosswordPuzzleLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets path of points for the target letters.
     *
     * @param points path of target letters
     */
    public void setTargetWordPath(List<Point> points) {
        _targetPath = points;
    }

    /**
     * Loads this layout with a 2-d array representing the grid of letters for the crossword.
     *
     * @param grid     2-D array of letters
     * @param listener listener that will listen for updates to the crossword
     */
    public void loadCrosswords(String[][] grid, Listener listener) {
        _listener = listener;
        setRowCount(grid.length);
        setColumnCount(grid[0].length);

        for (String[] strings : grid) {
            for (String string : strings) {
                addCrosswordLetter(string);
            }
        }
    }

    private void addCrosswordLetter(String string) {
        CrosswordLetterView letter = new CrosswordLetterView(getContext());
        letter.setText(string);
        addView(letter);
    }

    public void setScreenCompleted(boolean completed) {
        if (completed) {
            completeCrossword();
        } else {
            clearPath();
        }
    }

    /**
     * Completes the crossword by highlighting the target letters.
     */
    private void completeCrossword() {
        for (Point point : _targetPath) {
            CrosswordLetterView letter = getLetterAtPoint(point);
            if (letter != null) {
                letter.setPressed(false);
                letter.setSelected(true);
            }
        }
    }

    /**
     * Resets the crossword UI on events such as failed attempts or the user highlighting outside the crossword bounds.
     * - Resets any highlighted letters
     * - Clears the selected path
     */
    private void clearPath() {
        for (Point point : _targetPath) {
            CrosswordLetterView letter = getLetterAtPoint(point);
            if (letter != null) {
                letter.setPressed(false);
                letter.setSelected(false);
            }
        }
        _finalPoint = null;

        if (!_selectedPath.values().isEmpty()) {
            highlightPath(_selectedPath, false);
            _selectedPath.clear();
        }
    }

    //region handle touch events
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1) {
            return false;
        }
        //pressed down
        return event.getAction() == MotionEvent.ACTION_DOWN;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            //Ignore user gestures while previous highlight is still on the screen
            return false;
        }

        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                //initial press
                clearPath();
                final int pointerIndex = event.getActionIndex();
                _pointer = event.getPointerId(pointerIndex);

                final float initialX = event.getX(pointerIndex);
                final float initialY = event.getY(pointerIndex);
                onInitialPointSelected(initialX, initialY);
                return true;
            }
            case MotionEvent.ACTION_UP: {
                //final release
                handleRelease(event);
                return true;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = event.getActionIndex();
                final int pointerID = event.getPointerId(pointerIndex);
                if (pointerID == _pointer) {
                    //final release
                    handleRelease(event);
                    return true;
                }
                return false;
            }
            case MotionEvent.ACTION_MOVE: {
                if (_pointer == MotionEvent.INVALID_POINTER_ID) {
                    return false;
                }
                int pointerIndex = event.findPointerIndex(_pointer);
                if (pointerIndex == MotionEvent.INVALID_POINTER_ID) {
                    return false;
                }
                float x = event.getX(pointerIndex);
                float y = event.getY(pointerIndex);
                onPointSelected(x, y);
                _listener.onPathPressed(new ArrayList<>(_selectedPath.keySet()));
                return true;
            }
            case MotionEvent.ACTION_CANCEL: {
                _pointer = MotionEvent.INVALID_POINTER_ID;
                Toast.makeText(getContext(), "Motion cancelled", Toast.LENGTH_SHORT).show();
                clearPath();
                return true;
            }
            default: {
                return false;
            }
        }
    }

    private void handleRelease(MotionEvent event) {
        final int pointerIndex = event.getActionIndex();
        float lastX = event.getX(pointerIndex);
        float lastY = event.getY(pointerIndex);
        onPointSelected(lastX, lastY);

        _listener.onPathSelected(new ArrayList<>(_selectedPath.keySet()));
        _pointer = MotionEvent.INVALID_POINTER_ID;
    }
    //endregion

    //region helper functions to calculate path

    /**
     * Handles a user pressing down their finger to begin letter selection.
     *
     * @param x x-coordinate of current user press
     * @param y y-coordinate of current user press
     */
    private void onInitialPointSelected(float x, float y) {
        _initialPoint = findClosestValidInitialPoint(x, y);
        _finalPoint = _initialPoint;

        //set initial coordinates to center of first letter
        CrosswordLetterView initialLetter = getLetterAtPoint(_initialPoint);
        float centerX = initialLetter.getLeft() + initialLetter.getWidth() / 2f;
        float centerY = initialLetter.getTop() + initialLetter.getHeight() / 2f;
        _initialCoordinates = new PointF(centerX, centerY);
        _selectedPath.put(_initialPoint, initialLetter);

        initialLetter.setPressed(true);
    }

    /**
     * Handles a user pressing moving their finger to highlight letters to select.
     *
     * @param x x-coordinate of current user press
     * @param y y-coordinate of current user press
     */
    private void onPointSelected(float x, float y) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            //raw coordinates outside crossword, clear selection until user press returns to valid bounds
            clearPath();
            return;
        }

        Point point = findClosestValidPoint(_initialCoordinates.x, _initialCoordinates.y, x, y);
        if (point.equals(_finalPoint)) {
            // same letter as before
            return;
        }

        clearPath();

        int row = point.y;
        int col = point.x;
        if (row < 0 || row >= getRowCount() || col < 0 || col >= getColumnCount()) {
            //calculated point outside crossword, clear selection until user press returns to valid bounds
            return;
        }
        _finalPoint = point;
        calculateNewPath(_initialPoint, _finalPoint);
        highlightPath(_selectedPath, true);
    }

    /**
     * Based on the closest valid point to the user's last press, create a path of letters to this point from the origin.
     *
     * @param finalPoint the closest valid point (along a 45 degree angle from origin) of user's current press
     */
    private void calculateNewPath(Point initialPoint, Point finalPoint) {
        switch (_direction) {
            case East: {
                int row = initialPoint.y;
                for (int col = initialPoint.x; col <= finalPoint.x; col++) {
                    addToPath(row, col);
                }
                break;
            }
            case NorthEast: {
                int row = initialPoint.y;
                for (int col = initialPoint.x; col <= finalPoint.x; col++) {
                    addToPath(row, col);
                    row--;
                }
                break;
            }
            case North: {
                int col = initialPoint.x;
                for (int row = initialPoint.y; row >= finalPoint.y; row--) {
                    addToPath(row, col);
                }
                break;
            }
            case NorthWest: {
                int row = initialPoint.y;
                for (int col = initialPoint.x; col >= finalPoint.x; col--) {
                    addToPath(row, col);
                    row--;
                }
                break;
            }
            case West: {
                int row = initialPoint.y;
                for (int col = initialPoint.x; col >= finalPoint.x; col--) {
                    addToPath(row, col);
                }
                break;
            }
            case SouthWest: {
                int row = initialPoint.y;
                for (int col = initialPoint.x; col >= finalPoint.x; col--) {
                    addToPath(row, col);
                    row++;
                }
                break;
            }
            case South: {
                int col = initialPoint.x;
                for (int row = initialPoint.y; row <= finalPoint.y; row++) {
                    addToPath(row, col);
                }
                break;
            }
            case SouthEast: {
                int row = initialPoint.y;
                for (int col = initialPoint.x; col <= finalPoint.x; col++) {
                    addToPath(row, col);
                    row++;
                }
                break;
            }
        }
    }

    private void addToPath(int row, int col) {
        Point point = new Point(col, row);
        CrosswordLetterView letter = getLetterAtPoint(point);
        if (letter != null) {
            _selectedPath.put(point, letter);
        }
    }

    private void highlightPath(LinkedHashMap<Point, CrosswordLetterView> selected, boolean pressed) {
        for (CrosswordLetterView letterView : selected.values()) {
            if (letterView != null) {
                letterView.setPressed(pressed);
            }
        }
    }

    /**
     * Find closest point (letter) given user press.
     * @param x x-coordinate of initial press
     * @param y y-coordinate of initial press
     * @return the closest valid point
     */
    private Point findClosestValidInitialPoint(float x, float y) {
        int row = (int) (y / getLetterSize());
        int col = (int) (x / getLetterSize());
        return new Point(col, row);
    }

    /**
     * Find closest point that is equidistant from the currently selected point to the origin, along
     * the nearest 45 degree axis.
     *
     * @param initialX x-coordinate of initial press
     * @param initialY y-coordinate of initial press
     * @param x x-coordinate of current press
     * @param y y-coordinate of current press
     * @return the closest valid point based on the logic described above
     */
    private Point findClosestValidPoint(float initialX, float initialY, float x, float y) {
        float deltaX = x - initialX;
        float deltaY = initialY - y; //y axis increases top to bottom

        if (Math.abs(deltaX) < getLetterSize()) {
            deltaX = 0;
        }
        if (Math.abs(deltaY) < getLetterSize()) {
            deltaY = 0;
        }
        if (deltaX == 0 && deltaY == 0) {
            // still in same box
            return _initialPoint;
        }

        double theta = Math.atan2(deltaY, deltaX);
        double thetaDegrees = Math.toDegrees(theta);
        thetaDegrees = (thetaDegrees < 0) ? (360d + thetaDegrees) : thetaDegrees;
        double distance = getDistance(deltaX, deltaY);

        int nearestAngle = ((int) Math.round(thetaDegrees / 45) * 45) % 360;
        double newX = initialX + (Math.cos(Math.toRadians(nearestAngle)) * distance);
        double newY = initialY - (Math.sin(Math.toRadians(nearestAngle)) * distance);
        _direction = PathDirection.valueOf(nearestAngle);

        int row = (int) (newY / getLetterSize());
        int col = (int) (newX / getLetterSize());
        return new Point(col, row);
    }

    /**
     * Given a point, finds the crossword letter at that point.
     * @param point location point of letter (column,row)
     * @return CrosswordLetterView at the point
     */
    private CrosswordLetterView getLetterAtPoint(Point point) {
        int row = point.y;
        int col = point.x;
        int index = row * getRowCount() + col;
        return (CrosswordLetterView) this.getChildAt(index);
    }

    /**
     * Given the distance between two points along the horizontal (deltaX) and vertical (deltaY) axis, calculates the total distance.
     *
     * @param deltaX Horizontal distance between original point and current point
     * @param deltaY Vertical distance between original point and current point
     * @return Distance between the two
     */
    private double getDistance(Float deltaX, Float deltaY) {
        return Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
    }

    /**
     * Calculates the size of each letter in the crossword.
     *
     * @return float representing single letter size;
     */
    private float getLetterSize() {
        float width = getWidth();
        return width / getColumnCount();
    }
    //endregion
}
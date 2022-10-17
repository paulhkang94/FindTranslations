package com.pkang.findtranslations.model;

/**
 * Create custom Point class that represents an (x,y) coordinate on the crossword.
 * The default Point class is in the Android package, and cannot be used for unit tests
 * x = column
 * y = row
 */
public class Point {
    public int x;
    public int y;

    public Point(int col, int row) {
        x = col;
        y = row;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Point point = (Point) o;
        return this.x == point.x && this.y == point.y;
    }
}

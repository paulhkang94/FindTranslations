package com.pkang.findtranslations.model;

/**
 * Enum representing a valid direction for a user's highlighted path (nearest 45 degrees).
 */
public enum PathDirection {
    East(0),
    NorthEast(45),
    North(90),
    NorthWest(135),
    West(180),
    SouthWest(225),
    South(270),
    SouthEast(315),
    ;

    private int _degrees;

    PathDirection(int val) {
        _degrees = val;
    }

    public static PathDirection valueOf(int degrees) {
        for (PathDirection pathDirection : values()) {
            if (pathDirection.getDegrees() == degrees) {
                return pathDirection;
            }
        }
        return null;
    }

    public int getDegrees() {
        return _degrees;
    }
}

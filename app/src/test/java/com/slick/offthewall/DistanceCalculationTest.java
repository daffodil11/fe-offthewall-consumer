package com.slick.offthewall;

import org.junit.Test;

import static org.junit.Assert.*;

public class DistanceCalculationTest {
    @Test
    public void distance_returnsZeroIfLocationsMatch() {
        Wall wallInLeeds = new Wall(1, 53.7949778, -1.5449472);
        int distanceToSameLocation = wallInLeeds.getDistanceFrom(53.7949778, -1.5449472);
        assertEquals(distanceToSameLocation, 0);
    }

    @Test
    public void distance_sortsCorrectly() {
        Wall wallInLeeds = new Wall(1, 53.7949778, -1.5449472);
        int distanceToYork = wallInLeeds.getDistanceFrom(53.959608, -1.084120);
        int distanceToSheffield = wallInLeeds.getDistanceFrom(53.371441, -1.523469);
        int distanceToWakefield = wallInLeeds.getDistanceFrom(53.682158, -1.497149);
        int distanceToPudsey = wallInLeeds.getDistanceFrom(53.799889, -1.673273);
        assertTrue(distanceToYork < distanceToSheffield);
        assertTrue(distanceToWakefield < distanceToSheffield);
        assertTrue(distanceToPudsey < distanceToWakefield);
    }
}
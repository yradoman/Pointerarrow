package com.pointarrow.nav.data.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MathHelperTest {

    private lateinit var tracker: MathHelper.NonCompassMotionTracker

    @Before
    fun setUp() {
        tracker = MathHelper.NonCompassMotionTracker()
    }

    @Test
    fun testHeadingInversionDetection() {
        // Direct 180° flip
        assertTrue(MathHelper.isHeadingInversion(0f, 180f))
        // Boundary values [140°, 220°]
        assertTrue(MathHelper.isHeadingInversion(0f, 140f))
        assertTrue(MathHelper.isHeadingInversion(0f, 220f))
        assertTrue(MathHelper.isHeadingInversion(350f, 170f)) // 180° diff across 0°
        assertTrue(MathHelper.isHeadingInversion(10f, 170f))  // 160° diff

        // Non-inversions
        assertFalse(MathHelper.isHeadingInversion(0f, 139f))
        assertFalse(MathHelper.isHeadingInversion(0f, 221f))
        assertFalse(MathHelper.isHeadingInversion(0f, 90f))
        assertFalse(MathHelper.isHeadingInversion(180f, 185f))
    }

    @Test
    fun testLowSpeedBearingLock() {
        var lat = 50.450000
        var lon = 30.520000
        var time = 1000L

        // Initialize motion North (speed 1.5 m/s > 1.0 m/s threshold)
        for (i in 1..5) {
            lat += 0.000015 // ~1.6m per second North
            time += 1000L
            tracker.updateBearing(lat, lon, speedMps = 1.5f, accuracyMeters = 3f, currentTimeMs = time)
        }

        val bearingMoving = tracker.getLastReliableBearing()
        assertNotNull(bearingMoving)

        // Drop speed to 0.4 m/s (below 1.0 m/s lock threshold)
        for (i in 1..3) {
            lat += 0.000003
            time += 1000L
            val lockedBearing = tracker.updateBearing(
                lat, lon, speedMps = 0.4f, accuracyMeters = 3f, currentTimeMs = time
            )
            // Must stay locked at the previous reliable high-speed bearing
            assertEquals(bearingMoving, lockedBearing)
        }
    }

    @Test
    fun testHeadingInversionHysteresis_PreventsFlippingWithoutSustainedDisplacement() {
        var lat = 50.450000
        val lon = 30.520000
        var time = 1000L

        // Step 1: Walk North consistently
        for (i in 1..6) {
            lat += 0.000015 // ~1.6m North
            time += 1000L
            tracker.updateBearing(lat, lon, speedMps = 1.5f, accuracyMeters = 3f, currentTimeMs = time)
        }

        val initialNorthBearing = tracker.getLastReliableBearing()!!
        assertTrue("Initial bearing should be approximately North (around 0°)", initialNorthBearing < 10f || initialNorthBearing > 350f)

        // Step 2: Take small jitter steps South (< 4.0 meters displacement)
        for (i in 1..2) {
            lat -= 0.000009 // ~1.0m South per step (total ~2.0m < 4.0m)
            time += 1000L
            val bearing = tracker.updateBearing(lat, lon, speedMps = 1.2f, accuracyMeters = 3f, currentTimeMs = time)
            // Arrow MUST NOT flip to South yet!
            assertEquals("Bearing should remain locked North during jitter/step-back", initialNorthBearing, bearing)
        }

        // Step 3: Continue walking South for sustained displacement >= 4.0 meters
        for (i in 1..4) {
            lat -= 0.000015 // ~1.6m South per step
            time += 1000L
            tracker.updateBearing(lat, lon, speedMps = 1.4f, accuracyMeters = 3f, currentTimeMs = time)
        }

        val finalSouthBearing = tracker.getLastReliableBearing()!!
        assertTrue("Bearing should now confirm 180° flip to South (around 180°)", finalSouthBearing in 170f..190f)
    }
}

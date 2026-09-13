package com.pointarrow.nav.data.repository

import com.pointarrow.nav.data.datastore.TargetPreferences
import com.pointarrow.nav.data.model.TargetPoint
import kotlinx.coroutines.flow.Flow

class TargetRepository(private val preferences: TargetPreferences) {

    val targetPointFlow: Flow<TargetPoint?> = preferences.targetPointFlow

    suspend fun setTarget(target: TargetPoint) {
        preferences.saveTarget(target)
    }

    suspend fun clearTarget() {
        preferences.clearTarget()
    }
}

package com.jepretaja.app.data.repository

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.jepretaja.app.core.util.FirestorePaths
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveLocationRepository @Inject constructor(
    private val db: FirebaseFirestore,
) {
    suspend fun publish(bookingId: String, userId: String, role: String, point: LatLng) {
        db.collection(FirestorePaths.LIVE_LOCATIONS).document("${bookingId}_$userId").set(
            mapOf(
                "bookingId" to bookingId,
                "userId" to userId,
                "role" to role,
                "latitude" to point.latitude,
                "longitude" to point.longitude,
                "updatedAt" to Timestamp.now(),
            )
        ).await()
    }
}
package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class BorePileJobItem(
    val id: String = UUID.randomUUID().toString(),
    val jobName: String = "",
    val pointCount: Int = 0,
    val depthMeters: Double = 0.0,
    val pricePerMeter: Double = 0.0
) {
    val totalMeters: Double get() = pointCount * depthMeters
    val subtotal: Double get() = totalMeters * pricePerMeter
}

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectCode: String,
    val name: String,
    val clientName: String,
    val location: String,
    val startDate: String,
    val targetDate: String,
    val contractAmount: Double,
    val status: String, // "PLANNING", "ACTIVE", "COMPLETED", "CANCELLED"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    // Rincian Perhitungan Sistematis Bore Pile
    val jobName: String = "",
    val pointCount: Int = 0,
    val depthMeters: Double = 0.0,
    val pricePerMeter: Double = 0.0,
    val mobiUnits: Int = 0,
    val mobiPricePerUnit: Double = 0.0,
    val jobItemsJson: String = ""
) {
    fun getJobItemsList(): List<BorePileJobItem> {
        if (jobItemsJson.isNotBlank()) {
            try {
                val array = JSONArray(jobItemsJson)
                val list = mutableListOf<BorePileJobItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        BorePileJobItem(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            jobName = obj.optString("jobName", ""),
                            pointCount = obj.optInt("pointCount", 0),
                            depthMeters = obj.optDouble("depthMeters", 0.0),
                            pricePerMeter = obj.optDouble("pricePerMeter", 0.0)
                        )
                    )
                }
                if (list.isNotEmpty()) return list
            } catch (_: Exception) {
                // fallback
            }
        }
        if (pointCount > 0 || jobName.isNotBlank()) {
            return listOf(
                BorePileJobItem(
                    jobName = jobName.ifBlank { "Bore Pile" },
                    pointCount = pointCount,
                    depthMeters = depthMeters,
                    pricePerMeter = pricePerMeter
                )
            )
        }
        return emptyList()
    }

    companion object {
        fun serializeJobItems(items: List<BorePileJobItem>): String {
            val array = JSONArray()
            for (item in items) {
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("jobName", item.jobName)
                obj.put("pointCount", item.pointCount)
                obj.put("depthMeters", item.depthMeters)
                obj.put("pricePerMeter", item.pricePerMeter)
                array.put(obj)
            }
            return array.toString()
        }
    }
}


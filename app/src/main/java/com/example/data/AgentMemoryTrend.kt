package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agent_memory_trends")
data class AgentMemoryTrend(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val trendName: String,
  val discoveryDate: String,
  val opportunityReason: String,
  val opportunityScore: Int = 8, // legacy 1-10
  val isUsed: Boolean = false,
  val category: String = "Instagram Reels",
  val trendScore: Int = 85, // 0 to 100
  val isBestPick: Boolean = false,
  val growthScore: Int = 21,      // 0 to 25
  val freshnessScore: Int = 17,   // 0 to 20
  val interestScore: Int = 13,    // 0 to 15
  val competitionScore: Int = 13, // 0 to 15
  val easeScore: Int = 9,         // 0 to 10
  val relevanceScore: Int = 9,    // 0 to 10
  val seriesScore: Int = 4        // 0 to 5
) {
  val classificationText: String
    get() = when {
      trendScore >= 90 -> "🔥 فرصة استثنائية"
      trendScore >= 75 -> "🟢 فرصة قوية"
      trendScore >= 60 -> "🟡 فرصة متوسطة"
      else -> "🔴 تجاهل"
    }
}

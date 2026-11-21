package com.example.data

import kotlinx.coroutines.flow.Flow

class AgentMemoryRepository(private val dao: AgentMemoryDao) {
  val allTrends: Flow<List<AgentMemoryTrend>> = dao.getAllMemoryTrends()

  suspend fun getAllTrendsList(): List<AgentMemoryTrend> = dao.getAllMemoryTrendsList()

  suspend fun addTrend(trend: AgentMemoryTrend): Long = dao.insert(trend)

  suspend fun addTrends(trends: List<AgentMemoryTrend>) = dao.insertAll(trends)

  suspend fun setUsedStatus(id: Int, isUsed: Boolean) = dao.setUsedStatus(id, isUsed)

  suspend fun deleteTrend(id: Int) = dao.deleteById(id)

  suspend fun clearMemory() = dao.clearAll()

  suspend fun prepopulateInitialMemoryIfNeeded() {
    val current = dao.getAllMemoryTrendsList()
    if (current.isEmpty()) {
      val initial = listOf(
        AgentMemoryTrend(
          trendName = "The 3-Second Weather Metamorphosis (التحول المناخي فائق السرعة)",
          discoveryDate = "2026-10-08",
          opportunityReason = "معدل إكمال يتجاوز 85% على Reels، واستغلال تقنية Speed-Ramping مع صوت هبوط سينمائي في أول 3 ثوانٍ.",
          opportunityScore = 10,
          isUsed = false,
          category = "Reels / Video Editing",
          trendScore = 94,
          isBestPick = true,
          growthScore = 24,
          freshnessScore = 19,
          interestScore = 15,
          competitionScore = 13,
          easeScore = 8,
          relevanceScore = 10,
          seriesScore = 5
        ),
        AgentMemoryTrend(
          trendName = "Human Scale in Epic Nature (المقياس البشري في اتساع الطبيعة)",
          discoveryDate = "2026-10-08",
          opportunityReason = "أثبتت تقارير تحليل الأداء نمو التفاعل بنسبة 60% عند وضع شخص صغير لإظهار ضخامة الجبل أو الغابة وتوليد ارتباط عاطفي.",
          opportunityScore = 9,
          isUsed = true,
          category = "Landscape Photography",
          trendScore = 90,
          isBestPick = false,
          growthScore = 23,
          freshnessScore = 17,
          interestScore = 14,
          competitionScore = 12,
          easeScore = 10,
          relevanceScore = 10,
          seriesScore = 4
        ),
        AgentMemoryTrend(
          trendName = "Anti-Saturation & Film Emulation (الألوان الترابية ومناهضة التشبع)",
          discoveryDate = "2026-10-08",
          opportunityReason = "ردة فعل قوية ضد صور وفلاتر الذكاء الاصطناعي المشبعة بإفراط، وارتفاع طلب الحفظ (Saves) لإعدادات درجات ألوان 35mm.",
          opportunityScore = 9,
          isUsed = false,
          category = "Editing / Presets",
          trendScore = 86,
          isBestPick = false,
          growthScore = 21,
          freshnessScore = 18,
          interestScore = 13,
          competitionScore = 12,
          easeScore = 9,
          relevanceScore = 9,
          seriesScore = 4
        ),
        AgentMemoryTrend(
          trendName = "The Micro-Forest POV (عوالم الغابات المصغرة)",
          discoveryDate = "2026-10-08",
          opportunityReason = "علاج إرهاق المشاهد الواسعة المكررة عبر تصوير ماكرو دقيق للأرضية وحركات الندى كأنها كوكب خيالي، يرفع معدل الإعادة.",
          opportunityScore = 9,
          isUsed = false,
          category = "Macro Photography",
          trendScore = 85,
          isBestPick = false,
          growthScore = 20,
          freshnessScore = 17,
          interestScore = 12,
          competitionScore = 14,
          easeScore = 9,
          relevanceScore = 9,
          seriesScore = 4
        )
      )
      dao.insertAll(initial)
    }
  }
}

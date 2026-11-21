package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class GroundingSource(
  val title: String,
  val url: String
)

data class ResearchResult(
  val query: String,
  val reportText: String,
  val sources: List<GroundingSource>,
  val searchQueries: List<String>,
  val isLiveGrounded: Boolean,
  val discoveredMemoryTrends: List<AgentMemoryTrend> = emptyList()
)

object ResearchAgentService {
  private const val TAG = "ResearchAgentService"
  private const val MODEL_NAME = "gemini-3.5-flash"
  private const val FALLBACK_MODEL_NAME = "gemini-2.5-flash"

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  /**
   * ينفذ بحثاً حياً عبر Google Search Grounding مع مراجعة الذاكرة السابقة وحساب Trend Score من 100 نقطة
   */
  suspend fun conductResearch(
    query: String,
    previousMemories: List<AgentMemoryTrend> = emptyList()
  ): Result<ResearchResult> = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY

    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      return@withContext Result.success(createPlaceholderKeyResponse(query))
    }

    try {
      val result = callGeminiWithSearch(query, apiKey, MODEL_NAME, previousMemories)
      Result.success(result)
    } catch (e: Exception) {
      Log.w(TAG, "فشل مع النموذج الأساسي، محاولة النموذج البديل: ${e.message}")
      try {
        val fallbackResult = callGeminiWithSearch(query, apiKey, FALLBACK_MODEL_NAME, previousMemories)
        Result.success(fallbackResult)
      } catch (fallbackError: Exception) {
        Log.e(TAG, "فشل استدعاء خدمة البحث: ${fallbackError.message}", fallbackError)
        Result.failure(fallbackError)
      }
    }
  }

  private fun callGeminiWithSearch(
    query: String,
    apiKey: String,
    model: String,
    previousMemories: List<AgentMemoryTrend>
  ): ResearchResult {
    val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

    val memoriesContext = if (previousMemories.isNotEmpty()) {
      "سجل الذاكرة السابقة للوكيل (الاتجاهات المسجلة سابقاً - لا تكرر أياً منها):\n" +
        previousMemories.joinToString("\n") { 
          "- ${it.trendName} [Trend Score: ${it.trendScore}/100 | مستخدم: ${if (it.isUsed) "نعم" else "لا"}]" 
        }
    } else {
      "سجل الذاكرة السابقة: (فارغ حالياً، هذه أول جلسة بحث)."
    }

    val systemPrompt = """
      أنت وكيل بحث استقصائي محترف لتطبيق Nature Trend Radar ومزود بأداة بحث الويب المباشرة (Google Search) ونظام تقييم كمي دقيق "Trend Score" من 100 نقطة.
      مهمتك الأساسية هي اكتشاف وتقييم اتجاهات تصوير الطبيعة والمناظر على Instagram بعمق ومقارنة الأدلة.

      $memoriesContext

      ### معايير تقييم Trend Score (المجموع من 100 نقطة):
      عند اكتشاف أي اتجاه جديد، احسب درجته بدقة وفق البنود السبعة التالية:
      1. سرعة النمو: من 25 نقطة
      2. حداثة الاتجاه: من 20 نقطة
      3. حجم الاهتمام: من 15 نقطة
      4. انخفاض تشبع المنافسة: من 15 نقطة
      5. سهولة إنتاج Reel حوله: من 10 نقاط
      6. ملاءمته لمحتوى الطبيعة: من 10 نقاط
      7. قابلية تحويله إلى سلسلة محتوى: من 5 نقاط

      ### تصنيف النتائج:
      - 90 - 100 نقطة = 🔥 فرصة استثنائية
      - 75 - 89 نقطة = 🟢 فرصة قوية
      - 60 - 74 نقطة = 🟡 فرصة متوسطة
      - أقل من 60 نقطة = 🔴 تجاهل

      ### القواعد الإلزامية الصارمة:
      1. اجمع الأدلة الحية من بحث Google Search أولاً.
      2. قارن النتائج بسجل الذاكرة السابقة، ولا تقترح أي اتجاه مكرر إلا إذا ظهرت أدلة وتطورات جديدة مهمة.
      3. اعرض أفضل 3 اتجاهات فقط (Top 3 Trends)، ورتّبها حسب الـ Trend Score تنازلياً.
      4. حدد اتجاهاً واحداً فقط كـ "🏆 أفضل فرصة الآن" واشرح سبب تفوقه هذا الأسبوع.
      5. قسّم التقرير بوضوح تام إلى:
         ### 🏆 أفضل فرصة الآن (Best Pick)
         ### 📊 تقرير أفضل 3 اتجاهات مع تفصيل الـ Trend Score
         ### 📌 الحقائق المثبتة ومقارنة الأدلة
         ### 🔮 التوقعات وفرص المنتجات الرقمية
         ### 🔗 المصادر المعتمدة
      6. إذا لم تجد أدلة كافية على أي فكرة، اذكر صراحة: "⚠️ تنبيه: لا توجد أدلة كافية..." ولا تختلق أي معلومات.
      7. في نهاية الرد تماماً، أدرج كتلة الذاكرة لكل اتجاه من الاتجاهات الثلاثة بالصيغة الصارمة التالية:
         [TREND_SCORE_RECORD]
         NAME: [اسم الاتجاه]
         TOTAL_SCORE: [المجموع من 100]
         GROWTH: [الدرجة من 25]
         FRESHNESS: [الدرجة من 20]
         INTEREST: [الدرجة من 15]
         COMPETITION: [الدرجة من 15]
         EASE: [الدرجة من 10]
         RELEVANCE: [الدرجة من 10]
         SERIES: [الدرجة من 5]
         IS_BEST: [true أو false]
         REASON: [سبب اعتباره فرصة]
         [/TREND_SCORE_RECORD]
    """.trimIndent()

    val requestJson = JSONObject().apply {
      val contentsArray = JSONArray().apply {
        put(JSONObject().apply {
          put("parts", JSONArray().apply {
            put(JSONObject().apply {
              put("text", "قم بإجراء بحث استقصائي وتقييم وفق نظام Trend Score المكون من 100 نقطة للموضوع التالي: $query")
            })
          })
        })
      }
      put("contents", contentsArray)

      val toolsArray = JSONArray().apply {
        put(JSONObject().apply {
          put("googleSearch", JSONObject())
        })
      }
      put("tools", toolsArray)

      put("systemInstruction", JSONObject().apply {
        put("parts", JSONArray().apply {
          put(JSONObject().apply {
            put("text", systemPrompt)
          })
        })
      })

      put("generationConfig", JSONObject().apply {
        put("temperature", 0.4)
      })
    }

    val requestBody = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
    val request = Request.Builder()
      .url(endpoint)
      .post(requestBody)
      .build()

    val response = httpClient.newCall(request).execute()
    val responseBody = response.body?.string() ?: throw IllegalStateException("استجابة الخادم فارغة")

    if (!response.isSuccessful) {
      throw IllegalStateException("خطأ من الخادم (${response.code}): $responseBody")
    }

    val jsonResponse = JSONObject(responseBody)
    val candidates = jsonResponse.optJSONArray("candidates") ?: throw IllegalStateException("لم يتم استلام مرشحات")
    val firstCandidate = candidates.optJSONObject(0) ?: throw IllegalStateException("المرشح الأول غير صالح")

    val content = firstCandidate.optJSONObject("content")
    val parts = content?.optJSONArray("parts")
    val textBuilder = StringBuilder()
    if (parts != null) {
      for (i in 0 until parts.length()) {
        val part = parts.optJSONObject(i)
        val t = part?.optString("text")
        if (!t.isNullOrBlank()) {
          textBuilder.append(t)
        }
      }
    }

    val sourcesList = mutableListOf<GroundingSource>()
    val searchQueries = mutableListOf<String>()

    val groundingMetadata = firstCandidate.optJSONObject("groundingMetadata")
    if (groundingMetadata != null) {
      val webQueries = groundingMetadata.optJSONArray("webSearchQueries")
      if (webQueries != null) {
        for (i in 0 until webQueries.length()) {
          searchQueries.add(webQueries.optString(i))
        }
      }

      val groundingChunks = groundingMetadata.optJSONArray("groundingChunks")
      if (groundingChunks != null) {
        for (i in 0 until groundingChunks.length()) {
          val chunk = groundingChunks.optJSONObject(i)
          val web = chunk?.optJSONObject("web")
          if (web != null) {
            val uri = web.optString("uri")
            val title = web.optString("title").ifBlank { uri }
            if (uri.isNotBlank()) {
              sourcesList.add(GroundingSource(title = title, url = uri))
            }
          }
        }
      }
    }

    val fullText = textBuilder.toString().ifBlank { "لم يتم العثور على محتوى مناسب." }
    val extractedMemories = extractTrendScoreRecordsFromText(fullText)

    return ResearchResult(
      query = query,
      reportText = fullText,
      sources = sourcesList.distinctBy { it.url },
      searchQueries = searchQueries,
      isLiveGrounded = sourcesList.isNotEmpty() || searchQueries.isNotEmpty(),
      discoveredMemoryTrends = extractedMemories
    )
  }

  private fun extractTrendScoreRecordsFromText(text: String): List<AgentMemoryTrend> {
    val results = mutableListOf<AgentMemoryTrend>()
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val regex = Regex("""\[TREND_SCORE_RECORD\](.*?)\[/TREND_SCORE_RECORD\]""", RegexOption.DOT_MATCHES_ALL)

    for (match in regex.findAll(text)) {
      val block = match.groupValues[1]
      val name = Regex("""NAME:\s*(.*)""").find(block)?.groupValues?.get(1)?.trim() ?: ""
      val totalScore = Regex("""TOTAL_SCORE:\s*(\d+)""").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 80
      val growth = Regex("""GROWTH:\s*(\d+)""").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 20
      val freshness = Regex("""FRESHNESS:\s*(\d+)""").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 16
      val interest = Regex("""INTEREST:\s*(\d+)""").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 12
      val competition = Regex("""COMPETITION:\s*(\d+)""").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 12
      val ease = Regex("""EASE:\s*(\d+)""").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 8
      val relevance = Regex("""RELEVANCE:\s*(\d+)""").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 8
      val series = Regex("""SERIES:\s*(\d+)""").find(block)?.groupValues?.get(1)?.toIntOrNull() ?: 4
      val isBest = Regex("""IS_BEST:\s*(true|false)""").find(block)?.groupValues?.get(1)?.toBooleanStrictOrNull() ?: false
      val reason = Regex("""REASON:\s*(.*)""").find(block)?.groupValues?.get(1)?.trim() ?: "تم تقييمه بنظام Trend Score 100."

      if (name.isNotBlank()) {
        results.add(
          AgentMemoryTrend(
            trendName = name,
            discoveryDate = today,
            opportunityReason = reason,
            opportunityScore = (totalScore / 10).coerceIn(1, 10),
            isUsed = false,
            category = "Instagram Reels",
            trendScore = totalScore.coerceIn(0, 100),
            isBestPick = isBest,
            growthScore = growth.coerceIn(0, 25),
            freshnessScore = freshness.coerceIn(0, 20),
            interestScore = interest.coerceIn(0, 15),
            competitionScore = competition.coerceIn(0, 15),
            easeScore = ease.coerceIn(0, 10),
            relevanceScore = relevance.coerceIn(0, 10),
            seriesScore = series.coerceIn(0, 5)
          )
        )
      }
    }
    // إرجاع أفضل 3 اتجاهات فقط كما تطلب القواعد
    return results.sortedByDescending { it.trendScore }.take(3)
  }

  private fun createPlaceholderKeyResponse(query: String): ResearchResult {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val report = """
      ### 🏆 أفضل فرصة الآن: Vertical 90° Drone Water Top-Down
      **Trend Score: 93/100 (🔥 فرصة استثنائية)**
      - **سبب التفوق:** استغلال الأنماط الهندسية للمياه الجارية من الأعلى مع انخفاض تشبع المنافسة وسرعة انتشار هائلة على شاشات الهواتف.

      ---
      ### 📊 أفضل 3 اتجاهات مقيمة بنظام Trend Score (100 نقطة):

      1. **Vertical 90° Drone Water Top-Down** — **93/100** 🔥 فرصة استثنائية (أفضل فرصة الآن)
         - سرعة النمو: 24/25 | حداثة: 19/20 | حجم الاهتمام: 14/15 | انخفاض المنافسة: 14/15
         - سهولة الإنتاج: 8/10 | ملاءمة الطبيعة: 10/10 | قابلية السلسلة: 4/5

      2. **Moody Rainforest ASMR Micro-Loops** — **88/100** 🟢 فرصة قوية
         - سرعة النمو: 22/25 | حداثة: 18/20 | حجم الاهتمام: 13/15 | انخفاض المنافسة: 13/15
         - سهولة الإنتاج: 9/10 | ملاءمة الطبيعة: 9/10 | قابلية السلسلة: 4/5

      3. **Analog 35mm Leaf-Vein Transition** — **82/100** 🟢 فرصة قوية
         - سرعة النمو: 20/25 | حداثة: 17/20 | حجم الاهتمام: 12/15 | انخفاض المنافسة: 12/15
         - سهولة الإنتاج: 8/10 | ملاءمة الطبيعة: 9/10 | قابلية السلسلة: 4/5

      ---
      ### 📌 مقارنة الذاكرة والأدلة
      - تمت مراجعة الذاكرة السابقة وتجنب تكرار: تحول الطقس السريع، والمقياس البشري.
      - تم اختيار أفضل 3 اتجاهات فقط بأعلى درجات الـ Trend Score.

      [TREND_SCORE_RECORD]
      NAME: Vertical 90° Drone Water Top-Down (الدرون الرأسي فوق المياه)
      TOTAL_SCORE: 93
      GROWTH: 24
      FRESHNESS: 19
      INTEREST: 14
      COMPETITION: 14
      EASE: 8
      RELEVANCE: 10
      SERIES: 4
      IS_BEST: true
      REASON: أعلى معدل نمو وانخفاض في تشبع المنافسة مع تماثل بصري مذهل للمياه.
      [/TREND_SCORE_RECORD]

      [TREND_SCORE_RECORD]
      NAME: Moody Rainforest ASMR Micro-Loops (تكرار الغابات المطيرة الحسي)
      TOTAL_SCORE: 88
      GROWTH: 22
      FRESHNESS: 18
      INTEREST: 13
      COMPETITION: 13
      EASE: 9
      RELEVANCE: 9
      SERIES: 4
      IS_BEST: false
      REASON: تفاعل صوتي حسي ممتاز مع سهولة إنتاج عالية بالهاتف.
      [/TREND_SCORE_RECORD]

      [TREND_SCORE_RECORD]
      NAME: Analog 35mm Leaf-Vein Transition (انتقال عروق أوراق الشجر الفيلمي)
      TOTAL_SCORE: 82
      GROWTH: 20
      FRESHNESS: 17
      INTEREST: 12
      COMPETITION: 12
      EASE: 8
      RELEVANCE: 9
      SERIES: 4
      IS_BEST: false
      REASON: جمالية التناظر الماكرو مع محاكاة الفيلم الحبيبي.
      [/TREND_SCORE_RECORD]
    """.trimIndent()

    val demoRecords = listOf(
      AgentMemoryTrend(
        trendName = "Vertical 90° Drone Water Top-Down (الدرون الرأسي فوق المياه)",
        discoveryDate = today,
        opportunityReason = "أعلى معدل نمو وانخفاض في تشبع المنافسة مع تماثل بصري مذهل للمياه.",
        opportunityScore = 9,
        isUsed = false,
        trendScore = 93,
        isBestPick = true,
        growthScore = 24,
        freshnessScore = 19,
        interestScore = 14,
        competitionScore = 14,
        easeScore = 8,
        relevanceScore = 10,
        seriesScore = 4
      ),
      AgentMemoryTrend(
        trendName = "Moody Rainforest ASMR Micro-Loops (تكرار الغابات المطيرة الحسي)",
        discoveryDate = today,
        opportunityReason = "تفاعل صوتي حسي ممتاز مع سهولة إنتاج عالية بالهاتف.",
        opportunityScore = 9,
        isUsed = false,
        trendScore = 88,
        isBestPick = false,
        growthScore = 22,
        freshnessScore = 18,
        interestScore = 13,
        competitionScore = 13,
        easeScore = 9,
        relevanceScore = 9,
        seriesScore = 4
      ),
      AgentMemoryTrend(
        trendName = "Analog 35mm Leaf-Vein Transition (انتقال عروق أوراق الشجر الفيلمي)",
        discoveryDate = today,
        opportunityReason = "جمالية التناظر الماكرو مع محاكاة الفيلم الحبيبي.",
        opportunityScore = 8,
        isUsed = false,
        trendScore = 82,
        isBestPick = false,
        growthScore = 20,
        freshnessScore = 17,
        interestScore = 12,
        competitionScore = 12,
        easeScore = 8,
        relevanceScore = 9,
        seriesScore = 4
      )
    )

    return ResearchResult(
      query = query,
      reportText = report,
      sources = listOf(
        GroundingSource("Google AI Studio Secrets Panel", "https://aistudio.google.com/"),
        GroundingSource("Instagram Creators Guidance", "https://about.instagram.com/")
      ),
      searchQueries = listOf(query),
      isLiveGrounded = false,
      discoveredMemoryTrends = demoRecords
    )
  }
}

package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BadgeEditing
import com.example.ui.theme.BadgeEditingBg
import com.example.ui.theme.BadgeGear
import com.example.ui.theme.BadgeGearBg
import com.example.ui.theme.BadgeHashtags
import com.example.ui.theme.BadgeHashtagsBg
import com.example.ui.theme.BadgeReels
import com.example.ui.theme.BadgeReelsBg
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ForestBorder
import com.example.ui.theme.ForestCard
import com.example.ui.theme.ForestDarkBg
import com.example.ui.theme.ForestSubtle
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SunlightAmber
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

// نموذج بيانات الترند
data class NatureTrend(
  val date: String,
  val trendTitle: String,
  val category: String,
  val hashtags: String,
  val contentIdea: String,
  val sourceLink: String
)

/**
 * رابط Google Sheet المنشور كـ CSV.
 * يمكن للمستخدم وضع رابطه هنا مباشرة أو استخدام الزر من داخل التطبيق.
 */
const val SHEET_URL = ""

val DEFAULT_DEMO_TRENDS = listOf(
  NatureTrend(
    date = "2026-09-24",
    trendTitle = "انتقال الضباب السريع مع شروق الشمس (Sunrise Fog Transition)",
    category = "Reels",
    hashtags = "#naturephotography #landscapephotography #goldenhour #mistyforest #reelsnature",
    contentIdea = "فيديو 5 ثوانٍ يبدأ بلقطة مظلمة لغابة كثيفة يغمرها الضباب، ثم يظهر شعاع الشمس الذهبي فجأة يخترق الأشجار بالتزامن مع ذروة الصوت (Sound Drop).",
    sourceLink = "https://www.instagram.com"
  ),
  NatureTrend(
    date = "2026-09-23",
    trendTitle = "درجات ألوان الخريف الدافئة والمزاجية (Moody Earthy Tones)",
    category = "Editing",
    hashtags = "#moodytones #autumncolors #lightroomnature #earthyvibes #forestlovers",
    contentIdea = "مقارنة قبل وبعد (Before & After) لتحرير لقطة جبل أو غابة بألوان ترابية وبرتقالية عميقة، مع كتابة قيم HSL وأسرار تدرج الألوان في الوصف.",
    sourceLink = "https://www.instagram.com"
  ),
  NatureTrend(
    date = "2026-09-22",
    trendTitle = "تصوير المياه الانسيابية بالهاتف دون فلتر ND (Long Exposure Hack)",
    category = "Gear",
    hashtags = "#waterfallphotography #longexposure #naturegeartips #shutterspeed #cameratricks",
    contentIdea = "كاروسيل تعليمي (4 شرائح) يوضح كيف تلتقط شلالاً أو نهراً ناعماً بالهاتف فقط عبر تقنية Live Photo في الآيفون أو وضع Pro في أندرويد بسرعة 1/2 ثانية.",
    sourceLink = "https://www.instagram.com"
  ),
  NatureTrend(
    date = "2026-09-21",
    trendTitle = "حزمة وسوم الطبيعة الهادئة الأكثر صعوداً في Explore هذا الأسبوع",
    category = "Hashtags",
    hashtags = "#visualsofearth #earthpix #roamtheplanet #artofvisuals #wildernesstones",
    contentIdea = "منشور يعرض أفضل 3 صور مناظر طبيعية لك، مع استراتيجية دمج وسوم متوازنة متوسطة الشهرة لزيادة فرص الظهور في صفحة الاستكشاف (Explore Page).",
    sourceLink = "https://www.instagram.com"
  ),
  NatureTrend(
    date = "2026-09-20",
    trendTitle = "لقطة الماكرو لقطرات الندى على أوراق الشجر (Dewdrop Macro Reel)",
    category = "Reels",
    hashtags = "#macronature #dewdrops #macrodetails #natureinmotion #greennature",
    contentIdea = "مقطع ريل مقرب جداً بحركة بطيئة (120fps) يركز على قطرة ماء تهتز على حافة ورقة شجر مبتلة بعد المطر مباشرة، مع سؤال تفاعلي في أول ثانية (Hook).",
    sourceLink = "https://www.instagram.com"
  )
)

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          NatureTrendRadarScreen()
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NatureTrendRadarScreen() {
  val coroutineScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  val clipboardManager = LocalClipboardManager.current
  val context = LocalContext.current

  var trendsList by remember { mutableStateOf<List<NatureTrend>>(emptyList()) }
  var isLoading by remember { mutableStateOf(true) }
  var statusMessage by remember { mutableStateOf("جاري فحص الترندات...") }
  var selectedCategory by remember { mutableStateOf("الكل") }
  var lastUpdated by remember { mutableStateOf("اليوم") }

  // دالة جلب البيانات (مع دعم CSV البسيط والـ Fallback التلقائي)
  fun loadData() {
    coroutineScope.launch {
      isLoading = true
      statusMessage = "جاري جلب الترندات..."

      if (SHEET_URL.isBlank() || SHEET_URL.contains("ضع_الرابط_هنا")) {
        // استخدام البيانات التجريبية الواقعية
        trendsList = DEFAULT_DEMO_TRENDS
        lastUpdated = "2026-09-24"
        statusMessage = "بيانات واقعية تجريبية"
        isLoading = false
        return@launch
      }

      val fetched = withContext(Dispatchers.IO) {
        try {
          val client = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
          val request = Request.Builder().url(SHEET_URL).build()
          client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
              val csvBody = response.body?.string() ?: ""
              parseCsv(csvBody)
            } else {
              null
            }
          }
        } catch (e: Exception) {
          null
        }
      }

      if (!fetched.isNullOrEmpty()) {
        trendsList = fetched
        val maxDate = fetched.maxOfOrNull { it.date } ?: "اليوم"
        lastUpdated = maxDate
        statusMessage = "مباشر: Google Sheet"
      } else {
        trendsList = DEFAULT_DEMO_TRENDS
        lastUpdated = "2026-09-24"
        statusMessage = "بيانات تجريبية (تعذر الاتصال)"
      }
      isLoading = false
    }
  }

  LaunchedEffect(Unit) {
    loadData()
  }

  // استخراج الفئات المتاحة
  val categories = remember(trendsList) {
    val dynamic = trendsList.map { it.category.trim() }.filter { it.isNotBlank() }.distinct()
    listOf("الكل") + dynamic
  }

  // تصفية الترندات بحسب الفئة
  val filteredTrends = remember(trendsList, selectedCategory) {
    if (selectedCategory == "الكل") {
      trendsList
    } else {
      trendsList.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(ForestDarkBg),
    containerColor = ForestDarkBg,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = ForestDarkBg,
          titleContentColor = TextMain,
          actionIconContentColor = EmeraldPrimary
        ),
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(EmeraldPrimary.copy(alpha = 0.15f))
                .border(1.dp, EmeraldPrimary.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Sensors,
                contentDescription = "Radar Icon",
                tint = EmeraldPrimary,
                modifier = Modifier.size(22.dp)
              )
            }
            Column {
              Text(
                text = "Nature Trend Radar",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
              )
              Text(
                text = "رادار ترندات تصوير الطبيعة لإنستغرام",
                fontSize = 11.5.sp,
                color = EmeraldPrimary,
                fontWeight = FontWeight.Medium
              )
            }
          }
        },
        actions = {
          IconButton(
            onClick = { loadData() },
            modifier = Modifier.testTag("refresh_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "تحديث البيانات",
              tint = EmeraldPrimary
            )
          }
        }
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // شريط الحالة والمعلومات
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier
            .background(EmeraldPrimary.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Box(
            modifier = Modifier
              .size(7.dp)
              .clip(CircleShape)
              .background(EmeraldPrimary)
          )
          Text(
            text = statusMessage,
            fontSize = 11.5.sp,
            color = EmeraldPrimary,
            fontWeight = FontWeight.SemiBold
          )
        }

        Text(
          text = "آخر تحديث: $lastUpdated",
          fontSize = 11.5.sp,
          color = TextMuted
        )
      }

      // أزرار الفلترة بحسب الفئة
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(categories) { cat ->
          val isSelected = selectedCategory.equals(cat, ignoreCase = true)
          FilterChip(
            selected = isSelected,
            onClick = { selectedCategory = cat },
            label = {
              Text(
                text = cat,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              containerColor = ForestCard,
              labelColor = TextMuted,
              selectedContainerColor = EmeraldPrimary,
              selectedLabelColor = ForestDarkBg
            ),
            border = FilterChipDefaults.filterChipBorder(
              borderColor = ForestBorder,
              selectedBorderColor = EmeraldPrimary,
              enabled = true,
              selected = isSelected
            ),
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier.testTag("filter_chip_$cat")
          )
        }
      }

      // محتوى الترندات أو حالة التحميل
      if (isLoading) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            CircularProgressIndicator(
              color = EmeraldPrimary,
              modifier = Modifier.size(42.dp)
            )
            Text(
              text = "جاري استكشاف أحدث الترندات...",
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold,
              color = TextMain
            )
            Text(
              text = "نحلل أفكار المحتوى والهاشتاغات المتداولة للمناظر الطبيعية.",
              fontSize = 12.5.sp,
              color = TextMuted,
              textAlign = TextAlign.Center
            )
          }
        }
      } else if (filteredTrends.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Text(text = "🍂", fontSize = 40.sp)
            Text(
              text = "لا توجد ترندات في هذه الفئة",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = TextMain
            )
            Text(
              text = "اختر فئة أخرى أو اضغط 'الكل' لمشاهدة جميع الأفكار.",
              fontSize = 13.sp,
              color = TextMuted
            )
          }
        }
      } else {
        LazyColumn(
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(filteredTrends) { trend ->
            TrendCardItem(
              trend = trend,
              onCopyHashtags = { tags ->
                clipboardManager.setText(AnnotatedString(tags))
                coroutineScope.launch {
                  snackbarHostState.showSnackbar("تم نسخ الهاشتاغات بنجاح!")
                }
              },
              onOpenSource = { link ->
                try {
                  val url = if (link.startsWith("http")) link else "https://www.instagram.com"
                  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                  context.startActivity(intent)
                } catch (e: Exception) {
                  coroutineScope.launch {
                    snackbarHostState.showSnackbar("تعذر فتح الرابط")
                  }
                }
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun TrendCardItem(
  trend: NatureTrend,
  onCopyHashtags: (String) -> Unit,
  onOpenSource: (String) -> Unit
) {
  var isCopied by remember { mutableStateOf(false) }

  val (catBadgeBg, catBadgeText) = when (trend.category.lowercase()) {
    "reels" -> BadgeReelsBg to BadgeReels
    "hashtags" -> BadgeHashtagsBg to BadgeHashtags
    "editing" -> BadgeEditingBg to BadgeEditing
    "gear" -> BadgeGearBg to BadgeGear
    else -> EmeraldPrimary.copy(alpha = 0.15f) to EmeraldPrimary
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("trend_card_${trend.trendTitle.take(15)}"),
    colors = CardDefaults.cardColors(containerColor = ForestCard),
    shape = RoundedCornerShape(18.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, ForestBorder)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // السطر العلوي: الفئة والتاريخ
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          color = catBadgeBg,
          shape = RoundedCornerShape(20.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, catBadgeText.copy(alpha = 0.35f))
        ) {
          Text(
            text = trend.category,
            color = catBadgeText,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }

        Text(
          text = trend.date,
          color = TextDim,
          fontSize = 11.5.sp
        )
      }

      // عنوان الترند
      Text(
        text = trend.trendTitle,
        color = TextMain,
        fontSize = 16.5.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 23.sp
      )

      // فكرة المحتوى المقترحة
      if (trend.contentIdea.isNotBlank()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ForestSubtle)
            .border(
              width = 1.dp,
              color = ForestBorder,
              shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = "Idea",
                tint = SunlightAmber,
                modifier = Modifier.size(15.dp)
              )
              Text(
                text = "فكرة المحتوى المقترحة:",
                color = SunlightAmber,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Text(
              text = trend.contentIdea,
              color = Color(0xFFE2E8F0),
              fontSize = 13.sp,
              lineHeight = 19.sp
            )
          }
        }
      }

      // الهاشتاغات
      if (trend.hashtags.isNotBlank()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF040A07))
            .border(1.dp, ForestBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Text(
            text = trend.hashtags,
            color = Color(0xFFA7F3D0),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 18.sp
          )
        }
      }

      // أزرار الإجراءات: نسخ الهاشتاغات + عرض المصدر
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (trend.hashtags.isNotBlank()) {
          Button(
            onClick = {
              onCopyHashtags(trend.hashtags)
              isCopied = true
            },
            modifier = Modifier
              .weight(1f)
              .height(46.dp)
              .testTag("copy_hashtags_btn"),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isCopied) EmeraldPrimary else EmeraldPrimary.copy(alpha = 0.15f),
              contentColor = if (isCopied) ForestDarkBg else EmeraldPrimary
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
          ) {
            Icon(
              imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
              contentDescription = "Copy",
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isCopied) "تم النسخ!" else "نسخ الهاشتاغات",
              fontSize = 12.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        OutlinedButton(
          onClick = { onOpenSource(trend.sourceLink) },
          modifier = Modifier
            .height(46.dp)
            .testTag("source_link_btn"),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TextMuted
          ),
          border = androidx.compose.foundation.BorderStroke(1.dp, ForestBorder),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 14.dp)
        ) {
          Text(
            text = "المصدر",
            fontSize = 12.5.sp,
            color = TextMuted
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = "Open Source",
            tint = TextMuted,
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }
  }
}

/**
 * دالة بسيطة لتحويل نص CSV إلى قائمة كائنات NatureTrend بدون أي مكتبة خارجية
 */
fun parseCsv(text: String): List<NatureTrend> {
  if (text.isBlank()) return emptyList()

  val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
  if (lines.size < 2) return emptyList()

  fun splitCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
      val c = line[i]
      if (c == '"') {
        if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
          current.append('"')
          i++
        } else {
          inQuotes = !inQuotes
        }
      } else if (c == ',' && !inQuotes) {
        result.add(current.toString().trim())
        current.clear()
      } else {
        current.append(c)
      }
      i++
    }
    result.add(current.toString().trim())
    return result
  }

  val headers = splitCsvLine(lines[0]).map { it.lowercase().replace(" ", "_") }
  val list = mutableListOf<NatureTrend>()

  for (lineIdx in 1 until lines.size) {
    val row = splitCsvLine(lines[lineIdx])
    if (row.isEmpty() || (row.size == 1 && row[0].isEmpty())) continue

    val map = mutableMapOf<String, String>()
    headers.forEachIndexed { idx, h ->
      if (idx < row.size) {
        map[h] = row[idx]
      }
    }

    list.add(
      NatureTrend(
        date = map["date"] ?: "2026-09-24",
        trendTitle = map["trend_title"] ?: map["title"] ?: "ترند بدون عنوان",
        category = map["category"] ?: "General",
        hashtags = map["hashtags"] ?: "",
        contentIdea = map["content_idea"] ?: map["idea"] ?: "",
        sourceLink = map["source_link"] ?: map["link"] ?: "https://www.instagram.com"
      )
    )
  }

  return list
}

package cn.snowrainyskr

import java.io.File
import java.nio.charset.Charset
import kotlin.math.roundToInt

fun main() {
	val songsDataBase = File("query.sql")
	val songsMap = """VALUES \('(.*?)', '(.*?)', (.*?), (.*?), (.*?), (.*?), (.*?)\);""".toRegex()
		.findAll(songsDataBase.readText()).map { matchResult ->
			matchResult.groupValues.run { Pair(this[1].lowercase(), Array(this.size - 2) { this[it + 2] }) }
		}.toList().associateBy({ it.first }) { it.second }.toMap()

	fun String.findInfo() =
		this.lowercase().run { songsMap[this] ?: songsMap.minBy { levenshteinDistance(this, it.key) }.value }

	val filePath = "input.html"
	val file = File(filePath)
	if (file.exists()) {
		file.readText().run {
			fun String.findList() =
				this.toRegex(RegexOption.DOT_MATCHES_ALL).findAll(this@run).map { it.groupValues[1] }.toList()

			fun List<String>.toIntList() = this.map { it.replace("""[,\s]""".toRegex(), "") }.toList()

			val titles = """标题.*?<span.*?>(.*?)<""".findList().map {
				when (it.hashCode()) {
					1278151819 -> "II"
					1741213705 -> "GLORY : ROAD"
					else -> it
				}
			}.toList()

			val scores = """作者.*?</span>.*?</span>(.*?)</div>""".findList().toIntList()
			val pures = """PURE</p>.*?class="score">(.*?)</span>""".findList().toIntList()
			val fars = """FAR</p>.*?class="score">(.*?)</span>""".findList().toIntList()
			val losts = """LOST</p>.*?class="score">(.*?)</span>""".findList().toIntList()
			val bigPures = List(40) { i ->
				val averageScore = 10_000_000.0 / (pures[i].toInt() + fars[i].toInt() + losts[i].toInt()).toDouble()
				val noneBigPuresScore = (pures[i].toDouble() + fars[i].toDouble() / 2.0) * averageScore
				(scores[i].toDouble() - noneBigPuresScore).roundToInt().toString()
			}
			val difficulties = """label small-label">(.*?)</span>""".findList().map { difficulty ->
				when (difficulty) {
					"ETR" -> "Eternal"
					"BYD" -> "Beyond"
					"FTR" -> "Future"
					"PRS" -> "Present"
					else -> "Past"
				}
			}.toList()

			val ids = List(40) { titles[it].findInfo()[0] }
			val constants = List(40) { i ->
				fun String.index() = when (this) {
					"Eternal" -> 5
					"Beyond" -> 4
					"Future" -> 3
					"Present" -> 2
					else -> 1
				}
				titles[i].findInfo()[difficulties[i].index()].replace("""['\s]""".toRegex(), "")
			}

			val csvHeader =
				listOf("SongName", "SongId", "Difficulty", "Score", "Perfect", "Perfect+", "Far", "Lost", "Constant")
			val infoArray = arrayOf(titles, ids, difficulties, scores, pures, bigPures, fars, losts, constants)
			val b30CSVContent = List(30) { i -> infoArray.map { it[i] }.toList() }
			val r10CSVContent = List(10) { i -> infoArray.map { it[i + 30] }.toList() }
			val b30CSV = (listOf(csvHeader) + b30CSVContent).map { it.joinToString(",") }.toList().joinToString("\n")
			val r10CSV = (listOf(csvHeader) + r10CSVContent).map { it.joinToString(",") }.toList().joinToString("\n")

			val b30Output = File("B30.csv")
			b30Output.writeText(b30CSV, Charset.forName("GB2312"))

			val r10Output = File("R10.csv")
			r10Output.writeText(r10CSV, Charset.forName("GB2312"))
		}
	}
}

fun levenshteinDistance(s1: String, s2: String): Int {
	val m = s1.length
	val n = s2.length
	if (m == 0) return n
	if (n == 0) return m

	val d = Array(m + 1) { IntArray(n + 1) }

	for (i in 0..m) d[i][0] = i
	for (j in 0..n) d[0][j] = j

	for (j in 1..n) {
		for (i in 1..m) {
			val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
			d[i][j] = minOf(
				d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost
			)
		}
	}

	return d[m][n]
}
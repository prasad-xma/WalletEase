package com.example.walletease

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.io.BufferedReader
import java.io.InputStreamReader

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate


class HistoryActivity : AppCompatActivity() {


    // bottom navigation
    private lateinit var bottomNavHome: LinearLayout
    private lateinit var bottomNavAction: LinearLayout
    private lateinit var bottomNavHistory: LinearLayout

    private lateinit var pieChart: PieChart
    // barChart
    private lateinit var barChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // bottom navigation
        bottomNavHome = findViewById(R.id.bottomNavHome)
        bottomNavHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        bottomNavAction = findViewById(R.id.bottomNavAction)
        bottomNavAction.setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }
        bottomNavHistory = findViewById(R.id.bottomNavHistory)
        bottomNavHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // Pie Chart
        pieChart = findViewById(R.id.pieChart1)
        // bar chart
        barChart = findViewById(R.id.barChart1)

        val spendingByCategory = readWithdrawalsAndGroupByCategory()
        setupPieChart(spendingByCategory)
        setupBarChart(spendingByCategory)
    }

    private fun readWithdrawalsAndGroupByCategory(): Map<String, Float> {
        val categoryTotals = mutableMapOf<String, Float>()

        try {
            val reader = BufferedReader(InputStreamReader(openFileInput("withdrawals.txt")))
            reader.forEachLine { line ->
                val parts = line.split(",")
                if(parts.size >= 2) {
                    val category = parts[0]
                    val amount = parts[1].toFloatOrNull() ?: 0f
                    categoryTotals[category] = categoryTotals.getOrDefault(category, 0f) + amount
                }
            }
            reader.close()

        }catch (e: Exception) {
            e.printStackTrace()
        }
        return  categoryTotals
    }

    private fun setupPieChart(categoryData: Map<String, Float>) {
        val entries = ArrayList<PieEntry>()
        categoryData.forEach { (category, total) ->
            entries.add(PieEntry(total, category))
        }

        val dataSet = PieDataSet(entries, "Spending by Category")
        dataSet.setColors(
            Color.rgb(244, 67, 54),
            Color.rgb(33, 150, 243),
            Color.rgb(76, 175, 80),
            Color.rgb(255, 193, 7),
            Color.rgb(156, 39, 176),
            Color.rgb(255, 87, 34)
        )
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 16f

        val pieData = PieData(dataSet)

        pieChart.data = pieData
        pieChart.setUsePercentValues(true)
        pieChart.description = Description().apply { text = "Spending Breakdown" }
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(14f)
        pieChart.centerText = "Spending"
        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    // setup bar chart
    private fun setupBarChart(categoryData: Map<String, Float>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        var index = 0f

        categoryData.forEach { (category, total) ->
            entries.add(BarEntry(index, total))
            labels.add(category)
            index++
        }

        val dataSet = BarDataSet(entries, "Spending by Category")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 14f

        val barData = BarData(dataSet)
        barChart.data = barData

        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.granularity = 1f
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.labelRotationAngle = -45f
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.isEnabled = false

        barChart.description.isEnabled = false
        barChart.animateY(1000)
        barChart.invalidate()
    }

}
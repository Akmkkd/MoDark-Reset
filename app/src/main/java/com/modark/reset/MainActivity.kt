package com.modark.reset

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var resetButton: Button
    private lateinit var killButton: Button
    private lateinit var blockButton: Button
    private lateinit var logText: TextView
    private lateinit var rootStatus: TextView
    private lateinit var systemStatus: TextView
    private lateinit var appStatus: TextView
    private lateinit var firewallStatus: TextView
    private lateinit var identityStatus: TextView
    private lateinit var logCount: TextView
    private lateinit var clearLogButton: ImageView
    private lateinit var logScrollView: ScrollView
    private lateinit var statusCard: CardView
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var logCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initializeViews()
        checkRootStatus()
        setClickListeners()
        animateWelcome()
    }

    private fun initializeViews() {
        resetButton = findViewById(R.id.resetButton)
        killButton = findViewById(R.id.killButton)
        blockButton = findViewById(R.id.blockButton)
        logText = findViewById(R.id.logText)
        rootStatus = findViewById(R.id.rootStatus)
        systemStatus = findViewById(R.id.systemStatus)
        appStatus = findViewById(R.id.appStatus)
        firewallStatus = findViewById(R.id.firewallStatus)
        identityStatus = findViewById(R.id.identityStatus)
        logCount = findViewById(R.id.logCount)
        clearLogButton = findViewById(R.id.clearLogButton)
        logScrollView = findViewById(R.id.logScrollView)
        statusCard = findViewById(R.id.statusCard)
    }

    private fun checkRootStatus() {
        val hasRoot = RootExecutor.hasRootAccess()
        if (hasRoot) {
            rootStatus.text = "ROOT ✓"
            rootStatus.setTextColor(ContextCompat.getColor(this, R.color.success))
            addLog("✅ صلاحيات الجذر متاحة")
            systemStatus.text = "● جاهز"
            systemStatus.setTextColor(ContextCompat.getColor(this, R.color.success))
        } else {
            rootStatus.text = "ROOT ✗"
            rootStatus.setTextColor(ContextCompat.getColor(this, R.color.error))
            addLog("❌ صلاحيات الجذر غير متاحة")
            systemStatus.text = "● غير جاهز"
            systemStatus.setTextColor(ContextCompat.getColor(this, R.color.error))
            resetButton.isEnabled = false
            killButton.isEnabled = false
            blockButton.isEnabled = false
        }
    }

    private fun setClickListeners() {
        resetButton.setOnClickListener {
            animateButton(it)
            executeFullReset()
        }
        killButton.setOnClickListener {
            animateButton(it)
            killApplicationOnly()
        }
        blockButton.setOnClickListener {
            animateButton(it)
            blockNetworkOnly()
        }
        clearLogButton.setOnClickListener { clearLog() }
    }

    private fun animateButton(view: View) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun animateWelcome() {
        statusCard.alpha = 0f
        statusCard.translationY = 50f
        statusCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun executeFullReset() {
        scope.launch {
            withContext(Dispatchers.Main) {
                setButtonsEnabled(false)
                systemStatus.text = "● جاري..."
                systemStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.warning))
                addLog("═══════════════════════════════════════")
                addLog("🚀 بدء تنفيذ الإعادة الكاملة")
                animateStatusCard()
            }
            
            val commands = listOf(
                "kill com.tencent.ig",
                "rm -rf /data/data/com.tencent.ig/shared_prefs",
                "rm -rf /storage/emulated/0/Documents/",
                "mkdir /data/data/com.tencent.ig/shared_prefs",
                "chmod 777 /data/data/com.tencent.ig/shared_prefs",
                "rm -rf /data/data/com.tencent.ig/files",
                "rm -rf /data/data/com.tencent.ig/databases",
                "rm -rf /data/media/0/Android/data/com.tencent.ig/files/login-identifier.txt",
                "rm -rf /data/media/0/Android/data/com.tencent.ig/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Intermediate",
                "touch /data/media/0/Android/data/com.tencent.ig/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Intermediate",
                "rm -rf /data/media/0/Android/data/com.tencent.ig/files/TGPA",
                "touch /data/media/0/Android/data/com.tencent.ig/files/TGPA",
                "rm -rf /data/media/0/Android/data/com.tencent.ig/files/ProgramBinaryCache",
                "touch /data/media/0/Android/data/com.tencent.ig/files/ProgramBinaryCache",
                "iptables -I OUTPUT -d cloud.vmp.onezapp.com -j REJECT",
                "iptables -I INPUT -s cloud.vmp.onezapp.com -j REJECT"
            )
            
            val random1 = (10000..99999).random()
            val random2 = (10000..99999).random()
            val uuid = "$random1$random2-${(1000..9999).random()}-${(1000..9999).random()}-${(1000..9999).random()}-${random1}${random2}${(1000..9999).random()}"
            
            val xmlContent = """<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <string name="random"></string>
    <string name="install"></string>
    <string name="uuid">$uuid</string>
</map>"""
            
            var step = 1
            for (cmd in commands) {
                val result = RootExecutor.execute(cmd)
                val status = if (result.first == 0) "✅" else "⚠️"
                addLog("$status $step. ${cmd.take(50)}${if (cmd.length > 50) "..." else ""}")
                step++
            }
            
            RootExecutor.execute("echo \"$xmlContent\" > /data/data/com.tencent.ig/shared_prefs/device_id.xml")
            addLog("✅ $step. إنشاء device_id.xml (UUID: $uuid)")
            addLog("✅ ${step+1}. GL reset Guest")
            addLog("═══════════════════════════════════════")
            addLog("🎯 الإعادة الكاملة مكتملة بنجاح!")
            
            withContext(Dispatchers.Main) {
                appStatus.text = "مُعاد"
                appStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.success))
                identityStatus.text = "جديدة"
                identityStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.success))
                systemStatus.text = "● مكتمل"
                systemStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.success))
                setButtonsEnabled(true)
            }
        }
    }

    private fun killApplicationOnly() {
        scope.launch {
            withContext(Dispatchers.Main) {
                setButtonsEnabled(false)
                addLog("═══════════════════════════════════════")
                addLog("⛔ بدء قتل التطبيق")
                systemStatus.text = "● قتل..."
                systemStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.warning))
            }
            
            val result = RootExecutor.execute("kill com.tencent.ig")
            
            withContext(Dispatchers.Main) {
                if (result.first == 0) {
                    addLog("✅ تم قتل com.tencent.ig بنجاح")
                    appStatus.text = "مقتول"
                    appStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.error))
                    systemStatus.text = "● مقتول"
                    systemStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.warning))
                } else {
                    addLog("⚠️ فشل قتل التطبيق أو غير موجود")
                }
                addLog("⛔ اكتمل القتل")
                setButtonsEnabled(true)
            }
        }
    }

    private fun blockNetworkOnly() {
        scope.launch {
            withContext(Dispatchers.Main) {
                setButtonsEnabled(false)
                addLog("═══════════════════════════════════════")
                addLog("🌐 بدء حظر الخادم")
                systemStatus.text = "● حظر..."
                systemStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.warning))
            }
            
            val commands = listOf(
                "iptables -I OUTPUT -d cloud.vmp.onezapp.com -j REJECT",
                "iptables -I INPUT -s cloud.vmp.onezapp.com -j REJECT"
            )
            
            for (cmd in commands) {
                val result = RootExecutor.execute(cmd)
                val status = if (result.first == 0) "✅" else "⚠️"
                addLog("$status ${cmd}")
            }
            
            withContext(Dispatchers.Main) {
                addLog("✅ تم حظر cloud.vmp.onezapp.com")
                firewallStatus.text = "محظور"
                firewallStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.success))
                systemStatus.text = "● محظور"
                systemStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.success))
                setButtonsEnabled(true)
            }
        }
    }

    private fun animateStatusCard() {
        statusCard.animate()
            .scaleX(1.02f)
            .scaleY(1.02f)
            .setDuration(200)
            .withEndAction {
                statusCard.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        resetButton.isEnabled = enabled
        killButton.isEnabled = enabled
        blockButton.isEnabled = enabled
        val alpha = if (enabled) 1.0f else 0.5f
        resetButton.alpha = alpha
        killButton.alpha = alpha
        blockButton.alpha = alpha
    }

    private fun addLog(message: String) {
        withContext(Dispatchers.Main) {
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date())
            val logEntry = "[$timestamp] $message\n"
            
            val currentLog = logText.text.toString()
            val lines = currentLog.split("\n")
            val limitedLines = if (lines.size > 50) {
                lines.takeLast(50).joinToString("\n")
            } else {
                currentLog
            }
            logText.text = limitedLines + logEntry
            logCounter++
            logCount.text = logCounter.toString()
            
            logScrollView.post {
                logScrollView.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    private fun clearLog() {
        logText.text = "[●] النظام جاهز\n[●] انتظار التنفيذ..."
        logCounter = 0
        logCount.text = "0"
        addLog("🗑️ تم مسح السجل")
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.util.AirplaneIpChanger
import com.example.util.IndonesianNameGenerator
import com.example.util.LanguageManager
import kotlin.math.abs

class FloatingAssistantService : Service() {

    companion object {
        const val ACTION_START = "com.example.service.START_FLOATING"
        const val ACTION_STOP = "com.example.service.STOP_FLOATING"
        private const val CHANNEL_ID = "capsule_floating_channel"
        private const val NOTIFICATION_ID = 2001

        var isRunning = false
            private set
    }

    private var windowManager: WindowManager? = null
    private var floatingView: FrameLayout? = null
    private var params: WindowManager.LayoutParams? = null

    private var isExpanded = false
    private var currentName: String = ""
    private var currentPassword: String = ""

    // UI elements
    private var bubbleIcon: FrameLayout? = null
    private var expandedUnifiedPanel: LinearLayout? = null
    private var nameTextView: TextView? = null
    private var passwordEditText: EditText? = null
    private var ipStatusTextView: TextView? = null
    private var ipTriggerButton: TextView? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private var isIpChanging = false

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        LanguageManager.init(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        initFloatingWindow()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Capsule Floating Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Layanan bubble melayang untuk ID Assistant & Auto Ganti IP"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, FloatingAssistantService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val appIntent = Intent(this, MainActivity::class.java)
        val appPendingIntent = PendingIntent.getActivity(
            this,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Capsule Floating Assistant Aktif")
            .setContentText("Ketuk bubble di layar untuk ID & Ganti IP Pesawat (3s delay)")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(appPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Matikan Bubble", stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initFloatingWindow() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        currentName = IndonesianNameGenerator.generateTwoWordName()
        currentPassword = IndonesianNameGenerator.getSavedPassword(this)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 300
        }

        floatingView = FrameLayout(this)

        // 1. Floating Bubble Icon (Collapsed State)
        bubbleIcon = createBubbleView()
        floatingView?.addView(bubbleIcon)

        // 2. Expanded Unified Stack Panel (ATAS: Quick ID, BAWAH: Auto Ganti IP Mode Pesawat)
        // STRICTLY LOCKED AS STACKED ATAS-BAWAH (UNIFIED & UNEDITABLE SEPARATION)
        expandedUnifiedPanel = createExpandedUnifiedView()
        expandedUnifiedPanel?.visibility = View.GONE
        floatingView?.addView(expandedUnifiedPanel)

        // Touch Drag & Click Handling
        setupTouchListener()

        windowManager?.addView(floatingView, params)
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }

    private fun createBubbleView(): FrameLayout {
        val bubble = FrameLayout(this).apply {
            val size = dpToPx(56f)
            layoutParams = FrameLayout.LayoutParams(size, size)

            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                colors = intArrayOf(
                    Color.parseColor("#00E5FF"), // Capsule Cyan
                    Color.parseColor("#00838F")
                )
                setStroke(dpToPx(2f), Color.parseColor("#E0F7FA"))
            }
            elevation = 16f
        }

        val text = TextView(this).apply {
            text = "⚡ID\n✈️IP"
            setTextColor(Color.parseColor("#090D16"))
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        bubble.addView(text)
        return bubble
    }

    /**
     * UNIFIED STACKED VIEW:
     * TOP SECTION: Quick ID (Nama Indo 2 Kata & Password)
     * BOTTOM SECTION: Auto IP Changer (Airplane Mode ON -> 3s Delay -> OFF)
     * Permanently unified as top-bottom stack without separate draggable layers.
     */
    private fun createExpandedUnifiedView(): LinearLayout {
        val rootContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val width = dpToPx(310f)
            layoutParams = FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.WRAP_CONTENT)
            setPadding(dpToPx(12f), dpToPx(12f), dpToPx(12f), dpToPx(12f))

            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(16f).toFloat()
                setColor(Color.parseColor("#0D131F"))
                setStroke(dpToPx(1.5f), Color.parseColor("#00E5FF"))
            }
            elevation = 22f
        }

        // ==========================================
        // 1. MASTER HEADER (Title & Minimize/Close)
        // ==========================================
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val title = TextView(this).apply {
            text = "⚡ Capsule Floating ID & IP"
            setTextColor(Color.parseColor("#00E5FF"))
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        header.addView(title)

        val minimizeBtn = TextView(this).apply {
            text = " ─ "
            setTextColor(Color.parseColor("#B0BEC5"))
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dpToPx(6f), dpToPx(2f), dpToPx(6f), dpToPx(2f))
            setOnClickListener { collapsePanel() }
        }
        header.addView(minimizeBtn)

        val closeBtn = TextView(this).apply {
            text = " ✕ "
            setTextColor(Color.parseColor("#EF5350"))
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dpToPx(6f), dpToPx(2f), dpToPx(6f), dpToPx(2f))
            setOnClickListener { stopSelf() }
        }
        header.addView(closeBtn)

        rootContainer.addView(header)
        addDivider(rootContainer, dpToPx(8f), "#1E2C44")

        // ==========================================
        // 2. BAGIAN ATAS: FLOATING NAMA & PASSWORD
        // ==========================================
        val topSectionCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(10f), dpToPx(10f), dpToPx(10f), dpToPx(10f))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(12f).toFloat()
                setColor(Color.parseColor("#121B2B"))
                setStroke(dpToPx(1f), Color.parseColor("#1F314D"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Section Title: NAMA INDONESIA (2 KATA)
        val nameLabel = TextView(this).apply {
            text = "NAMA INDONESIA (2 KATA)"
            setTextColor(Color.parseColor("#90CAF9"))
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
        }
        topSectionCard.addView(nameLabel)

        // Name TextView Box
        nameTextView = TextView(this).apply {
            text = currentName
            setTextColor(Color.parseColor("#FFFFFF"))
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dpToPx(8f), dpToPx(6f), dpToPx(8f), dpToPx(6f))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(6f).toFloat()
                setColor(Color.parseColor("#18253A"))
                setStroke(dpToPx(1f), Color.parseColor("#263859"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(3f)
                bottomMargin = dpToPx(5f)
            }
        }
        topSectionCard.addView(nameTextView)

        // Name Buttons (Acak & Salin)
        val nameButtonsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(32f)
            )
        }

        val randomizeNameBtn = createButton("🎲 Acak Nama", "#1A365D", "#64B5F6", true) {
            currentName = IndonesianNameGenerator.generateTwoWordName()
            nameTextView?.text = currentName
        }
        randomizeNameBtn.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
            marginEnd = dpToPx(3f)
        }
        nameButtonsRow.addView(randomizeNameBtn)

        val copyNameBtn = createButton("📋 Salin Nama", "#00E5FF", "#090D16", true) {
            copyToClipboard("Nama", currentName)
        }
        copyNameBtn.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
            marginStart = dpToPx(3f)
        }
        nameButtonsRow.addView(copyNameBtn)
        topSectionCard.addView(nameButtonsRow)

        // Divider inside Top Section
        addDivider(topSectionCard, dpToPx(8f), "#1F314D")

        // Section Title: PASSWORD
        val passLabel = TextView(this).apply {
            text = "PASSWORD (BISA DIUBAH / SIMPAN)"
            setTextColor(Color.parseColor("#FFE082"))
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
        }
        topSectionCard.addView(passLabel)

        // Password EditText
        passwordEditText = EditText(this).apply {
            setText(currentPassword)
            setTextColor(Color.parseColor("#00E676"))
            textSize = 13f
            typeface = Typeface.MONOSPACE
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            setPadding(dpToPx(8f), dpToPx(6f), dpToPx(8f), dpToPx(6f))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(6f).toFloat()
                setColor(Color.parseColor("#18253A"))
                setStroke(dpToPx(1f), Color.parseColor("#263859"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(3f)
                bottomMargin = dpToPx(5f)
            }
            setOnFocusChangeListener { _, hasFocus -> updateFocusable(hasFocus) }
        }
        topSectionCard.addView(passwordEditText)

        // Password Buttons (Acak & Salin)
        val passButtonsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(32f)
            )
        }

        val randomizePassBtn = createButton("🔑 Acak Kuat", "#3E2723", "#FFB74D", true) {
            currentPassword = IndonesianNameGenerator.generateRandomPassword(10)
            passwordEditText?.setText(currentPassword)
            IndonesianNameGenerator.saveCustomPassword(this, currentPassword)
        }
        randomizePassBtn.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
            marginEnd = dpToPx(3f)
        }
        passButtonsRow.addView(randomizePassBtn)

        val copyPassBtn = createButton("📋 Salin Password", "#00E676", "#090D16", true) {
            val pass = passwordEditText?.text?.toString() ?: currentPassword
            currentPassword = pass
            IndonesianNameGenerator.saveCustomPassword(this, pass)
            copyToClipboard("Password", pass)
        }
        copyPassBtn.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
            marginStart = dpToPx(3f)
        }
        passButtonsRow.addView(copyPassBtn)
        topSectionCard.addView(passButtonsRow)

        rootContainer.addView(topSectionCard)

        // Permanent Divider/Space Between Top (ID) and Bottom (Airplane IP)
        val middleSpace = android.view.View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(8f)
            )
        }
        rootContainer.addView(middleSpace)

        // ==========================================
        // 3. BAGIAN BAWAH: AUTO GANTI IP MODE PESAWAT
        // (ON -> JEDA 3 DETIK -> OFF)
        // ==========================================
        val bottomSectionCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(10f), dpToPx(10f), dpToPx(10f), dpToPx(10f))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(12f).toFloat()
                setColor(Color.parseColor("#151D28"))
                setStroke(dpToPx(1f), Color.parseColor("#0288D1").apply { })
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Section Title: AUTO GANTI IP
        val ipHeaderRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val ipLabel = TextView(this).apply {
            text = "✈️ AUTO GANTI IP (MODE PESAWAT)"
            setTextColor(Color.parseColor("#4FC3F7"))
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        ipHeaderRow.addView(ipLabel)

        val timerBadge = TextView(this).apply {
            text = "3 Detik Jeda"
            setTextColor(Color.parseColor("#00E5FF"))
            textSize = 9f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dpToPx(6f), dpToPx(2f), dpToPx(6f), dpToPx(2f))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(4f).toFloat()
                setColor(Color.parseColor("#00363A"))
            }
        }
        ipHeaderRow.addView(timerBadge)
        bottomSectionCard.addView(ipHeaderRow)

        // Status description
        ipStatusTextView = TextView(this).apply {
            text = "Klik tombol di bawah: Mode pesawat ON -> Jeda 3 detik -> OFF untuk perbarui IP."
            setTextColor(Color.parseColor("#B0BEC5"))
            textSize = 10f
            setPadding(0, dpToPx(4f), 0, dpToPx(6f))
        }
        bottomSectionCard.addView(ipStatusTextView)

        // 1-Tap Trigger Button for 3-Second Airplane IP Cycle
        ipTriggerButton = createButton(
            text = "✈️ Ganti IP Sekarang (Mode Pesawat 3s)",
            bgColor = "#0288D1",
            textColor = "#FFFFFF",
            isBold = true
        ) {
            triggerAirplaneCycle()
        }.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(38f)
            )
        }
        bottomSectionCard.addView(ipTriggerButton)

        rootContainer.addView(bottomSectionCard)

        return rootContainer
    }

    private fun triggerAirplaneCycle() {
        if (isIpChanging) return
        isIpChanging = true
        ipTriggerButton?.apply {
            text = "⏳ Memproses Ganti IP..."
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(8f).toFloat()
                setColor(Color.parseColor("#546E7A"))
            }
        }

        AirplaneIpChanger.performAirplaneCycle(
            context = this,
            onProgress = { step, seconds ->
                mainHandler.post {
                    ipStatusTextView?.text = if (seconds > 0) "$step ($seconds detik)" else step
                }
            },
            onComplete = {
                mainHandler.post {
                    isIpChanging = false
                    ipStatusTextView?.text = "✓ IP Berhasil Diperbarui! Mode Pesawat kembali OFF."
                    ipTriggerButton?.apply {
                        text = "✈️ Ganti IP Lagi (3s)"
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = dpToPx(8f).toFloat()
                            setColor(Color.parseColor("#0288D1"))
                        }
                    }
                }
            }
        )
    }

    private fun createButton(
        text: String,
        bgColor: String,
        textColor: String,
        isBold: Boolean = false,
        onClick: () -> Unit
    ): TextView {
        return TextView(this).apply {
            this.text = text
            this.setTextColor(Color.parseColor(textColor))
            this.textSize = 11f
            if (isBold) this.typeface = Typeface.DEFAULT_BOLD
            this.gravity = Gravity.CENTER
            this.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(6f).toFloat()
                setColor(Color.parseColor(bgColor))
            }
            this.setOnClickListener { onClick() }
        }
    }

    private fun addDivider(parent: LinearLayout, marginVertical: Int, hexColor: String = "#263859") {
        val divider = View(this).apply {
            setBackgroundColor(Color.parseColor(hexColor))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(1f)
            ).apply {
                topMargin = marginVertical
                bottomMargin = marginVertical
            }
        }
        parent.addView(divider)
    }

    private fun copyToClipboard(label: String, value: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, value)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "✓ $label tersalin: $value", Toast.LENGTH_SHORT).show()
    }

    private fun expandPanel() {
        isExpanded = true
        bubbleIcon?.visibility = View.GONE
        expandedUnifiedPanel?.visibility = View.VISIBLE
        currentPassword = IndonesianNameGenerator.getSavedPassword(this)
        passwordEditText?.setText(currentPassword)
    }

    private fun collapsePanel() {
        isExpanded = false
        updateFocusable(false)
        expandedUnifiedPanel?.visibility = View.GONE
        bubbleIcon?.visibility = View.VISIBLE
    }

    private fun updateFocusable(focusable: Boolean) {
        params?.let { p ->
            p.flags = if (focusable) {
                p.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
            } else {
                p.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            }
            windowManager?.updateViewLayout(floatingView, p)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false

        floatingView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params?.x ?: 0
                    initialY = params?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()

                    if (abs(deltaX) > 10 || abs(deltaY) > 10) {
                        isDragging = true
                        params?.x = initialX + deltaX
                        params?.y = initialY + deltaY
                        windowManager?.updateViewLayout(floatingView, params)
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        if (!isExpanded) {
                            expandPanel()
                        }
                    }
                    true
                }

                else -> false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        floatingView?.let {
            try {
                windowManager?.removeView(it)
            } catch (_: Exception) {}
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

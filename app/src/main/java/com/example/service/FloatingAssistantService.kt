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
import android.os.IBinder
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.util.IndonesianNameGenerator
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
    private var expandedCard: LinearLayout? = null
    private var nameTextView: TextView? = null
    private var passwordEditText: EditText? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
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
                description = "Layanan bubble melayang untuk nama & password cepat"
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
            .setContentText("Ketuk bubble di layar untuk salin Nama Indo 2 Kata & Password")
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

        // 1. Build Floating Bubble Icon (Collapsed State)
        bubbleIcon = createBubbleView()
        floatingView?.addView(bubbleIcon)

        // 2. Build Expanded Floating Panel
        expandedCard = createExpandedView()
        expandedCard?.visibility = View.GONE
        floatingView?.addView(expandedCard)

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

            // Cyber Cyan Glowing Circular Background
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
            text = "⚡ID"
            setTextColor(Color.parseColor("#090D16"))
            textSize = 14f
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

    private fun createExpandedView(): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val width = dpToPx(300f)
            layoutParams = FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.WRAP_CONTENT)
            setPadding(dpToPx(14f), dpToPx(14f), dpToPx(14f), dpToPx(14f))

            // Dark Cyber Card Background
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(16f).toFloat()
                setColor(Color.parseColor("#101726"))
                setStroke(dpToPx(1.5f), Color.parseColor("#00E5FF"))
            }
            elevation = 20f
        }

        // Header Row: Title & Close / Minimize Buttons
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val title = TextView(this).apply {
            text = "⚡ Capsule Quick ID"
            setTextColor(Color.parseColor("#00E5FF"))
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        header.addView(title)

        // Minimize (-) Button
        val minimizeBtn = TextView(this).apply {
            text = " ─ "
            setTextColor(Color.parseColor("#B0BEC5"))
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dpToPx(6f), dpToPx(2f), dpToPx(6f), dpToPx(2f))
            setOnClickListener { collapsePanel() }
        }
        header.addView(minimizeBtn)

        // Close (X) Button (Stops Service)
        val closeBtn = TextView(this).apply {
            text = " ✕ "
            setTextColor(Color.parseColor("#EF5350"))
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dpToPx(6f), dpToPx(2f), dpToPx(6f), dpToPx(2f))
            setOnClickListener { stopSelf() }
        }
        header.addView(closeBtn)

        container.addView(header)
        addDivider(container, dpToPx(10f))

        // SECTION 1: NAMA INDONESIA (2 KATA)
        val nameLabel = TextView(this).apply {
            text = "NAMA INDONESIA (2 KATA)"
            setTextColor(Color.parseColor("#90CAF9"))
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
        }
        container.addView(nameLabel)

        // Name Box Display
        nameTextView = TextView(this).apply {
            text = currentName
            setTextColor(Color.parseColor("#FFFFFF"))
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dpToPx(10f), dpToPx(8f), dpToPx(10f), dpToPx(8f))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(8f).toFloat()
                setColor(Color.parseColor("#182338"))
                setStroke(dpToPx(1f), Color.parseColor("#263859"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(4f)
                bottomMargin = dpToPx(6f)
            }
        }
        container.addView(nameTextView)

        // Name Action Buttons (Acak & Salin)
        val nameButtonsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(36f)
            )
        }

        val randomizeNameBtn = createButton(
            text = "🎲 Acak Nama",
            bgColor = "#1A365D",
            textColor = "#64B5F6",
            isBold = true
        ) {
            currentName = IndonesianNameGenerator.generateTwoWordName()
            nameTextView?.text = currentName
        }
        randomizeNameBtn.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
            marginEnd = dpToPx(4f)
        }
        nameButtonsRow.addView(randomizeNameBtn)

        val copyNameBtn = createButton(
            text = "📋 Salin Nama",
            bgColor = "#00E5FF",
            textColor = "#090D16",
            isBold = true
        ) {
            copyToClipboard("Nama", currentName)
        }
        copyNameBtn.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
            marginStart = dpToPx(4f)
        }
        nameButtonsRow.addView(copyNameBtn)

        container.addView(nameButtonsRow)
        addDivider(container, dpToPx(12f))

        // SECTION 2: PASSWORD
        val passLabel = TextView(this).apply {
            text = "PASSWORD (BISA DIUBAH / SIMPAN)"
            setTextColor(Color.parseColor("#FFE082"))
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
        }
        container.addView(passLabel)

        // Password Input / Display
        passwordEditText = EditText(this).apply {
            setText(currentPassword)
            setTextColor(Color.parseColor("#00E676"))
            textSize = 14f
            typeface = Typeface.MONOSPACE
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            setPadding(dpToPx(10f), dpToPx(8f), dpToPx(10f), dpToPx(8f))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(8f).toFloat()
                setColor(Color.parseColor("#182338"))
                setStroke(dpToPx(1f), Color.parseColor("#263859"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(4f)
                bottomMargin = dpToPx(6f)
            }
            setOnFocusChangeListener { _, hasFocus ->
                updateFocusable(hasFocus)
            }
        }
        container.addView(passwordEditText)

        // Password Action Buttons (Acak & Salin)
        val passButtonsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(36f)
            )
        }

        val randomizePassBtn = createButton(
            text = "🔑 Acak Baru",
            bgColor = "#3E2723",
            textColor = "#FFB74D",
            isBold = true
        ) {
            currentPassword = IndonesianNameGenerator.generateRandomPassword(10)
            passwordEditText?.setText(currentPassword)
            IndonesianNameGenerator.saveCustomPassword(this, currentPassword)
        }
        randomizePassBtn.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
            marginEnd = dpToPx(4f)
        }
        passButtonsRow.addView(randomizePassBtn)

        val copyPassBtn = createButton(
            text = "📋 Salin Password",
            bgColor = "#00E676",
            textColor = "#090D16",
            isBold = true
        ) {
            val pass = passwordEditText?.text?.toString() ?: currentPassword
            currentPassword = pass
            IndonesianNameGenerator.saveCustomPassword(this, pass)
            copyToClipboard("Password", pass)
        }
        copyPassBtn.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
            marginStart = dpToPx(4f)
        }
        passButtonsRow.addView(copyPassBtn)

        container.addView(passButtonsRow)

        return container
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
                cornerRadius = dpToPx(8f).toFloat()
                setColor(Color.parseColor(bgColor))
            }
            this.setOnClickListener { onClick() }
        }
    }

    private fun addDivider(parent: LinearLayout, marginVertical: Int) {
        val divider = View(this).apply {
            setBackgroundColor(Color.parseColor("#263859"))
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
        expandedCard?.visibility = View.VISIBLE
        // Update values
        currentPassword = IndonesianNameGenerator.getSavedPassword(this)
        passwordEditText?.setText(currentPassword)
    }

    private fun collapsePanel() {
        isExpanded = false
        updateFocusable(false)
        expandedCard?.visibility = View.GONE
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

package cn.lanzp.jffw.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Calendar;

import cn.lanzp.jffw.model.MeridianData;

/**
 * 子午流注时钟自定义View
 * 三层结构：外圈12时辰文字、中圈经络标注、内核心太极☯️三针
 * 子北午南，左卯右酉
 * 时分秒三针，每秒刷新
 * 当前时辰高亮（扇形弧 + 文字放大/变色）
 * 支持点击时辰事件回调
 * 中心为太极阴阳鱼样式
 *
 * 🦞🎨5号设计：宣纸白底色，金色时辰文字，朱砂红高亮
 * 🦞太极中心：米白阳鱼、墨黑阴鱼，鱼眼朱砂红
 */
public class MeridianClockView extends View {

    // 12时辰名称（子丑寅卯辰巳午未申酉戌亥）
    private static final String[] SHICHEN_NAMES = {
            "子", "丑", "寅", "卯", "辰", "巳",
            "午", "未", "申", "酉", "戌", "亥"
    };

    // 🦞🎨5号方案 — 基础配色
    private static final int COLOR_BG = 0xFFF5F0E8;       // 宣纸白
    private static final int COLOR_OUTER_RING = 0xFFE0D5C8; // 外圈色
    private static final int COLOR_TEXT_NORMAL = 0xFFC9A96E; // 金色 - 正常时辰文字
    private static final int COLOR_TEXT_HIGHLIGHT = 0xFFFFF8E7; // 高亮文字（浅金色，在高亮弧上）
    private static final int COLOR_HIGHLIGHT_ARC = 0xFFC43A31; // 朱砂红 - 高亮扇形
    // 🦞选中高亮配色
    private static final int COLOR_SELECTED_ARC = 0x40B0D0FF; // 浅蓝半透明 - 选中时辰背景
    private static final int COLOR_MERIDIAN_TEXT = 0xFF8B7355; // 深棕 - 经络文字
    private static final int COLOR_ARABIC_NUM = 0xFFA09080; // 淡金灰 - 阿拉伯数字（不喧宾夺主）
    private static final int COLOR_HOUR_HAND = 0xFFC43A31;  // 朱砂红 - 时针
    private static final int COLOR_MINUTE_HAND = 0xFFC9A96E; // 深金 - 分针
    private static final int COLOR_SECOND_HAND = 0xFFC9A96E; // 细金线 - 秒针
    private static final int COLOR_CENTER_DOT = 0xFFC43A31;  // 朱砂红 - 中心圆点
    private static final int COLOR_TICK = 0xFFD8D0C0;       // 刻度线
    private static final int COLOR_INNER_CIRCLE = 0xFFE0D5C8; // 内圈

    // 🦞太极配色
    private static final int COLOR_TAIJI_YANG = 0xFFF5F0EB;  // 米白 - 阳鱼
    private static final int COLOR_TAIJI_YIN = 0xFF2D2D2D;   // 墨黑 - 阴鱼
    private static final int COLOR_TAIJI_EYE_YANG = 0xFF000000; // 纯黑 - 阳鱼眼（白鱼黑眼）
    private static final int COLOR_TAIJI_EYE_YIN = 0xFFFFFFFF;  // 纯白 - 阴鱼眼（黑鱼白眼）
    private static final int COLOR_HAND_SHADOW = 0x44000000; // 半透明黑 - 指针阴影

    // 尺寸常量
    private static final float TEXT_SIZE_RATIO = 0.08f;    // 时辰文字大小相对于半径
    private static final float MERIDIAN_TEXT_SIZE_RATIO = 0.035f; // 经络文字大小
    private static final float ARABIC_TEXT_SIZE_RATIO = 0.042f; // 阿拉伯数字大小（比时辰字小一号）
    private static final float HIGHLIGHT_TEXT_SCALE = 1.4f; // 高亮文字放大倍率
    private static final float OUTER_RING_RATIO = 0.85f;    // 外圈位置
    private static final float MERIDIAN_RING_RATIO = 0.68f; // 经络标注圈位置
    private static final float ARABIC_RING_RATIO = 0.655f;  // 阿拉伯数字位置
    private static final float INNER_CIRCLE_RATIO = 0.12f;  // 内圈半径比
    private static final float TAIJI_RATIO = 0.18f;         // 🦞太极半径比
    private static final float HOUR_HAND_RATIO = 0.50f;     // 时针长度比
    private static final float MINUTE_HAND_RATIO = 0.65f;   // 分针长度比
    private static final float SECOND_HAND_RATIO = 0.75f;   // 秒针长度比
    // 🦞太极鱼眼半径比（约为大圆半径的1/7）
    private static final float TAIJI_EYE_RATIO = 0.14f;

    // 🌀后天八卦符号（离☲南/午→艮☶东北→震☳东/卯→巽☴东南→坎☵北/子→坤☷西南→兑☱西/酉→乾☰西北）
    // 顺时针从0°（顶部/午位）开始排列
    private static final String[] BAGUA_SYMBOLS = {
            "☲ 离", "☶ 艮", "☳ 震", "☴ 巽", "☵ 坎", "☷ 坤", "☱ 兑", "☰ 乾"
    };
    private static final float BAGUA_RING_RATIO = 0.38f;     // 🌀八卦符号位置（经络内圈，太极外圈）
    private static final float BAGUA_TEXT_SIZE_RATIO = 0.055f; // 🌀八卦字号（在经络内圈空间较小）
    private static final int COLOR_BAGUA_TEXT = 0xFFC0B090;   // 淡金灰 - 八卦符号

    // 阿拉伯数字1-12
    private static final String[] ARABIC_NUMERALS = {
            "12", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"
    };

    // 绘图组件
    private Paint paintArc;        // 高亮弧
    private Paint paintText;       // 时辰文字
    private Paint paintMeridianText; // 经络文字
    private Paint paintHand;      // 指针（时针/分针/秒针）
    private Paint paintHandTip;   // 指针尖端
    private Paint paintHandShadow; // 🦞指针阴影
    private Paint paintCircle;    // 圆/弧
    private Paint paintCenter;    // 中心圆点
    private Paint paintTick;      // 刻度线
    private Paint paintArabicNum;  // 阿拉伯数字
    private Paint paintTaijiFill; // 🦞太极填充
    private Paint paintBagua;     // 🌀八卦符号

    // 尺寸参数
    private float centerX;
    private float centerY;
    private float radius;
    private float density;

    // 🦞太极路径缓存
    private Path taijiYinPath;
    private Path taijiClipPath;
    private float taijiRadius;

    // 时分秒刷新
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updater = new Runnable() {
        @Override
        public void run() {
            invalidate();
            if (isRunning) {
                handler.postDelayed(this, 1000);
            }
        }
    };

    // 当前时辰索引 (0-11)
    private int currentShichenIndex = -1;
    // 用户选中的时辰索引 (0-11)，-1表示未选中
    private int selectedShichenIndex = -1;
    // 是否已启动刷新
    private boolean isRunning = false;

    // 触摸回调
    private OnShichenClickListener onShichenClickListener;

    public interface OnShichenClickListener {
        void onShichenClick(int index, String shichenName);
    }

    public MeridianClockView(Context context) {
        super(context);
        init();
    }

    public MeridianClockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MeridianClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        density = Resources.getSystem().getDisplayMetrics().density;
        setBackgroundColor(Color.TRANSPARENT);
        setClickable(true);

        paintArc = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintArc.setStyle(Paint.Style.FILL);

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setTypeface(android.graphics.Typeface.SERIF);

        paintMeridianText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintMeridianText.setStyle(Paint.Style.FILL);
        paintMeridianText.setTextAlign(Paint.Align.CENTER);
        paintMeridianText.setTypeface(android.graphics.Typeface.SERIF);
        paintMeridianText.setColor(COLOR_MERIDIAN_TEXT);

        paintArabicNum = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintArabicNum.setStyle(Paint.Style.FILL);
        paintArabicNum.setTextAlign(Paint.Align.CENTER);
        paintArabicNum.setTypeface(android.graphics.Typeface.DEFAULT);
        paintArabicNum.setColor(COLOR_ARABIC_NUM);

        // 🌀八卦符号画笔
        paintBagua = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBagua.setStyle(Paint.Style.FILL);
        paintBagua.setTextAlign(Paint.Align.CENTER);
        paintBagua.setTypeface(android.graphics.Typeface.SERIF);
        paintBagua.setColor(COLOR_BAGUA_TEXT);

        paintHand = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintHand.setStyle(Paint.Style.FILL);
        paintHand.setStrokeCap(Paint.Cap.ROUND);

        paintHandTip = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintHandTip.setStyle(Paint.Style.FILL);
        paintHandTip.setStrokeCap(Paint.Cap.ROUND);

        // 🦞指针阴影：半透明黑色描边，偏移2px
        paintHandShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintHandShadow.setStyle(Paint.Style.STROKE);
        paintHandShadow.setStrokeCap(Paint.Cap.ROUND);
        paintHandShadow.setColor(COLOR_HAND_SHADOW);

        paintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCircle.setStyle(Paint.Style.STROKE);

        paintCenter = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCenter.setStyle(Paint.Style.FILL);

        paintTick = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTick.setStyle(Paint.Style.STROKE);
        paintTick.setStrokeCap(Paint.Cap.ROUND);

        // 🦞太极填充
        paintTaijiFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTaijiFill.setStyle(Paint.Style.FILL);

        updateCurrentShichen();
    }

    public void setOnShichenClickListener(OnShichenClickListener listener) {
        this.onShichenClickListener = listener;
    }

    /**
     * 启动时钟刷新（每秒重绘）
     */
    public void startClock() {
        if (!isRunning) {
            isRunning = true;
            updateCurrentShichen();
            handler.postDelayed(updater, 1000);
        }
    }

    /**
     * 停止时钟刷新
     */
    public void stopClock() {
        isRunning = false;
        handler.removeCallbacks(updater);
    }

    /**
     * 更新当前时辰索引
     */
    private void updateCurrentShichen() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        int newIndex;
        if (hour == 23 || hour == 0) {
            newIndex = 0; // 子时
        } else {
            newIndex = (hour + 1) / 2;
        }

        if (newIndex != currentShichenIndex) {
            currentShichenIndex = newIndex;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
        radius = Math.min(w, h) / 2f - 16 * density;
        // 🦞太极尺寸
        taijiRadius = radius * TAIJI_RATIO;
        // 太极路径随尺寸重建
        buildTaijiPaths();
    }

    /**
     * 🦞构建太极阴阳鱼路径
     * 子北午南：上白（阳鱼）下黑（阴鱼）
     * S形分割使用两条 arcTo 半圆弧
     */
    private void buildTaijiPaths() {
        if (radius <= 0) return;

        float cx = centerX;
        float cy = centerY;
        float R = taijiRadius;

        // 阴鱼（黑色）路径：左侧区域 + 上半部右侧鼓包 + 下半部左侧延伸
        taijiYinPath = new Path();

        // 1. 上半部右鼓包弧（从小圆右上到中心）
        // 上半小圆：圆心(cx, cy-R/2)，半径R/2，取右半弧
        RectF upperRect = new RectF(cx - R / 2, cy - R, cx + R / 2, cy);
        // 从-90°(顶)顺时针180°到+90°(底) — 右半弧
        taijiYinPath.arcTo(upperRect, -90, 180);

        // 2. 下半部左鼓包弧（从中心到左下）
        // 下半小圆：圆心(cx, cy+R/2)，半径R/2，取左半弧
        RectF lowerRect = new RectF(cx - R / 2, cy, cx + R / 2, cy + R);
        // 从-90°(顶)逆时针-180°到-270°(=+90°)(底) — 左半弧
        taijiYinPath.arcTo(lowerRect, -90, -180);

        // 3. 外圈大圆左侧弧（从底回到顶，沿左侧走）
        RectF bigRect = new RectF(cx - R, cy - R, cx + R, cy + R);
        // 从+90°(底)顺时针180°到+270°(顶) — 左侧弧
        taijiYinPath.arcTo(bigRect, 90, 180);

        taijiYinPath.close();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (radius <= 0) return;

        // 更新当前时辰
        updateCurrentShichen();

        // 绘制背景圆形
        drawClockFace(canvas);

        // 绘制当前时辰高亮扇形弧
        drawHighlightArc(canvas);

        // 绘制用户选中时辰的浅蓝半透明高亮背景
        drawSelectedHighlight(canvas);

        // 绘制刻度
        drawTicks(canvas);

        // 绘制外圈时辰文字
        drawShichenText(canvas);

        // 绘制阿拉伯数字钟点（1-12，视觉层级低于时辰文字）
        drawArabicNumerals(canvas);

        // 绘制中圈经络标注
        drawMeridianLabels(canvas);

        // 🌀绘制八卦符号（经络内圈，太极外圈）
        drawBaguaSymbols(canvas);

        // 🦞绘制中心太极阴阳鱼（替换内圈空白）
        drawTaiji(canvas);

        // 绘制三针（太极之上）
        drawClockHands(canvas);
    }

    /**
     * 绘制时钟基底圆
     */
    private void drawClockFace(Canvas canvas) {
        // 外圈主圆
        paintCircle.setColor(COLOR_OUTER_RING);
        paintCircle.setStrokeWidth(2 * density);
        canvas.drawCircle(centerX, centerY, radius, paintCircle);

        // 内圈辅助圆（不再绘制纯圆，由太极替代视觉中心）
        // 保留小圆作为太极外部装饰
        paintCircle.setColor(COLOR_INNER_CIRCLE);
        paintCircle.setStrokeWidth(1.5f * density);
        float innerR = radius * INNER_CIRCLE_RATIO;
        if (innerR > taijiRadius + 4 * density) {
            canvas.drawCircle(centerX, centerY, innerR, paintCircle);
        }
    }

    /**
     * 绘制用户选中的时辰扇形高亮背景（浅蓝半透明）
     */
    private void drawSelectedHighlight(Canvas canvas) {
        if (selectedShichenIndex < 0) return;

        paintArc.setColor(COLOR_SELECTED_ARC);
        // COLOR_SELECTED_ARC已是半透明，#40B0D0FF 约25%透明度，无需额外设置alpha

        // 扇形范围：选中时辰对应的30°扇形区间
        float startAngle = -90f + selectedShichenIndex * 30f - 15f;
        float sweepAngle = 30f;

        float arcRadius = radius * OUTER_RING_RATIO;
        RectF arcRect = new RectF(
                centerX - arcRadius, centerY - arcRadius,
                centerX + arcRadius, centerY + arcRadius);

        canvas.drawArc(arcRect, startAngle, sweepAngle, true, paintArc);
    }

    /**
     * 🌀绘制八卦符号
     * 后天八卦方位，淡金灰小字，在经络内圈均匀分布
     */
    private void drawBaguaSymbols(Canvas canvas) {
        float textRadius = radius * BAGUA_RING_RATIO;
        float textSize = radius * BAGUA_TEXT_SIZE_RATIO;

        paintBagua.setTextSize(textSize);
        paintBagua.setColor(COLOR_BAGUA_TEXT);

        for (int i = 0; i < 8; i++) {
            // 角度：从顶部0°开始，顺时针45°间隔
            double angle = Math.toRadians(-90 + i * 45);
            float x = centerX + (float) (textRadius * Math.cos(angle));
            float y = centerY + (float) (textRadius * Math.sin(angle));

            Paint.FontMetrics fm = paintBagua.getFontMetrics();
            float baseline = y - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(BAGUA_SYMBOLS[i], x, baseline, paintBagua);
        }
    }

    /**
     * 🦞绘制太极阴阳鱼
     */
    private void drawTaiji(Canvas canvas) {
        if (taijiYinPath == null || taijiRadius <= 0) return;

        float cx = centerX;
        float cy = centerY;
        float R = taijiRadius;

        // 1. 画阳鱼（米白）：先画整个大圆，再叠阴鱼
        paintTaijiFill.setColor(COLOR_TAIJI_YANG);
        canvas.drawCircle(cx, cy, R, paintTaijiFill);

        // 2. 画阴鱼（墨黑）
        paintTaijiFill.setColor(COLOR_TAIJI_YIN);
        canvas.drawPath(taijiYinPath, paintTaijiFill);

        // 3. 鱼眼
        float eyeR = R * TAIJI_EYE_RATIO;
        float eyeOffset = R / 4f; // 大圆半径1/4偏移

        // 🎯阳鱼黑眼（白色区域中的黑色眼）：在上半部，右偏移
        paintTaijiFill.setColor(COLOR_TAIJI_EYE_YANG); // 纯黑
        canvas.drawCircle(cx + eyeOffset, cy - eyeOffset, eyeR, paintTaijiFill);

        // 🎯阴鱼白眼（黑色区域中的白色眼）：在下半部，左偏移
        paintTaijiFill.setColor(COLOR_TAIJI_EYE_YIN); // 纯白
        canvas.drawCircle(cx - eyeOffset, cy + eyeOffset, eyeR, paintTaijiFill);

        // 4. 外圈细边框勾勒太极边界
        paintCircle.setColor(COLOR_OUTER_RING);
        paintCircle.setStrokeWidth(1 * density);
        canvas.drawCircle(cx, cy, R, paintCircle);
    }

    /**
     * 绘制当前时辰高亮扇形弧
     */
    private void drawHighlightArc(Canvas canvas) {
        if (currentShichenIndex < 0) return;

        paintArc.setColor(COLOR_HIGHLIGHT_ARC);
        paintArc.setAlpha(200);

        // 子时从0°（12点位置）开始，每个时辰30°
        // 注意：Canvas中0°在3点钟位置，顺时针为正
        // 子时（index 0）应该在12点钟位置 = -90°
        // index 0→-90°, 1→-60°, ... 11→-90+330 = 240°
        float startAngle = -90f + currentShichenIndex * 30f - 15f;
        float sweepAngle = 30f;

        float arcRadius = radius * OUTER_RING_RATIO;
        RectF arcRect = new RectF(
                centerX - arcRadius, centerY - arcRadius,
                centerX + arcRadius, centerY + arcRadius);

        canvas.drawArc(arcRect, startAngle, sweepAngle, true, paintArc);
    }

    /**
     * 绘制外圈12时辰文字
     * 子时在顶部12点方向，顺时针排列
     */
    private void drawShichenText(Canvas canvas) {
        float textRadius = radius * 0.78f;
        float normalTextSize = radius * TEXT_SIZE_RATIO;
        float highlightTextSize = normalTextSize * HIGHLIGHT_TEXT_SCALE;

        for (int i = 0; i < 12; i++) {
            // 角度：子时(i=0)在12点 = -90°, 顺时针+30°
            double angle = Math.toRadians(-90 + i * 30);
            float x = centerX + (float) (textRadius * Math.cos(angle));
            float y = centerY + (float) (textRadius * Math.sin(angle));

            boolean isCurrent = (i == currentShichenIndex);

            paintText.setColor(isCurrent ? COLOR_TEXT_HIGHLIGHT : COLOR_TEXT_NORMAL);
            paintText.setTextSize(isCurrent ? highlightTextSize : normalTextSize);
            paintText.setFakeBoldText(isCurrent);

            // 垂直位置需要补偿文字基线
            Paint.FontMetrics fm = paintText.getFontMetrics();
            float baseline = y - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(SHICHEN_NAMES[i], x, baseline, paintText);
        }
    }

    /**
     * 绘制中圈经络标注（精简为2字名称）
     */
    private void drawMeridianLabels(Canvas canvas) {
        MeridianData[] allData = MeridianData.getAllData();
        float textRadius = radius * 0.58f;
        float textSize = radius * MERIDIAN_TEXT_SIZE_RATIO;

        paintMeridianText.setTextSize(textSize);

        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(-90 + i * 30);
            float x = centerX + (float) (textRadius * Math.cos(angle));
            float y = centerY + (float) (textRadius * Math.sin(angle));

            // 经络名称缩写：取最后两个字（如"胆经"、"肝经"）
            String meridian = allData[i].getMeridian();
            String shortName = meridian.length() >= 2
                    ? meridian.substring(meridian.length() - 2)
                    : meridian;

            Paint.FontMetrics fm = paintMeridianText.getFontMetrics();
            float baseline = y - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(shortName, x, baseline, paintMeridianText);
        }
    }

    /**
     * 绘制阿拉伯数字钟点（1-12）
     * 位置：在经络标注圈与时辰文字之间
     * 视觉层级：弱于时辰文字，颜色淡雅不喧宾夺主
     */
    private void drawArabicNumerals(Canvas canvas) {
        float textRadius = radius * ARABIC_RING_RATIO;
        float textSize = radius * ARABIC_TEXT_SIZE_RATIO;

        paintArabicNum.setTextSize(textSize);
        paintArabicNum.setColor(COLOR_ARABIC_NUM);

        for (int i = 0; i < 12; i++) {
            // 角度：子时(i=0)在12点 = -90°，顺时针+30°
            // 数字映射：子(0)→12, 丑(1)→1, 寅(2)→2, ..., 亥(11)→11
            double angle = Math.toRadians(-90 + i * 30);
            float x = centerX + (float) (textRadius * Math.cos(angle));
            float y = centerY + (float) (textRadius * Math.sin(angle));

            Paint.FontMetrics fm = paintArabicNum.getFontMetrics();
            float baseline = y - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(ARABIC_NUMERALS[i], x, baseline, paintArabicNum);
        }
    }

    /**
     * 绘制刻度线
     */
    private void drawTicks(Canvas canvas) {
        paintTick.setColor(COLOR_TICK);
        paintTick.setStrokeWidth(1.5f * density);

        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(-90 + i * 30);
            float innerX = centerX + (float) (radius * 0.86f * Math.cos(angle));
            float innerY = centerY + (float) (radius * 0.86f * Math.sin(angle));
            float outerX = centerX + (float) (radius * 0.90f * Math.cos(angle));
            float outerY = centerY + (float) (radius * 0.90f * Math.sin(angle));
            canvas.drawLine(innerX, innerY, outerX, outerY, paintTick);
        }
    }

    /**
     * 🦞绘制时分秒三针（加半透明阴影描边）
     */
    private void drawClockHands(Canvas canvas) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);

        // 计算角度 (0° = 12点方向, 顺时针)
        float hourAngle = (hour % 12) * 30f + minute * 0.5f;
        float minuteAngle = minute * 6f + second * 0.1f;
        float secondAngle = second * 6f;

        // 🦞指针阴影偏移（右下方向2px确保双色背景可见）
        float shadowDx = 1.5f * density;
        float shadowDy = 1.5f * density;

        // 画时针 (朱砂红 + 阴影)
        drawHandWithShadow(canvas, hourAngle, radius * HOUR_HAND_RATIO,
                5 * density, COLOR_HOUR_HAND, shadowDx, shadowDy);

        // 画分针 (深金 + 阴影)
        drawHandWithShadow(canvas, minuteAngle, radius * MINUTE_HAND_RATIO,
                3.5f * density, COLOR_MINUTE_HAND, shadowDx, shadowDy);

        // 🦞画秒针 (细金线 + 阴影)
        drawSecondHandWithShadow(canvas, secondAngle, radius * SECOND_HAND_RATIO,
                1.5f * density, COLOR_SECOND_HAND, shadowDx, shadowDy);

        // 🦞太极中心小圆点（朱砂红，盖住三针交叉点）
        paintCenter.setColor(COLOR_CENTER_DOT);
        canvas.drawCircle(centerX, centerY, 2.5f * density, paintCenter);
    }

    /**
     * 🦞绘制单根指针（时针/分针）带阴影
     */
    private void drawHandWithShadow(Canvas canvas, float angleDeg, float length,
                                    float width, int color, float shadowDx, float shadowDy) {
        double angle = Math.toRadians(angleDeg - 90); // -90 因为12点是0°= -90 in math

        float endX = centerX + (float) (length * Math.cos(angle));
        float endY = centerY + (float) (length * Math.sin(angle));

        // 先画阴影（偏移+描边模式）
        paintHandShadow.setStrokeWidth(width);
        canvas.drawLine(centerX + shadowDx, centerY + shadowDy,
                endX + shadowDx, endY + shadowDy, paintHandShadow);

        // 再画指针本体
        paintHand.setColor(color);
        paintHand.setStrokeWidth(width);
        canvas.drawLine(centerX, centerY, endX, endY, paintHand);

        // 指针尖端小圆
        paintHandTip.setColor(color);
        canvas.drawCircle(endX, endY, width * 0.8f, paintHandTip);
    }

    /**
     * 🦞绘制秒针（细金线 + 尾部超出中心）带阴影
     */
    private void drawSecondHandWithShadow(Canvas canvas, float angleDeg, float length,
                                          float width, int color, float shadowDx, float shadowDy) {
        double angle = Math.toRadians(angleDeg - 90);

        float endX = centerX + (float) (length * Math.cos(angle));
        float endY = centerY + (float) (length * Math.sin(angle));
        // 尾部超出中心一小段
        float tailX = centerX - (float) (length * 0.2f * Math.cos(angle));
        float tailY = centerY - (float) (length * 0.2f * Math.sin(angle));

        // 先画阴影（偏移）
        paintHandShadow.setStrokeWidth(width);
        canvas.drawLine(tailX + shadowDx, tailY + shadowDy,
                endX + shadowDx, endY + shadowDy, paintHandShadow);

        // 再画秒针本体
        paintHand.setColor(color);
        paintHand.setStrokeWidth(width);
        canvas.drawLine(tailX, tailY, endX, endY, paintHand);
    }

    /**
     * 绘制单根指针（时针/分针）— 保留供内部备用
     */
    @SuppressWarnings("unused")
    private void drawHand(Canvas canvas, float angleDeg, float length,
                          float width, int color) {
        drawHandWithShadow(canvas, angleDeg, length, width, color, 0, 0);
    }

    /**
     * 绘制秒针 — 保留供内部备用
     */
    @SuppressWarnings("unused")
    private void drawSecondHand(Canvas canvas, float angleDeg, float length,
                                float width, int color) {
        drawSecondHandWithShadow(canvas, angleDeg, length, width, color, 0, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float touchX = event.getX();
        float touchY = event.getY();
        float dx = touchX - centerX;
        float dy = touchY - centerY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        // 判断触摸是否在时钟范围内（扩大范围，覆盖整个时钟面）
        boolean inClockArea = dist <= radius * 0.98f;

        if (action == MotionEvent.ACTION_DOWN) {
            // ★ 关键修复：ACTION_DOWN 必须返回 true 来声明消费触摸事件
            // 否则 Android 不会传递后续的 ACTION_UP 给此 View
            if (inClockArea) {
                return true;
            }
            return super.onTouchEvent(event);
        }

        if (action == MotionEvent.ACTION_UP && inClockArea) {
            // 计算触摸点的角度
            double angle = Math.toDegrees(Math.atan2(dy, dx));
            // 转为时钟角度（0°在12点，顺时针）
            double clockAngle = (angle + 90 + 360) % 360;
            // 转换为时辰索引（每个时辰占30°扇形）
            int index = (int) Math.round(clockAngle / 30) % 12;

            // 记录选中时辰并触发重绘
            selectedShichenIndex = index;
            invalidate();

            if (onShichenClickListener != null) {
                onShichenClickListener.onShichenClick(index, SHICHEN_NAMES[index]);
            }
            performClick();
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startClock();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopClock();
    }
}

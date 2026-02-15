package com.virex.wallpapers.generator

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Procedural Wallpaper Generator
 *
 * Generates AMOLED-optimized wallpapers using procedural algorithms. All generation happens locally
 * without any network requests. Designed to be fast and memory-efficient on mid-range devices.
 */
object ProceduralGenerator {

    // Standard wallpaper size for generation (will be scaled if needed)
    private const val DEFAULT_WIDTH = 1080
    private const val DEFAULT_HEIGHT = 2400

    /**
     * Generate a wallpaper with the specified style and parameters.
     *
     * @param style The visual style to generate
     * @param accentColor The primary accent color
     * @param intensity Generation intensity (0.0 - 1.0), affects density/complexity
     * @param seed Random seed for reproducible results
     * @param width Output width in pixels
     * @param height Output height in pixels
     * @return Generated bitmap
     */
    suspend fun generate(
            style: WallpaperStyle,
            accentColor: Color,
            intensity: Float,
            seed: Long,
            width: Int = DEFAULT_WIDTH,
            height: Int = DEFAULT_HEIGHT
    ): Bitmap =
            withContext(Dispatchers.Default) {
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                val random = Random(seed)

                // Fill with pure black background (AMOLED optimization)
                canvas.drawColor(android.graphics.Color.BLACK)

                when (style) {
                    WallpaperStyle.GEOMETRIC_LINES ->
                            drawGeometricLines(
                                    canvas,
                                    accentColor,
                                    intensity,
                                    random,
                                    width,
                                    height
                            )
                    WallpaperStyle.NEON_GLOW ->
                            drawNeonGlow(canvas, accentColor, intensity, random, width, height)
                    WallpaperStyle.PARTICLE_FIELD ->
                            drawParticleField(canvas, accentColor, intensity, random, width, height)
                    WallpaperStyle.ABSTRACT_WAVES ->
                            drawAbstractWaves(canvas, accentColor, intensity, random, width, height)
                    WallpaperStyle.DARK_GRADIENT ->
                            drawDarkGradient(canvas, accentColor, intensity, random, width, height)
                    WallpaperStyle.CONSTELLATION ->
                            drawConstellation(canvas, accentColor, intensity, random, width, height)
                    WallpaperStyle.MINIMAL_SHAPES ->
                            drawMinimalShapes(canvas, accentColor, intensity, random, width, height)
                    WallpaperStyle.CIRCUIT_BOARD ->
                            drawCircuitBoard(canvas, accentColor, intensity, random, width, height)
                    WallpaperStyle.AURORA ->
                            drawAurora(canvas, accentColor, intensity, random, width, height)
                    WallpaperStyle.FRACTAL_NOISE ->
                            drawFractalNoise(canvas, accentColor, intensity, random, width, height)
                }

                bitmap
            }

    // ==================== GEOMETRIC LINES ====================
    private fun drawGeometricLines(
            canvas: Canvas,
            accentColor: Color,
            intensity: Float,
            random: Random,
            width: Int,
            height: Int
    ) {
        val paint =
                Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                }

        val lineCount = (20 + intensity * 40).toInt()
        val maxStrokeWidth = 2f + intensity * 4f

        for (i in 0 until lineCount) {
            val alpha = (0.3f + random.nextFloat() * 0.7f)
            paint.color = accentColor.copy(alpha = alpha).toArgb()
            paint.strokeWidth = random.nextFloat() * maxStrokeWidth + 1f

            val startX = random.nextFloat() * width
            val startY = random.nextFloat() * height
            val length = height * (0.2f + random.nextFloat() * 0.6f)
            val angle = random.nextFloat() * PI.toFloat() * 2

            val endX = startX + cos(angle) * length
            val endY = startY + sin(angle) * length

            canvas.drawLine(startX, startY, endX, endY, paint)
        }

        // Add some accent highlights
        paint.style = Paint.Style.FILL
        val highlightCount = (5 + intensity * 10).toInt()
        for (i in 0 until highlightCount) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            val radius = 2f + random.nextFloat() * (3f + intensity * 5f)
            paint.color = accentColor.copy(alpha = 0.8f + random.nextFloat() * 0.2f).toArgb()
            canvas.drawCircle(x, y, radius, paint)
        }
    }

    // ==================== NEON GLOW ====================
    private fun drawNeonGlow(
            canvas: Canvas,
            accentColor: Color,
            intensity: Float,
            random: Random,
            width: Int,
            height: Int
    ) {
        val paint = Paint().apply { isAntiAlias = true }

        val glowCount = (3 + intensity * 7).toInt()

        for (i in 0 until glowCount) {
            val centerX = random.nextFloat() * width
            val centerY = random.nextFloat() * height
            val radius = width * (0.15f + random.nextFloat() * 0.35f)

            val gradient =
                    RadialGradient(
                            centerX,
                            centerY,
                            radius,
                            intArrayOf(
                                    accentColor.copy(alpha = 0.4f * intensity).toArgb(),
                                    accentColor.copy(alpha = 0.15f * intensity).toArgb(),
                                    android.graphics.Color.TRANSPARENT
                            ),
                            floatArrayOf(0f, 0.5f, 1f),
                            Shader.TileMode.CLAMP
                    )
            paint.shader = gradient
            canvas.drawCircle(centerX, centerY, radius, paint)
        }

        // Add bright core points
        paint.shader = null
        paint.color = accentColor.toArgb()
        for (i in 0 until glowCount) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            canvas.drawCircle(x, y, 3f + intensity * 4f, paint)
        }
    }

    // ==================== PARTICLE FIELD ====================
    private fun drawParticleField(
            canvas: Canvas,
            accentColor: Color,
            intensity: Float,
            random: Random,
            width: Int,
            height: Int
    ) {
        val paint =
                Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.FILL
                }

        val particleCount = (100 + intensity * 400).toInt()

        for (i in 0 until particleCount) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            val size = 1f + random.nextFloat() * (2f + intensity * 3f)
            val alpha = 0.2f + random.nextFloat() * 0.8f

            paint.color = accentColor.copy(alpha = alpha).toArgb()
            canvas.drawCircle(x, y, size, paint)
        }

        // Connect some particles with lines
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.5f
        val connectionCount = (10 + intensity * 30).toInt()
        for (i in 0 until connectionCount) {
            val x1 = random.nextFloat() * width
            val y1 = random.nextFloat() * height
            val x2 = x1 + (random.nextFloat() - 0.5f) * width * 0.3f
            val y2 = y1 + (random.nextFloat() - 0.5f) * height * 0.3f
            paint.color = accentColor.copy(alpha = 0.1f + random.nextFloat() * 0.2f).toArgb()
            canvas.drawLine(x1, y1, x2, y2, paint)
        }
    }

    // ==================== ABSTRACT WAVES ====================
    private fun drawAbstractWaves(
            canvas: Canvas,
            accentColor: Color,
            intensity: Float,
            random: Random,
            width: Int,
            height: Int
    ) {
        val paint =
                Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    strokeWidth = 2f + intensity * 3f
                }

        val waveCount = (3 + intensity * 5).toInt()

        for (w in 0 until waveCount) {
            val path = Path()
            val baseY = height * (0.2f + random.nextFloat() * 0.6f)
            val amplitude = height * (0.05f + random.nextFloat() * 0.15f)
            val frequency = 2f + random.nextFloat() * 4f
            val phase = random.nextFloat() * PI.toFloat() * 2

            path.moveTo(0f, baseY)
            for (x in 0..width step 5) {
                val y =
                        baseY +
                                sin((x / width.toFloat()) * PI.toFloat() * frequency + phase) *
                                        amplitude
                path.lineTo(x.toFloat(), y.toFloat())
            }

            paint.color = accentColor.copy(alpha = 0.3f + random.nextFloat() * 0.5f).toArgb()
            canvas.drawPath(path, paint)
        }

        // Add glow effect at random points on waves
        paint.style = Paint.Style.FILL
        val glowPoints = (5 + intensity * 10).toInt()
        for (i in 0 until glowPoints) {
            val x = random.nextFloat() * width
            val y = height * 0.3f + random.nextFloat() * height * 0.4f
            val gradient =
                    RadialGradient(
                            x,
                            y,
                            50f + intensity * 50f,
                            intArrayOf(
                                    accentColor.copy(alpha = 0.5f).toArgb(),
                                    android.graphics.Color.TRANSPARENT
                            ),
                            null,
                            Shader.TileMode.CLAMP
                    )
            paint.shader = gradient
            canvas.drawCircle(x, y, 50f + intensity * 50f, paint)
        }
        paint.shader = null
    }

    // ==================== DARK GRADIENT ====================
    private fun drawDarkGradient(
            canvas: Canvas,
            accentColor: Color,
            intensity: Float,
            random: Random,
            width: Int,
            height: Int
    ) {
        val paint = Paint().apply { isAntiAlias = true }

        // Main gradient from corner
        val cornerX = if (random.nextBoolean()) 0f else width.toFloat()
        val cornerY = if (random.nextBoolean()) 0f else height.toFloat()

        val gradient =
                RadialGradient(
                        cornerX,
                        cornerY,
                        sqrt((width * width + height * height).toFloat()),
                        intArrayOf(
                                accentColor.copy(alpha = 0.3f * intensity).toArgb(),
                                accentColor.copy(alpha = 0.1f * intensity).toArgb(),
                                android.graphics.Color.BLACK
                        ),
                        floatArrayOf(0f, 0.3f, 0.7f),
                        Shader.TileMode.CLAMP
                )
        paint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Secondary gradient spot
        val spotX = width * random.nextFloat()
        val spotY = height * random.nextFloat()
        val spotGradient =
                RadialGradient(
                        spotX,
                        spotY,
                        width * 0.4f,
                        intArrayOf(
                                accentColor.copy(alpha = 0.2f * intensity).toArgb(),
                                android.graphics.Color.TRANSPARENT
                        ),
                        null,
                        Shader.TileMode.CLAMP
                )
        paint.shader = spotGradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null
    }

    // ==================== CONSTELLATION ====================
    private fun drawConstellation(
            canvas: Canvas,
            accentColor: Color,
            intensity: Float,
            random: Random,
            width: Int,
            height: Int
    ) {
        val paint = Paint().apply { isAntiAlias = true }

        // Star positions
        data class Star(val x: Float, val y: Float, val brightness: Float, val size: Float)
        val starCount = (30 + intensity * 70).toInt()
        val stars =
                List(starCount) {
                    Star(
                            x = random.nextFloat() * width,
                            y = random.nextFloat() * height,
                            brightness = 0.3f + random.nextFloat() * 0.7f,
                            size = 1f + random.nextFloat() * (2f + intensity * 2f)
                    )
                }

        // Draw connections between nearby stars
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.5f
        val connectionDistance = width * 0.15f
        for (i in stars.indices) {
            for (j in i + 1 until stars.size) {
                val dx = stars[i].x - stars[j].x
                val dy = stars[i].y - stars[j].y
                val distance = sqrt(dx * dx + dy * dy)
                if (distance < connectionDistance && random.nextFloat() < 0.3f) {
                    val alpha = (1 - distance / connectionDistance) * 0.3f
                    paint.color = accentColor.copy(alpha = alpha).toArgb()
                    canvas.drawLine(stars[i].x, stars[i].y, stars[j].x, stars[j].y, paint)
                }
            }
        }

        // Draw stars
        paint.style = Paint.Style.FILL
        for (star in stars) {
            // Glow
            val glowGradient =
                    RadialGradient(
                            star.x,
                            star.y,
                            star.size * 4,
                            intArrayOf(
                                    accentColor.copy(alpha = star.brightness * 0.3f).toArgb(),
                                    android.graphics.Color.TRANSPARENT
                            ),
                            null,
                            Shader.TileMode.CLAMP
                    )
            paint.shader = glowGradient
            canvas.drawCircle(star.x, star.y, star.size * 4, paint)

            // Core
            paint.shader = null
            paint.color = accentColor.copy(alpha = star.brightness).toArgb()
            canvas.drawCircle(star.x, star.y, star.size, paint)
        }
    }

    // ==================== MINIMAL SHAPES ====================
    private fun drawMinimalShapes(
            canvas: Canvas,
            accentColor: Color,
            intensity: Float,
            random: Random,
            width: Int,
            height: Int
    ) {
        val paint = Paint().apply { isAntiAlias = true }

        val shapeCount = (3 + intensity * 5).toInt()

        for (i in 0 until shapeCount) {
            val centerX = random.nextFloat() * width
            val centerY = random.nextFloat() * height
            val size = width * (0.1f + random.nextFloat() * 0.3f)
            val alpha = 0.2f + random.nextFloat() * 0.4f

            when (random.nextInt(4)) {
                0 -> {
                    // Circle outline
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 2f + intensity * 3f
                    paint.color = accentColor.copy(alpha = alpha).toArgb()
                    canvas.drawCircle(centerX, centerY, size, paint)
                }
                1 -> {
                    // Filled circle with gradient
                    paint.style = Paint.Style.FILL
                    val gradient =
                            RadialGradient(
                                    centerX,
                                    centerY,
                                    size,
                                    intArrayOf(
                                            accentColor.copy(alpha = alpha).toArgb(),
                                            android.graphics.Color.TRANSPARENT
                                    ),
                                    null,
                                    Shader.TileMode.CLAMP
                            )
                    paint.shader = gradient
                    canvas.drawCircle(centerX, centerY, size, paint)
                    paint.shader = null
                }
                2 -> {
                    // Triangle
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 2f + intensity * 2f
                    paint.color = accentColor.copy(alpha = alpha).toArgb()
                    val path = Path()
                    path.moveTo(centerX, centerY - size)
                    path.lineTo(centerX - size * 0.866f, centerY + size * 0.5f)
                    path.lineTo(centerX + size * 0.866f, centerY + size * 0.5f)
                    path.close()
                    canvas.drawPath(path, paint)
                }
                3 -> {
                    // Square
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 2f + intensity * 2f
                    paint.color = accentColor.copy(alpha = alpha).toArgb()
                    canvas.drawRect(
                            centerX - size / 2,
                            centerY - size / 2,
                            centerX + size / 2,
                            centerY + size / 2,
                            paint
                    )
                }
            }
        }
    }

    // ==================== CIRCUIT BOARD ====================
    private fun drawCircuitBoard(
            canvas: Canvas,
            accentColor: Color,
            intensity: Float,
            random: Random,
            width: Int,
            height: Int
    ) {
        val paint =
                Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f + intensity
                    strokeCap = Paint.Cap.ROUND
                }

        val gridSize = (40 - intensity * 15).toInt().coerceAtLeast(20)
        val nodeCount = (10 + intensity * 20).toInt()

        // Create nodes
        data class Node(val x: Int, val y: Int)
        val nodes = mutableListOf<Node>()
        repeat(nodeCount) {
            nodes.add(
                    Node(
                            x = (random.nextInt(width / gridSize) * gridSize),
                            y = (random.nextInt(height / gridSize) * gridSize)
                    )
            )
        }

        // Draw traces between nodes
        for (i in nodes.indices) {
            val start = nodes[i]
            val endIndex = (i + 1 + random.nextInt(3.coerceAtMost(nodes.size - 1))) % nodes.size
            val end = nodes[endIndex]

            val alpha = 0.4f + random.nextFloat() * 0.4f
            paint.color = accentColor.copy(alpha = alpha).toArgb()

            val path = Path()
            path.moveTo(start.x.toFloat(), start.y.toFloat())

            // Create right-angle path
            if (random.nextBoolean()) {
                path.lineTo(end.x.toFloat(), start.y.toFloat())
                path.lineTo(end.x.toFloat(), end.y.toFloat())
            } else {
                path.lineTo(start.x.toFloat(), end.y.toFloat())
                path.lineTo(end.x.toFloat(), end.y.toFloat())
            }

            canvas.drawPath(path, paint)
        }

        // Draw nodes
        paint.style = Paint.Style.FILL
        for (node in nodes) {
            val glowGradient =
                    RadialGradient(
                            node.x.toFloat(),
                            node.y.toFloat(),
                            15f + intensity * 10f,
                            intArrayOf(
                                    accentColor.copy(alpha = 0.5f).toArgb(),
                                    android.graphics.Color.TRANSPARENT
                            ),
                            null,
                            Shader.TileMode.CLAMP
                    )
            paint.shader = glowGradient
            canvas.drawCircle(node.x.toFloat(), node.y.toFloat(), 15f + intensity * 10f, paint)

            paint.shader = null
            paint.color = accentColor.toArgb()
            canvas.drawCircle(node.x.toFloat(), node.y.toFloat(), 3f + intensity * 2f, paint)
        }
    }

    // ==================== AURORA ====================
    private fun drawAurora(
            canvas: Canvas,
            accentColor: Color,
            intensity: Float,
            random: Random,
            width: Int,
            height: Int
    ) {
        val paint = Paint().apply { isAntiAlias = true }

        val bandCount = (3 + intensity * 4).toInt()
        val secondaryColor =
                Color(
                        red = (1f - accentColor.red).coerceIn(0f, 1f),
                        green = accentColor.green,
                        blue = (accentColor.blue + 0.3f).coerceIn(0f, 1f),
                        alpha = 1f
                )

        for (band in 0 until bandCount) {
            val baseY = height * (0.2f + band * 0.15f + random.nextFloat() * 0.1f)
            val bandHeight = height * (0.15f + random.nextFloat() * 0.2f)

            val path = Path()
            path.moveTo(0f, baseY + bandHeight)

            // Create wavy top edge
            for (x in 0..width step 20) {
                val waveOffset =
                        sin((x / width.toFloat()) * PI * (3 + random.nextFloat() * 2)).toFloat()
                val y = baseY + waveOffset * bandHeight * 0.3f
                path.lineTo(x.toFloat(), y)
            }

            path.lineTo(width.toFloat(), baseY + bandHeight)
            path.close()

            val gradient =
                    LinearGradient(
                            0f,
                            baseY,
                            0f,
                            baseY + bandHeight,
                            intArrayOf(
                                    if (band % 2 == 0)
                                            accentColor.copy(alpha = 0.3f * intensity).toArgb()
                                    else secondaryColor.copy(alpha = 0.2f * intensity).toArgb(),
                                    android.graphics.Color.TRANSPARENT
                            ),
                            null,
                            Shader.TileMode.CLAMP
                    )
            paint.shader = gradient
            canvas.drawPath(path, paint)
        }
        paint.shader = null

        // Add some stars
        paint.style = Paint.Style.FILL
        val starCount = (20 + intensity * 30).toInt()
        for (i in 0 until starCount) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height * 0.6f
            val size = 0.5f + random.nextFloat() * 1.5f
            paint.color = Color.White.copy(alpha = 0.3f + random.nextFloat() * 0.5f).toArgb()
            canvas.drawCircle(x, y, size, paint)
        }
    }

    // ==================== FRACTAL NOISE ====================
    private fun drawFractalNoise(
            canvas: Canvas,
            accentColor: Color,
            intensity: Float,
            random: Random,
            width: Int,
            height: Int
    ) {
        val paint =
                Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.FILL
                }

        // Generate Perlin-like noise at low resolution for performance
        val scale = 8
        val noiseWidth = width / scale
        val noiseHeight = height / scale

        val noise =
                Array(noiseWidth) { x ->
                    FloatArray(noiseHeight) { y ->
                        // Simple pseudo-noise based on seed
                        val value =
                                sin(x * 0.1 + random.nextDouble()) *
                                        cos(y * 0.1 + random.nextDouble()) *
                                        sin((x + y) * 0.05 + random.nextDouble())
                        ((value + 1) / 2).toFloat().coerceIn(0f, 1f)
                    }
                }

        // Draw noise as colored rectangles
        for (x in 0 until noiseWidth) {
            for (y in 0 until noiseHeight) {
                val noiseValue = noise[x][y] * intensity
                if (noiseValue > 0.3f) {
                    val alpha = (noiseValue - 0.3f) * 1.4f
                    paint.color = accentColor.copy(alpha = alpha.coerceIn(0f, 0.8f)).toArgb()
                    canvas.drawRect(
                            (x * scale).toFloat(),
                            (y * scale).toFloat(),
                            ((x + 1) * scale).toFloat(),
                            ((y + 1) * scale).toFloat(),
                            paint
                    )
                }
            }
        }

        // Add some bright spots
        val spotCount = (5 + intensity * 10).toInt()
        for (i in 0 until spotCount) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            val gradient =
                    RadialGradient(
                            x,
                            y,
                            80f + intensity * 60f,
                            intArrayOf(
                                    accentColor.copy(alpha = 0.4f).toArgb(),
                                    android.graphics.Color.TRANSPARENT
                            ),
                            null,
                            Shader.TileMode.CLAMP
                    )
            paint.shader = gradient
            canvas.drawCircle(x, y, 80f + intensity * 60f, paint)
        }
        paint.shader = null
    }
}

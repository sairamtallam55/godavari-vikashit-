package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.model.Model3DType
import com.example.ui.theme.HeritageGold
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyLight
import com.example.ui.theme.SaffronPrimary
import kotlin.math.*

data class Point3D(val x: Float, val y: Float, val z: Float) {
    fun rotateY(angleRad: Float): Point3D {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Point3D(x * cosA + z * sinA, y, -x * sinA + z * cosA)
    }

    fun rotateX(angleRad: Float): Point3D {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Point3D(x, y * cosA - z * sinA, y * sinA + z * cosA)
    }

    fun project(width: Float, height: Float, fov: Float = 450f, zoom: Float = 1f): Offset {
        val distance = 400f
        val factor = fov / (distance + z) * zoom
        return Offset(width / 2f + x * factor, height / 2f - y * factor)
    }
}

data class Face3D(val indices: List<Int>, val baseColor: Color) {
    fun normal(vertices: List<Point3D>): Point3D {
        if (indices.size < 3) return Point3D(0f, 0f, 1f)
        val v0 = vertices[indices[0]]
        val v1 = vertices[indices[1]]
        val v2 = vertices[indices[2]]
        val ax = v1.x - v0.x
        val ay = v1.y - v0.y
        val az = v1.z - v0.z
        val bx = v2.x - v0.x
        val by = v2.y - v0.y
        val bz = v2.z - v0.z
        val nx = ay * bz - az * by
        val ny = az * bx - ax * bz
        val nz = ax * by - ay * bx
        val len = sqrt(nx * nx + ny * ny + nz * nz).coerceAtLeast(0.0001f)
        return Point3D(nx / len, ny / len, nz / len)
    }

    fun averageZ(vertices: List<Point3D>): Float {
        var sum = 0f
        for (i in indices) {
            sum += vertices[i].z
        }
        return sum / indices.size
    }
}

data class Mesh3D(val vertices: List<Point3D>, val faces: List<Face3D>, val edges: List<Pair<Int, Int>>)

@Composable
fun Interactive3DCanvas(
    modelType: Model3DType,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    accentColor: Color = HeritageGold,
    showControls: Boolean = true
) {
    var rotX by remember(modelType) { mutableFloatStateOf(15f) }
    var rotY by remember(modelType) { mutableFloatStateOf(-30f) }
    var zoom by remember(modelType) { mutableFloatStateOf(1.0f) }
    var panX by remember(modelType) { mutableFloatStateOf(0f) }
    var panY by remember(modelType) { mutableFloatStateOf(0f) }
    var autoRotate by remember { mutableStateOf(true) }
    var wireframeOnly by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "auto_rotate")
    val animatedAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle_anim"
    )

    val currentRotY = if (autoRotate) rotY + animatedAngle else rotY

    val mesh = remember(modelType) { generateHeritageMesh(modelType) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDarkTheme) Color(0xFF091422) else Color(0xFFF8FAFC))
            .pointerInput(modelType) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    autoRotate = false
                    rotY += pan.x * 0.5f
                    rotX = (rotX - pan.y * 0.5f).coerceIn(-80f, 80f)
                    zoom = (zoom * gestureZoom).coerceIn(0.5f, 3.0f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Render subtle ambient radial depth background
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(canvasW / 2f + panX, canvasH / 2f + panY),
                    radius = canvasW * 0.6f
                )
            )

            // Render grid floor in perspective
            drawPerspectiveGrid(canvasW, canvasH, currentRotY, rotX, isDarkTheme)

            // Transform vertices
            val radX = Math.toRadians(rotX.toDouble()).toFloat()
            val radY = Math.toRadians(currentRotY.toDouble()).toFloat()

            val transformed = mesh.vertices.map { v ->
                v.rotateY(radY).rotateX(radX)
            }

            val projected = transformed.map { v ->
                val p = v.project(canvasW, canvasH, 450f, zoom)
                Offset(p.x + panX, p.y + panY)
            }

            // Directional light vector (coming from top-right-front)
            val lightDir = Point3D(0.577f, 0.577f, 0.577f)

            if (!wireframeOnly) {
                // Sort faces by depth (Painter's algorithm: farthest first)
                val sortedFaces = mesh.faces.sortedBy { face -> face.averageZ(transformed) }

                for (face in sortedFaces) {
                    val norm = face.normal(transformed)
                    // Backface culling: skip faces pointing away from camera
                    if (norm.z < -0.1f && face.indices.size >= 3) {
                        continue
                    }

                    // Diffuse lighting calculation (dot product with light direction)
                    val dot = (norm.x * lightDir.x + norm.y * lightDir.y + norm.z * lightDir.z).coerceIn(0.15f, 1.0f)
                    val faceColor = face.baseColor.copy(
                        red = (face.baseColor.red * dot).coerceIn(0f, 1f),
                        green = (face.baseColor.green * dot).coerceIn(0f, 1f),
                        blue = (face.baseColor.blue * dot).coerceIn(0f, 1f),
                        alpha = 0.95f
                    )

                    val path = Path()
                    face.indices.forEachIndexed { i, idx ->
                        val pt = projected[idx]
                        if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                    }
                    path.close()

                    drawPath(path = path, color = faceColor)
                    drawPath(
                        path = path,
                        color = if (isDarkTheme) Color(0x33FFFFFF) else Color(0x22000000),
                        style = Stroke(width = 1f)
                    )
                }
            }

            // Render edges
            val edgeColor = if (wireframeOnly) accentColor else if (isDarkTheme) Color(0x44FFFFFF) else Color(0x33000000)
            val strokeWidth = if (wireframeOnly) 2f else 1f

            for (edge in mesh.edges) {
                val p1 = projected[edge.first]
                val p2 = projected[edge.second]
                drawLine(
                    color = edgeColor,
                    start = p1,
                    end = p2,
                    strokeWidth = strokeWidth
                )
            }
        }

        // Overlay Interactive Controls & Status
        if (showControls) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = { autoRotate = !autoRotate },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = (if (isDarkTheme) NavyDeep else Color.White).copy(alpha = 0.85f)
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (autoRotate) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Auto Rotate",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { wireframeOnly = !wireframeOnly },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = (if (isDarkTheme) NavyDeep else Color.White).copy(alpha = 0.85f)
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (wireframeOnly) Icons.Default.ViewInAr else Icons.Default.GridOn,
                        contentDescription = "Wireframe Toggle",
                        tint = if (wireframeOnly) SaffronPrimary else accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = {
                        rotX = 15f
                        rotY = -30f
                        zoom = 1.0f
                        panX = 0f
                        panY = 0f
                        autoRotate = true
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = (if (isDarkTheme) NavyDeep else Color.White).copy(alpha = 0.85f)
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset View",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Bottom gesture guidance pill
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                color = (if (isDarkTheme) NavyLight else Color.White).copy(alpha = 0.85f),
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = accentColor
                    )
                    Text(
                        text = "Drag to rotate • Pinch to zoom",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.85f) else Color(0xFF334155)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawPerspectiveGrid(
    width: Float,
    height: Float,
    rotY: Float,
    rotX: Float,
    isDarkTheme: Boolean
) {
    val floorY = -90f
    val radX = Math.toRadians(rotX.toDouble()).toFloat()
    val radY = Math.toRadians(rotY.toDouble()).toFloat()
    val gridColor = if (isDarkTheme) Color(0x18FFFFFF) else Color(0x15000000)

    val step = 40f
    val count = 4
    for (i in -count..count) {
        val p1 = Point3D(i * step, floorY, -count * step).rotateY(radY).rotateX(radX).project(width, height)
        val p2 = Point3D(i * step, floorY, count * step).rotateY(radY).rotateX(radX).project(width, height)
        drawLine(gridColor, p1, p2, 1f)

        val q1 = Point3D(-count * step, floorY, i * step).rotateY(radY).rotateX(radX).project(width, height)
        val q2 = Point3D(count * step, floorY, i * step).rotateY(radY).rotateX(radX).project(width, height)
        drawLine(gridColor, q1, q2, 1f)
    }
}

// Generate realistic geometric meshes for each heritage artifact
fun generateHeritageMesh(type: Model3DType): Mesh3D {
    return when (type) {
        Model3DType.SHIVALINGAM -> createShivalingamMesh()
        Model3DType.BRIDGE_ARCH -> createBridgeArchMesh()
        Model3DType.SLUICE_GATE -> createSluiceGateMesh()
        Model3DType.TEMPLE_GOPURAM -> createGopuramMesh()
        Model3DType.HARATHI_LAMP -> createHarathiLampMesh()
        Model3DType.MONOLITHIC_PILLAR -> createPillarMesh()
        Model3DType.HOUSEBOAT -> createHouseboatMesh()
        Model3DType.WOODEN_TOY -> createWoodenToyMesh()
    }
}

private fun createShivalingamMesh(): Mesh3D {
    val vertices = mutableListOf<Point3D>()
    val faces = mutableListOf<Face3D>()
    val edges = mutableListOf<Pair<Int, Int>>()

    val darkBasalt = Color(0xFF263238)
    val goldTrim = Color(0xFFD4AF37)
    val redKumkum = Color(0xFFB71C1C)

    // Base Peetham (Octagonal plinth)
    val baseRadius = 75f
    val baseHeight = 25f
    val segments = 8
    val baseBottomIdx = vertices.size
    for (i in 0 until segments) {
        val angle = (i * 2 * PI / segments).toFloat()
        vertices.add(Point3D(cos(angle) * baseRadius, -70f, sin(angle) * baseRadius))
    }
    val baseTopIdx = vertices.size
    for (i in 0 until segments) {
        val angle = (i * 2 * PI / segments).toFloat()
        vertices.add(Point3D(cos(angle) * (baseRadius - 5f), -70f + baseHeight, sin(angle) * (baseRadius - 5f)))
    }

    for (i in 0 until segments) {
        val next = (i + 1) % segments
        faces.add(Face3D(listOf(baseBottomIdx + i, baseBottomIdx + next, baseTopIdx + next, baseTopIdx + i), darkBasalt))
        edges.add(Pair(baseBottomIdx + i, baseBottomIdx + next))
        edges.add(Pair(baseTopIdx + i, baseTopIdx + next))
        edges.add(Pair(baseBottomIdx + i, baseTopIdx + i))
    }

    // Spout (Gomukha facing +X)
    val spoutStart = vertices.size
    vertices.add(Point3D(baseRadius - 5f, -55f, -15f))
    vertices.add(Point3D(baseRadius + 45f, -55f, -10f))
    vertices.add(Point3D(baseRadius + 45f, -45f, 0f))
    vertices.add(Point3D(baseRadius + 45f, -55f, 10f))
    vertices.add(Point3D(baseRadius - 5f, -55f, 15f))

    edges.add(Pair(spoutStart, spoutStart + 1))
    edges.add(Pair(spoutStart + 1, spoutStart + 2))
    edges.add(Pair(spoutStart + 2, spoutStart + 3))
    edges.add(Pair(spoutStart + 3, spoutStart + 4))

    // Lingam Shaft (Cylinder transitioning into a dome)
    val shaftRadius = 38f
    val shaftSegments = 12
    val rings = 5
    val ringIndices = mutableListOf<Int>()

    for (r in 0..rings) {
        val y = -45f + r * 22f
        val rad = if (r < 3) shaftRadius else shaftRadius * cos(((r - 3) * PI / 4).toFloat())
        val ringStart = vertices.size
        ringIndices.add(ringStart)
        for (s in 0 until shaftSegments) {
            val angle = (s * 2 * PI / shaftSegments).toFloat()
            vertices.add(Point3D(cos(angle) * rad, y, sin(angle) * rad))
        }
    }

    // Top apex vertex
    val apexIdx = vertices.size
    vertices.add(Point3D(0f, 65f, 0f))

    for (r in 0 until rings) {
        val r1 = ringIndices[r]
        val r2 = ringIndices[r + 1]
        for (s in 0 until shaftSegments) {
            val next = (s + 1) % shaftSegments
            val color = if (r == 2) goldTrim else if (r == 3 && s in 4..8) redKumkum else darkBasalt
            faces.add(Face3D(listOf(r1 + s, r1 + next, r2 + next, r2 + s), color))
            edges.add(Pair(r1 + s, r1 + next))
            edges.add(Pair(r1 + s, r2 + s))
        }
    }

    // Top dome fan to apex
    val lastRing = ringIndices.last()
    for (s in 0 until shaftSegments) {
        val next = (s + 1) % shaftSegments
        faces.add(Face3D(listOf(lastRing + s, lastRing + next, apexIdx), darkBasalt))
        edges.add(Pair(lastRing + s, apexIdx))
    }

    return Mesh3D(vertices, faces, edges)
}

private fun createBridgeArchMesh(): Mesh3D {
    val vertices = mutableListOf<Point3D>()
    val faces = mutableListOf<Face3D>()
    val edges = mutableListOf<Pair<Int, Int>>()

    val stoneGray = Color(0xFF64748B)
    val ironRed = Color(0xFF991B1B)
    val ironDark = Color(0xFF1E293B)

    // Left Pier (Stone Cutwater)
    vertices.add(Point3D(-85f, -80f, -25f))
    vertices.add(Point3D(-55f, -80f, -25f))
    vertices.add(Point3D(-55f, -10f, -20f))
    vertices.add(Point3D(-85f, -10f, -20f))
    vertices.add(Point3D(-85f, -80f, 25f))
    vertices.add(Point3D(-55f, -80f, 25f))
    vertices.add(Point3D(-55f, -10f, 20f))
    vertices.add(Point3D(-85f, -10f, 20f))

    faces.add(Face3D(listOf(0, 1, 2, 3), stoneGray))
    faces.add(Face3D(listOf(4, 5, 6, 7), stoneGray))
    faces.add(Face3D(listOf(0, 4, 7, 3), stoneGray))
    faces.add(Face3D(listOf(1, 5, 6, 2), stoneGray))

    // Right Pier
    val rp = vertices.size
    vertices.add(Point3D(55f, -80f, -25f))
    vertices.add(Point3D(85f, -80f, -25f))
    vertices.add(Point3D(85f, -10f, -20f))
    vertices.add(Point3D(55f, -10f, -20f))
    vertices.add(Point3D(55f, -80f, 25f))
    vertices.add(Point3D(85f, -80f, 25f))
    vertices.add(Point3D(85f, -10f, 20f))
    vertices.add(Point3D(55f, -10f, 20f))

    faces.add(Face3D(listOf(rp, rp + 1, rp + 2, rp + 3), stoneGray))
    faces.add(Face3D(listOf(rp + 4, rp + 5, rp + 6, rp + 7), stoneGray))

    // Deck level & Arch Trusses
    val steps = 8
    val bottomNodes = mutableListOf<Int>()
    val topNodes = mutableListOf<Int>()

    for (i in 0..steps) {
        val t = i.toFloat() / steps
        val x = -70f + t * 140f
        val archY = -10f + sin(t * PI.toFloat()) * 45f

        val bIdx = vertices.size
        vertices.add(Point3D(x, -10f, -15f))
        vertices.add(Point3D(x, -10f, 15f))
        bottomNodes.add(bIdx)

        val tIdx = vertices.size
        vertices.add(Point3D(x, archY, -15f))
        vertices.add(Point3D(x, archY, 15f))
        topNodes.add(tIdx)

        edges.add(Pair(bIdx, tIdx))
        edges.add(Pair(bIdx + 1, tIdx + 1))
        edges.add(Pair(bIdx, bIdx + 1))
        edges.add(Pair(tIdx, tIdx + 1))
    }

    for (i in 0 until steps) {
        val b1 = bottomNodes[i]
        val b2 = bottomNodes[i + 1]
        val t1 = topNodes[i]
        val t2 = topNodes[i + 1]

        // Deck rails
        edges.add(Pair(b1, b2))
        edges.add(Pair(b1 + 1, b2 + 1))

        // Arch chord
        edges.add(Pair(t1, t2))
        edges.add(Pair(t1 + 1, t2 + 1))

        // Diagonals (Whipple truss X-bracing)
        edges.add(Pair(b1, t2))
        edges.add(Pair(t1, b2))
        edges.add(Pair(b1 + 1, t2 + 1))
        edges.add(Pair(t1 + 1, b2 + 1))

        faces.add(Face3D(listOf(b1, b2, t2, t1), ironRed.copy(alpha = 0.85f)))
    }

    return Mesh3D(vertices, faces, edges)
}

private fun createSluiceGateMesh(): Mesh3D {
    val vertices = mutableListOf<Point3D>()
    val faces = mutableListOf<Face3D>()
    val edges = mutableListOf<Pair<Int, Int>>()

    val ironTeal = Color(0xFF0F766E)
    val goldBrass = Color(0xFFD4AF37)
    val rustBrown = Color(0xFF78350F)

    // Outer Sluice Frame
    vertices.add(Point3D(-50f, -80f, -10f)) // 0
    vertices.add(Point3D(50f, -80f, -10f))  // 1
    vertices.add(Point3D(50f, 80f, -10f))   // 2
    vertices.add(Point3D(-50f, 80f, -10f))  // 3
    vertices.add(Point3D(-50f, -80f, 10f))  // 4
    vertices.add(Point3D(50f, -80f, 10f))   // 5
    vertices.add(Point3D(50f, 80f, 10f))    // 6
    vertices.add(Point3D(-50f, 80f, 10f))   // 7

    faces.add(Face3D(listOf(0, 1, 2, 3), ironTeal))
    faces.add(Face3D(listOf(4, 5, 6, 7), ironTeal))

    edges.add(Pair(0, 1)); edges.add(Pair(1, 2)); edges.add(Pair(2, 3)); edges.add(Pair(3, 0))
    edges.add(Pair(4, 5)); edges.add(Pair(5, 6)); edges.add(Pair(6, 7)); edges.add(Pair(7, 4))
    edges.add(Pair(0, 4)); edges.add(Pair(1, 5)); edges.add(Pair(2, 6)); edges.add(Pair(3, 7))

    // Central Cog Wheel (Spoked wheel)
    val wheelCenter = Point3D(0f, 20f, 0f)
    val wheelRadius = 38f
    val spokes = 12
    val wheelCenterIdx = vertices.size
    vertices.add(wheelCenter)

    val wheelRimIdx = vertices.size
    for (i in 0 until spokes) {
        val angle = (i * 2 * PI / spokes).toFloat()
        vertices.add(Point3D(cos(angle) * wheelRadius, 20f + sin(angle) * wheelRadius, 0f))
    }

    for (i in 0 until spokes) {
        val next = (i + 1) % spokes
        edges.add(Pair(wheelCenterIdx, wheelRimIdx + i))
        edges.add(Pair(wheelRimIdx + i, wheelRimIdx + next))
        faces.add(Face3D(listOf(wheelCenterIdx, wheelRimIdx + i, wheelRimIdx + next), goldBrass))
    }

    // Vertical Threaded Sluice Shaft
    val shaftIdx = vertices.size
    vertices.add(Point3D(-6f, -70f, 0f))
    vertices.add(Point3D(6f, -70f, 0f))
    vertices.add(Point3D(6f, 70f, 0f))
    vertices.add(Point3D(-6f, 70f, 0f))

    faces.add(Face3D(listOf(shaftIdx, shaftIdx + 1, shaftIdx + 2, shaftIdx + 3), rustBrown))

    return Mesh3D(vertices, faces, edges)
}

private fun createGopuramMesh(): Mesh3D {
    val vertices = mutableListOf<Point3D>()
    val faces = mutableListOf<Face3D>()
    val edges = mutableListOf<Pair<Int, Int>>()

    val sandstone = Color(0xFFD97706)
    val goldShikara = Color(0xFFFBBF24)
    val baseGranite = Color(0xFF475569)

    val tiers = 6
    val tierIndices = mutableListOf<Int>()

    for (t in 0..tiers) {
        val width = 75f * (1f - t * 0.12f)
        val depth = 55f * (1f - t * 0.12f)
        val y = -75f + t * 24f
        val start = vertices.size
        tierIndices.add(start)

        vertices.add(Point3D(-width, y, -depth))
        vertices.add(Point3D(width, y, -depth))
        vertices.add(Point3D(width, y, depth))
        vertices.add(Point3D(-width, y, depth))
    }

    for (t in 0 until tiers) {
        val b = tierIndices[t]
        val top = tierIndices[t + 1]
        val c = if (t == 0) baseGranite else sandstone

        // 4 side faces per tier
        faces.add(Face3D(listOf(b, b + 1, top + 1, top), c))
        faces.add(Face3D(listOf(b + 1, b + 2, top + 2, top + 1), c))
        faces.add(Face3D(listOf(b + 2, b + 3, top + 3, top + 2), c))
        faces.add(Face3D(listOf(b + 3, b, top, top + 3), c))

        // Edges
        edges.add(Pair(b, b + 1)); edges.add(Pair(b + 1, b + 2)); edges.add(Pair(b + 2, b + 3)); edges.add(Pair(b + 3, b))
        edges.add(Pair(b, top)); edges.add(Pair(b + 1, top + 1)); edges.add(Pair(b + 2, top + 2)); edges.add(Pair(b + 3, top + 3))
    }

    // Crowning Kalasam on top
    val apexY = 82f
    val apexIdx = vertices.size
    vertices.add(Point3D(0f, apexY, 0f))
    val topTier = tierIndices.last()

    faces.add(Face3D(listOf(topTier, topTier + 1, apexIdx), goldShikara))
    faces.add(Face3D(listOf(topTier + 1, topTier + 2, apexIdx), goldShikara))
    faces.add(Face3D(listOf(topTier + 2, topTier + 3, apexIdx), goldShikara))
    faces.add(Face3D(listOf(topTier + 3, topTier, apexIdx), goldShikara))

    edges.add(Pair(topTier, apexIdx))
    edges.add(Pair(topTier + 1, apexIdx))
    edges.add(Pair(topTier + 2, apexIdx))
    edges.add(Pair(topTier + 3, apexIdx))

    return Mesh3D(vertices, faces, edges)
}

private fun createHarathiLampMesh(): Mesh3D {
    val vertices = mutableListOf<Point3D>()
    val faces = mutableListOf<Face3D>()
    val edges = mutableListOf<Pair<Int, Int>>()

    val brassGold = Color(0xFFEAB308)
    val flameOrange = Color(0xFFF97316)

    // Base Pedestal
    val baseSeg = 8
    val baseRad = 55f
    val baseIdx = vertices.size
    for (i in 0 until baseSeg) {
        val angle = (i * 2 * PI / baseSeg).toFloat()
        vertices.add(Point3D(cos(angle) * baseRad, -80f, sin(angle) * baseRad))
    }
    for (i in 0 until baseSeg) {
        val next = (i + 1) % baseSeg
        edges.add(Pair(baseIdx + i, baseIdx + next))
    }

    // 4 Tiered Trays
    val trayTiers = 4
    for (t in 0 until trayTiers) {
        val y = -45f + t * 30f
        val rad = 50f - t * 10f
        val trayIdx = vertices.size

        for (i in 0 until baseSeg) {
            val angle = (i * 2 * PI / baseSeg).toFloat()
            vertices.add(Point3D(cos(angle) * rad, y, sin(angle) * rad))
        }

        for (i in 0 until baseSeg) {
            val next = (i + 1) % baseSeg
            edges.add(Pair(trayIdx + i, trayIdx + next))
            faces.add(Face3D(listOf(trayIdx + i, trayIdx + next, trayIdx + next), brassGold))
        }

        // Flame tips on outer rims
        if (t > 0) {
            val flameIdx = vertices.size
            for (i in 0 until 4) {
                val angle = (i * 2 * PI / 4).toFloat()
                vertices.add(Point3D(cos(angle) * rad, y + 10f, sin(angle) * rad))
                edges.add(Pair(trayIdx + i * 2, flameIdx + i))
            }
        }
    }

    // Central Stem
    val stemStart = vertices.size
    vertices.add(Point3D(0f, -80f, 0f))
    vertices.add(Point3D(0f, 65f, 0f))
    edges.add(Pair(stemStart, stemStart + 1))

    // Top Peacock Finial
    val topFlame = vertices.size
    vertices.add(Point3D(0f, 80f, 0f))
    faces.add(Face3D(listOf(stemStart + 1, topFlame, stemStart + 1), flameOrange))

    return Mesh3D(vertices, faces, edges)
}

private fun createPillarMesh(): Mesh3D {
    val vertices = mutableListOf<Point3D>()
    val faces = mutableListOf<Face3D>()
    val edges = mutableListOf<Pair<Int, Int>>()

    val graniteBlack = Color(0xFF1E293B)
    val inscriptionGold = Color(0xFFCA8A04)

    // Square Base
    vertices.add(Point3D(-45f, -80f, -45f))
    vertices.add(Point3D(45f, -80f, -45f))
    vertices.add(Point3D(45f, -40f, -45f))
    vertices.add(Point3D(-45f, -40f, -45f))
    vertices.add(Point3D(-45f, -80f, 45f))
    vertices.add(Point3D(45f, -80f, 45f))
    vertices.add(Point3D(45f, -40f, 45f))
    vertices.add(Point3D(-45f, -40f, 45f))

    faces.add(Face3D(listOf(0, 1, 2, 3), graniteBlack))
    faces.add(Face3D(listOf(4, 5, 6, 7), graniteBlack))

    // Octagonal Middle Shaft
    val seg = 8
    val rad = 28f
    val octBottom = vertices.size
    for (i in 0 until seg) {
        val angle = (i * 2 * PI / seg).toFloat()
        vertices.add(Point3D(cos(angle) * rad, -40f, sin(angle) * rad))
    }
    val octTop = vertices.size
    for (i in 0 until seg) {
        val angle = (i * 2 * PI / seg).toFloat()
        vertices.add(Point3D(cos(angle) * rad, 45f, sin(angle) * rad))
    }

    for (i in 0 until seg) {
        val next = (i + 1) % seg
        val color = if (i == 1 || i == 5) inscriptionGold else graniteBlack
        faces.add(Face3D(listOf(octBottom + i, octBottom + next, octTop + next, octTop + i), color))
        edges.add(Pair(octBottom + i, octTop + i))
        edges.add(Pair(octBottom + i, octBottom + next))
        edges.add(Pair(octTop + i, octTop + next))
    }

    // Capital
    val capIdx = vertices.size
    vertices.add(Point3D(-50f, 65f, -50f))
    vertices.add(Point3D(50f, 65f, -50f))
    vertices.add(Point3D(50f, 65f, 50f))
    vertices.add(Point3D(-50f, 65f, 50f))

    faces.add(Face3D(listOf(capIdx, capIdx + 1, capIdx + 2, capIdx + 3), graniteBlack))

    return Mesh3D(vertices, faces, edges)
}

private fun createHouseboatMesh(): Mesh3D {
    val vertices = mutableListOf<Point3D>()
    val faces = mutableListOf<Face3D>()
    val edges = mutableListOf<Pair<Int, Int>>()

    val teakWood = Color(0xFF78350F)
    val bambooCanopy = Color(0xFFD97706)

    // Boat Hull (Bow to Stern)
    val points = listOf(
        Point3D(-90f, 15f, 0f),   // Bow tip
        Point3D(-50f, -25f, -30f),
        Point3D(0f, -28f, -35f),
        Point3D(50f, -25f, -30f),
        Point3D(85f, 10f, 0f),    // Stern tip
        Point3D(50f, -25f, 30f),
        Point3D(0f, -28f, 35f),
        Point3D(-50f, -25f, 30f)
    )
    vertices.addAll(points)

    faces.add(Face3D(listOf(0, 1, 2, 3, 4), teakWood))
    faces.add(Face3D(listOf(0, 7, 6, 5, 4), teakWood))

    for (i in 0 until points.size) {
        val next = (i + 1) % points.size
        edges.add(Pair(i, next))
    }

    // Arched Canopy Ribs
    val ribs = 5
    for (r in 0 until ribs) {
        val x = -40f + r * 20f
        val ribIdx = vertices.size
        vertices.add(Point3D(x, -5f, -25f))
        vertices.add(Point3D(x, 25f, -18f))
        vertices.add(Point3D(x, 32f, 0f))
        vertices.add(Point3D(x, 25f, 18f))
        vertices.add(Point3D(x, -5f, 25f))

        edges.add(Pair(ribIdx, ribIdx + 1))
        edges.add(Pair(ribIdx + 1, ribIdx + 2))
        edges.add(Pair(ribIdx + 2, ribIdx + 3))
        edges.add(Pair(ribIdx + 3, ribIdx + 4))

        if (r > 0) {
            val prevRib = ribIdx - 5
            faces.add(Face3D(listOf(prevRib + 1, prevRib + 2, ribIdx + 2, ribIdx + 1), bambooCanopy))
            faces.add(Face3D(listOf(prevRib + 2, prevRib + 3, ribIdx + 3, ribIdx + 2), bambooCanopy))
        }
    }

    return Mesh3D(vertices, faces, edges)
}

private fun createWoodenToyMesh(): Mesh3D {
    val vertices = mutableListOf<Point3D>()
    val faces = mutableListOf<Face3D>()
    val edges = mutableListOf<Pair<Int, Int>>()

    val redLacquer = Color(0xFFDC2626)
    val yellowWood = Color(0xFFFACC15)
    val greenLacquer = Color(0xFF16A34A)

    // Base Stand
    vertices.add(Point3D(-40f, -75f, -40f))
    vertices.add(Point3D(40f, -75f, -40f))
    vertices.add(Point3D(40f, -60f, -40f))
    vertices.add(Point3D(-40f, -60f, -40f))
    vertices.add(Point3D(-40f, -75f, 40f))
    vertices.add(Point3D(40f, -75f, 40f))
    vertices.add(Point3D(40f, -60f, 40f))
    vertices.add(Point3D(-40f, -60f, 40f))

    faces.add(Face3D(listOf(0, 1, 2, 3), greenLacquer))

    // Torso (Angavastram)
    val torsoIdx = vertices.size
    vertices.add(Point3D(-25f, -60f, -15f))
    vertices.add(Point3D(25f, -60f, -15f))
    vertices.add(Point3D(30f, 10f, -18f))
    vertices.add(Point3D(-30f, 10f, -18f))
    vertices.add(Point3D(-25f, -60f, 15f))
    vertices.add(Point3D(25f, -60f, 15f))
    vertices.add(Point3D(30f, 10f, 18f))
    vertices.add(Point3D(-30f, 10f, 18f))

    faces.add(Face3D(listOf(torsoIdx, torsoIdx + 1, torsoIdx + 2, torsoIdx + 3), redLacquer))
    faces.add(Face3D(listOf(torsoIdx + 4, torsoIdx + 5, torsoIdx + 6, torsoIdx + 7), redLacquer))

    // Head
    val headIdx = vertices.size
    vertices.add(Point3D(-16f, 10f, -16f))
    vertices.add(Point3D(16f, 10f, -16f))
    vertices.add(Point3D(16f, 42f, -16f))
    vertices.add(Point3D(-16f, 42f, -16f))
    vertices.add(Point3D(-16f, 10f, 16f))
    vertices.add(Point3D(16f, 10f, 16f))
    vertices.add(Point3D(16f, 42f, 16f))
    vertices.add(Point3D(-16f, 42f, 16f))

    faces.add(Face3D(listOf(headIdx, headIdx + 1, headIdx + 2, headIdx + 3), yellowWood))

    // Turban Crown
    val crownIdx = vertices.size
    vertices.add(Point3D(-24f, 42f, -24f))
    vertices.add(Point3D(24f, 42f, -24f))
    vertices.add(Point3D(0f, 75f, 0f))
    vertices.add(Point3D(-24f, 42f, 24f))
    vertices.add(Point3D(24f, 42f, 24f))

    faces.add(Face3D(listOf(crownIdx, crownIdx + 1, crownIdx + 2), redLacquer))
    faces.add(Face3D(listOf(crownIdx + 3, crownIdx + 4, crownIdx + 2), redLacquer))

    // Edges
    edges.add(Pair(0, 1)); edges.add(Pair(1, 2)); edges.add(Pair(2, 3)); edges.add(Pair(3, 0))
    edges.add(Pair(4, 5)); edges.add(Pair(5, 6)); edges.add(Pair(6, 7)); edges.add(Pair(7, 4))
    edges.add(Pair(0, 4)); edges.add(Pair(1, 5)); edges.add(Pair(2, 6)); edges.add(Pair(3, 7))
    edges.add(Pair(headIdx, crownIdx)); edges.add(Pair(crownIdx, crownIdx + 2))

    return Mesh3D(vertices, faces, edges)
}

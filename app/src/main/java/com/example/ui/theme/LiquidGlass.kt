package com.example.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Liquid Glass UI tokens and styling primitives matching the provided reference designs.
 */

// Accent Glow Colors for Liquid Glass
val GlassCyan = Color(0xFF00E5FF)
val GlassViolet = Color(0xFF8B5CF6)
val GlassBlue = Color(0xFF2563EB)
val GlassIndigo = Color(0xFF4F46E5)
val GlassEmerald = Color(0xFF10B981)
val GlassAmber = Color(0xFFF59E0B)
val GlassPink = Color(0xFFEC4899)

// Base glass fills
val GlassDarkSurface = Color(0x660F172A)
val GlassDarkSurfaceHighlight = Color(0x991E293B)
val GlassLightSurface = Color(0x99FFFFFF)
val GlassLightSurfaceHighlight = Color(0xCCF8FAFC)

/**
 * Ambient Liquid Mesh Backdrop that renders deep flowing fluid curves,
 * dynamic glowing orbs, and glass bubble reflections.
 */
@Composable
fun LiquidGlassMeshBackground(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_mesh")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF070B16) else Color(0xFFF1F5F9))
            .drawBehind {
                val width = size.width
                val height = size.height

                if (isDark) {
                    // Deep fluid glow 1: Top-Left Cyan/Blue
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x4400E5FF),
                                Color(0x221D4ED8),
                                Color.Transparent
                            ),
                            center = Offset(width * 0.2f, height * 0.15f),
                            radius = width * 0.7f
                        )
                    )

                    // Deep fluid glow 2: Center-Right Violet/Indigo
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x557C3AED),
                                Color(0x224338CA),
                                Color.Transparent
                            ),
                            center = Offset(width * 0.85f, height * 0.45f),
                            radius = width * 0.8f
                        )
                    )

                    // Deep fluid glow 3: Bottom-Left Amber/Pink glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x33F59E0B),
                                Color(0x22EC4899),
                                Color.Transparent
                            ),
                            center = Offset(width * 0.1f, height * 0.82f),
                            radius = width * 0.65f
                        )
                    )

                    // Ambient Floating Liquid Bubbles with specular highlight
                    drawLiquidBubble(
                        center = Offset(width * 0.12f, height * 0.08f),
                        radius = 18.dp.toPx(),
                        baseColor = Color(0x3300E5FF),
                        highlightColor = Color(0x88FFFFFF)
                    )
                    drawLiquidBubble(
                        center = Offset(width * 0.90f, height * 0.14f),
                        radius = 24.dp.toPx(),
                        baseColor = Color(0x338B5CF6),
                        highlightColor = Color(0x99FFFFFF)
                    )
                    drawLiquidBubble(
                        center = Offset(width * 0.82f, height * 0.70f),
                        radius = 32.dp.toPx(),
                        baseColor = Color(0x337C3AED),
                        highlightColor = Color(0xAAFFFFFF)
                    )
                    drawLiquidBubble(
                        center = Offset(width * 0.08f, height * 0.48f),
                        radius = 14.dp.toPx(),
                        baseColor = Color(0x33F59E0B),
                        highlightColor = Color(0x77FFFFFF)
                    )
                } else {
                    // Light mode fluid mesh
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x3300B4D8),
                                Color(0x153B82F6),
                                Color.Transparent
                            ),
                            center = Offset(width * 0.2f, height * 0.18f),
                            radius = width * 0.7f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x258B5CF6),
                                Color(0x106366F1),
                                Color.Transparent
                            ),
                            center = Offset(width * 0.85f, height * 0.5f),
                            radius = width * 0.8f
                        )
                    )
                }
            }
    ) {
        content()
    }
}

private fun DrawScope.drawLiquidBubble(
    center: Offset,
    radius: Float,
    baseColor: Color,
    highlightColor: Color
) {
    // Bubble body with gradient
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                baseColor.copy(alpha = 0.05f),
                baseColor.copy(alpha = 0.4f),
                baseColor.copy(alpha = 0.7f)
            ),
            center = center,
            radius = radius
        ),
        center = center,
        radius = radius
    )

    // Outer rim stroke
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(
                highlightColor,
                baseColor.copy(alpha = 0.2f),
                highlightColor.copy(alpha = 0.5f)
            ),
            start = Offset(center.x - radius, center.y - radius),
            end = Offset(center.x + radius, center.y + radius)
        ),
        center = center,
        radius = radius,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
    )

    // Specular glass reflection inside
    drawCircle(
        color = highlightColor.copy(alpha = 0.65f),
        center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f),
        radius = radius * 0.28f
    )
}

/**
 * Reusable Liquid Glass Card with translucent background, specular rim highlight,
 * and glowing accents as showcased in the Liquid Glass UI design reference.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    accentGlow: Color = GlassCyan,
    isDark: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val containerColor = if (isDark) {
        Color(0x3D111C35)
    } else {
        Color(0x99FFFFFF)
    }

    val rimBrush = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = 0.55f),
                accentGlow.copy(alpha = 0.45f),
                Color(0x1AFFFFFF),
                accentGlow.copy(alpha = 0.25f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.9f),
                accentGlow.copy(alpha = 0.4f),
                Color(0x40CBD5E1),
                accentGlow.copy(alpha = 0.2f)
            )
        },
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    val baseModifier = modifier
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = if (isDark) {
                    listOf(
                        Color(0x2EFFFFFF),
                        containerColor,
                        containerColor.copy(alpha = 0.85f)
                    )
                } else {
                    listOf(
                        Color(0xEEFFFFFF),
                        Color(0xDDF8FAFC),
                        Color(0xCCF1F5F9)
                    )
                }
            )
        )
        .border(width = 1.2.dp, brush = rimBrush, shape = shape)

    val finalModifier = if (onClick != null) {
        baseModifier.clickable(onClick = onClick)
    } else {
        baseModifier
    }

    Box(modifier = finalModifier) {
        // Specular top highlight reflection
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = if (isDark) 0.6f else 0.8f),
                            accentGlow.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )
        content()
    }
}

/**
 * Numbered Liquid Glass Pill Badge (e.g. "01", "02", "03" from Image 1)
 */
@Composable
fun LiquidGlassPillBadge(
    numberText: String,
    accentColor: Color = GlassCyan,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.25f),
                        accentColor.copy(alpha = 0.2f),
                        Color(0x10000000)
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.6f), accentColor.copy(alpha = 0.5f))
                ),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = numberText,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            color = Color.White
        )
    }
}

data class LiquidGlassNavItem(
    val index: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val badgeCount: Int = 0
)

/**
 * Floating Liquid Glass Dock inspired by Image 2.
 * Translucent frosted capsule with specular top reflection and glowing indicator.
 */
@Composable
fun LiquidGlassDock(
    items: List<LiquidGlassNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(
                            Color(0x551E2E5D),
                            Color(0x990A1124),
                            Color(0xCC070C1B)
                        )
                    } else {
                        listOf(
                            Color(0xEEFFFFFF),
                            Color(0xDDEDF2F7),
                            Color(0xCCD8E2EC)
                        )
                    }
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = if (isDark) {
                        listOf(
                            Color.White.copy(alpha = 0.6f),
                            GlassCyan.copy(alpha = 0.4f),
                            Color.White.copy(alpha = 0.15f),
                            GlassViolet.copy(alpha = 0.35f)
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.95f),
                            GlassCyan.copy(alpha = 0.35f),
                            Color(0x50CBD5E1),
                            GlassViolet.copy(alpha = 0.3f)
                        )
                    }
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        // Specular top highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = if (isDark) 0.7f else 0.9f),
                            GlassCyan.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item.index == selectedIndex

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            GlassCyan.copy(alpha = 0.25f),
                                            GlassViolet.copy(alpha = 0.20f)
                                        )
                                    )
                                ).border(
                                    1.dp,
                                    Brush.linearGradient(
                                        listOf(GlassCyan.copy(alpha = 0.7f), Color.White.copy(alpha = 0.4f))
                                    ),
                                    RoundedCornerShape(24.dp)
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clickable { onItemSelected(item.index) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            androidx.compose.material3.Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) {
                                    GlassCyan
                                } else {
                                    if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            if (item.badgeCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .align(Alignment.TopEnd)
                                        .clip(CircleShape)
                                        .background(GlassCyan),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${item.badgeCount}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }
        }
    }
}

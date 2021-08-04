package tech.bam.livecoding.rating

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.bam.livecoding.R

@Composable
fun Star(size: Dp, modifier: Modifier = Modifier, isActive: Boolean = false, sizePercent: Float) {
    val colorPercent = remember { Animatable(if (isActive) 1f else 0f) }

    LaunchedEffect(isActive) {
        val targetValue = if (isActive) 1f else 0f
        val durationMillis = 100 * if (isActive) (1f - colorPercent.value) else colorPercent.value

        colorPercent.animateTo(
            targetValue = targetValue,
            animationSpec = tween(
                durationMillis = durationMillis.toInt(),
                easing = FastOutLinearInEasing
            )
        )
    }

    val starPathNodes = addPathNodes(stringResource(id = R.string.active_star))
    val dotPathNodes = addPathNodes(stringResource(id = R.string.inactive_star))
    val pathNodes = lerp(dotPathNodes, starPathNodes, sizePercent)
    val color = lerp(Color.Gray, Color.Yellow, colorPercent.value)

    Image(
        painter = rememberVectorPainter(
            defaultWidth = size,
            defaultHeight = size,
            viewportWidth = 48f,
            viewportHeight = 48f,
        ) { _, _ ->
            Path(
                pathData = pathNodes,
                fill = SolidColor(color),
            )
        },
        contentDescription = "Star",
        modifier = modifier
    )
}

@Preview
@Composable
fun StarPreview() {
    Star(sizePercent = 1f, size = 48.dp)
}

fun lerp(a: Float, b: Float, t: Float): Float {
    return a + (b - a) * t
}

fun lerp(fromPathNodes: List<PathNode>, toPathNodes: List<PathNode>, t: Float): List<PathNode> {
    return toPathNodes.mapIndexed { i, to ->
        if (fromPathNodes.size != toPathNodes.size) {
            Log.w(
                "lerp",
                "Path Nodes have different sizes. The lerp function will create/delete nodes."
            )
        }
        val from = if (i < fromPathNodes.size) fromPathNodes[i] else to
        if (from is PathNode.MoveTo && to is PathNode.MoveTo) {
            PathNode.MoveTo(
                lerp(from.x, to.x, t),
                lerp(from.y, to.y, t),
            )
        } else if (from is PathNode.CurveTo && to is PathNode.CurveTo) {
            PathNode.CurveTo(
                lerp(from.x1, to.x1, t),
                lerp(from.y1, to.y1, t),
                lerp(from.x2, to.x2, t),
                lerp(from.y2, to.y2, t),
                lerp(from.x3, to.x3, t),
                lerp(from.y3, to.y3, t),
            )
        } else if (from is PathNode.LineTo && to is PathNode.LineTo) {
            PathNode.LineTo(lerp(from.x, to.x, t), lerp(from.y, to.y, t))
        } else if (from is PathNode.Close && to is PathNode.Close) {
            PathNode.Close
        } else {
            Log.w(
                "lerp",
                "Can note interpolate node from type ${from::class.java} to type ${to::class.java}."
            )
            to
        }
    }
}
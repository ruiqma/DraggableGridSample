package com.example.draggablegridsample.composable

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.draggablegridsample.constants.Constants
import com.example.draggablegridsample.state.rememberDraggableGridState
import kotlinx.coroutines.delay

@Composable
fun DraggableGrid(
    list: List<String>,
    modifier: Modifier = Modifier,
    onListChange: (Int, Int) -> Unit = { _, _ -> },
    itemContent: @Composable (id: String, modifier: Modifier) -> Unit,
) {
    val draggableGridState = rememberDraggableGridState(onListChange)
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    // オーバースクロール処理
    val overScrollPercent = draggableGridState.getOverScrollPercent()
    LaunchedEffect(overScrollPercent) {
        while (overScrollPercent != 0f) {
            if (overScrollPercent < 0 && !draggableGridState.lazyGridState.canScrollBackward) break
            if (0 < overScrollPercent && !draggableGridState.lazyGridState.canScrollForward) break
            val scrollByPx = Constants.SCROLL_BY_PX_MAX * overScrollPercent
            draggableGridState.lazyGridState.scrollBy(scrollByPx)
            draggableGridState.onDrag(null, Offset(0f, scrollByPx))
            delay(Constants.DELAY_INTERVAL)
        }
    }

    LazyVerticalGrid(
        modifier = modifier.pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDragStart = { offset ->
                    // 触覚フィードバック
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    draggableGridState.onDragStart(offset)
                },
                onDrag = { change, dragAmount ->
                    draggableGridState.onDrag(change, dragAmount)
                    change.consume()
                },
                onDragEnd = draggableGridState::onDragEnd,
                onDragCancel = draggableGridState::onDragCancel
            )
        },
        columns = GridCells.Fixed(3),
        state = draggableGridState.lazyGridState,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(
            items = list,
            key = { index, item -> item }, // Compose側でアイテムを特定するためのキー
        ) { index: Int, item: String ->
            val dragging = draggableGridState.draggingIndex == index
            val offset = draggableGridState.dragOffset
            val modifier = if (dragging) {
                Modifier
                    .offset(
                        x = with(density) { offset.x.toDp() },
                        y = with(density) { offset.y.toDp() },
                    )
                    .zIndex(1f) // ドラッグ中のアイテムを前面に表示
            } else {
                Modifier.animateItem() // 入れ替えられる側にアニメーションを設定
            }
            itemContent(item, modifier)
        }
    }
}

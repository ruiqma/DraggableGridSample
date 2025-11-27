package com.example.draggablegridsample.composable

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex

@Composable
fun DraggableGrid(
    list: List<String>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (id: String, modifier: Modifier) -> Unit,
) {
    val lazyGridState = rememberLazyGridState()
    val haptic = LocalHapticFeedback.current

    val density = LocalDensity.current
    var draggingIndex by remember { mutableIntStateOf(0) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    LazyVerticalGrid(
        modifier = modifier.pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDragStart = { offset ->
                    // ドラッグ開始位置のインデックスを取得
                    draggingIndex = lazyGridState.layoutInfo.visibleItemsInfo.find { info ->
                        val rect = Rect(info.offset.toOffset(), info.size.toSize())
                        rect.contains(offset)
                    }?.index ?: -1
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress) // 触覚フィードバック
                },
                onDrag = { change, dragAmount ->
                    dragOffset += dragAmount
                    change.consume() // ドラッグイベントの消費
                },
                onDragEnd = {
                    dragOffset = Offset.Zero
                },
                onDragCancel = {
                    dragOffset = Offset.Zero
                }
            )
        },
        columns = GridCells.Fixed(3),
        state = lazyGridState,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(
            items = list,
            key = { index, item -> item },
        ) { index: Int, item: String ->
            val dragging = draggingIndex == index
            val modifier = if (dragging) {
                Modifier
                    .offset(
                        x = with(density) { dragOffset.x.toDp() },
                        y = with(density) { dragOffset.y.toDp() },
                    )
                    .zIndex(1f) // ドラッグ中のアイテムを前面に表示
            } else {
                Modifier
            }
            itemContent(item, modifier)
        }
    }
}

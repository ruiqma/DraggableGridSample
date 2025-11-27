package com.example.draggablegridsample.state

import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
class DraggableGridState(
    val lazyGridState: LazyGridState,
    private val scope: CoroutineScope,
    private val onListChange: (Int, Int) -> Unit = { _, _ -> },
) {
    var draggingIndex: Int by mutableIntStateOf(-1)
        private set
    var dragOffset: Offset by mutableStateOf(Offset.Zero)
        private set
    var dragPosition: Offset by mutableStateOf(Offset.Zero)
        private set

    private var draggedItem by mutableStateOf<LazyGridItemInfo?>(null)
    private var draggingItemOffsetY: Float = 0f // タップしている点とアイテムの中心点との高さの差

    fun onDragStart(offset: Offset) {
        val itemDragging =
            lazyGridState.layoutInfo.visibleItemsInfo.find { info ->
                val rect = Rect(info.offset.toOffset(), info.size.toSize())
                rect.contains(offset)
            }
        draggingIndex = itemDragging?.index ?: -1
        draggedItem = itemDragging
        dragPosition = offset
        draggingItemOffsetY = draggedItem?.let {
            dragPosition.y - it.offset.y - (it.size.height / 2f)
        } ?: run {
            0f
        }
    }

    fun onDrag(change: PointerInputChange?, dragAmount: Offset) {
        change?.let { dragPosition = it.position }
        dragOffset += dragAmount

        val target = itemIndexUnderDrag()
        // 並び替えが発生しない場合はreturn
        if (target == -1 || target == draggingIndex) return
        // 最上部要素の入れ替えが発生する場合、リストを最上部にスクロールする
        if (draggingIndex == 0 || target == lazyGridState.firstVisibleItemIndex) {
            scope.launch {
                lazyGridState.scrollToItem(
                    lazyGridState.firstVisibleItemIndex,
                    lazyGridState.firstVisibleItemScrollOffset
                )
            }
        }
        onListChange(draggingIndex, target)
        onDragIndexChange(target)
    }

    /**
     * オーバースクロール割合を返す
     * 要素の上端下端が画面の上端下端からどれだけはみ出しているかで割合を算出する
     *
     * @return -1 ~ 1(オーバースクロール中でない場合は0)
     */
    fun getOverScrollPercent(): Float {
        val item = draggedItem ?: return 0f
        val itemHeight = item.size.height.toFloat()
        val itemHeightHalf = itemHeight / 2f

        // 画面上端からどれだけはみ出しているか
        val topOverScroll =
            lazyGridState.layoutInfo.viewportStartOffset - dragPosition.y + (draggingItemOffsetY + itemHeightHalf)
        // 画面下端からどれだけはみ出しているか
        val bottomOverScroll =
            dragPosition.y - (itemHeightHalf + draggingItemOffsetY) + itemHeight - lazyGridState.layoutInfo.viewportEndOffset

        val overScrollPercent = if(0 < topOverScroll) {
            -topOverScroll.coerceAtMost(itemHeight) / itemHeight
        } else if(0 < bottomOverScroll) {
            bottomOverScroll.coerceAtMost(itemHeight) / itemHeight
        } else {
            0f
        }

        return overScrollPercent
    }

    private fun findItemInfo(index: Int): LazyGridItemInfo? {
        return lazyGridState.layoutInfo.visibleItemsInfo.find { info ->
            info.index == index
        }
    }

    private fun itemIndexUnderDrag(): Int {
        return itemUnderDrag()?.index ?: -1
    }

    private fun itemUnderDrag(): LazyGridItemInfo? {
        val draggingCenter = draggingCenter()
        if (draggingCenter == Offset.Zero) return null
        val currentItem =
            lazyGridState.layoutInfo.visibleItemsInfo.find { info ->
                val rect = Rect(info.offset.toOffset(), info.size.toSize())
                rect.contains(draggingCenter)
            }
        return currentItem
    }

    /**
     * ドラッグ中のアイテムの中心位置
     */
    private fun draggingCenter(): Offset {
        val draggingItem = findItemInfo(draggingIndex) ?: return Offset.Zero
        val center = draggingItem.size.center
        return dragOffset + draggingItem.offset.toOffset() + center.toOffset()
    }

    private fun resetIndex() {
        draggingIndex = -1
        dragOffset = Offset.Zero
        draggedItem = null
        draggingItemOffsetY = 0f
    }

    private fun onDragIndexChange(index: Int) {
        val prevItemInfo = findItemInfo(draggingIndex)
        val curItemInfo = findItemInfo(index)

        val prevOffset = prevItemInfo?.offset ?: IntOffset.Zero
        val curOffset = curItemInfo?.offset ?: return

        draggingIndex = index
        dragOffset += (prevOffset - curOffset).toOffset()
    }

    fun onDragEnd() {
        resetIndex()
    }

    fun onDragCancel() {
        resetIndex()
    }
}

@Composable
fun rememberDraggableGridState(
    onListChange: (Int, Int) -> Unit,
): DraggableGridState {
    val lazyGridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    return remember {
        DraggableGridState(
            lazyGridState = lazyGridState,
            scope = scope,
            onListChange = onListChange,
        )
    }
}

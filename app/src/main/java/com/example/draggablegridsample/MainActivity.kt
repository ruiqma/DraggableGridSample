package com.example.draggablegridsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.draggablegridsample.composable.DraggableGrid
import com.example.draggablegridsample.ui.theme.DraggableGridSampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DraggableGridSampleTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) { innerPadding ->
                    val list = remember { List(100) { "$it" }.toMutableList() }
                    Column(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        DraggableGrid(
                            list = list,
                            onListChange = { from, to ->
                                list.add(to, list.removeAt(from))
                            },
                        ) { id: String, modifier: Modifier ->
                            ItemBox(id = id, modifier = modifier)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemBox(id: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(4f / 3f)
            .border(
                width = 2.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Text(
            text = id,
            color = Color.Black,
        )
    }
}

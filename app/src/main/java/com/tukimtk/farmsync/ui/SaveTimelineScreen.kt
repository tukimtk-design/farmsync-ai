package com.tukimtk.farmsync.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.tukimtk.farmsync.sync.SaveNode

@Composable
fun SaveTimelineScreen(nodes: List<SaveNode>) {
    LazyColumn {
        items(nodes) { node ->
            Text("Save: ${node.id} (Parent: ${node.parentId ?: "None"})")
        }
    }
}

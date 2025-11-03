package com.example.posterminale.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.posterminale.presentation.viewmodel.TransactionViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MainScreen(viewModel: TransactionViewModel = hiltViewModel()) {

    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 72.dp, bottom = 16.dp)
    ) {
        Button(onClick = {
            viewModel.send(12345L, "4242********4242", "SHOP_001")
        }) {
            Text("Send Transaction")
        }

        Text(text = state?.toString() ?: "Waiting...")
    }
}

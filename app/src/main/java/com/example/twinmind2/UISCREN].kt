package com.example.twinmind2
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack

import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DinnerDiscussionScreenScaffold(
    list: List<TranscriptMetaData>,
    viewModel: AuthViewModel,
    date: String,
    sessionId: String,
    navController: NavController
) {
    val tabs = listOf("Questions", "Notes", "Transcript")
    var selectedTabIndex by remember { mutableStateOf(1) } // Default to "Notes"

    Scaffold(
        topBar = {
            TopAppBar(

                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { navController.popBackStack()}) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back",tint=Color(0xFF1A73E8))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.Share, contentDescription = "Share",tint=Color(0xFF1A73E8))
                        }
                    }
                },
                colors  = TopAppBarDefaults.topAppBarColors(Color.White)
            )
        },
        containerColor = Color.White,
        contentColor = contentColorFor(Color.White),



    ){
    innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
            .background(Color.White)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            viewModel.tittle,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color=Color(0xFF1A73E8)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "${viewModel.tdate} · ${viewModel.ttime} ",
            color = Color.Gray,
            fontSize = 14.sp
        )
         MainScreen1(list,viewModel ,date,sessionId)

    }
}
}




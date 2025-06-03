package com.example.twinmind2

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwinMindScreen(
    profileImageUrl: String,
    onHelpClick: () -> Unit,
    progress: Int,
    progressGoal: Int,
    viewModel: AuthViewModel,
    navController: NavController
) {
    val tabs = listOf("Memories", "Calendar", "Questions")
    var selectedTab by remember { mutableStateOf(1) } // Default to Calendar tab


    val memories by viewModel.memoryList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "TwinMind",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1976D2)
                        )
                        Spacer(Modifier.width(4.dp))
                        Box(
                            Modifier
                                .background(Color(0xFF1976D2), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "PRO",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.LightGray, CircleShape)
                    )
                }
                ,
                actions = {
                    Text(
                        "Help",
                        color = Color(0xFF1976D2),
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable { onHelpClick() }
                    )


                },

            )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                         ,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 70% width Surface
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFFF5F5F5),
                        modifier = Modifier.weight(0.7f)
                    ) {
                        Row(
                            Modifier
                                .clickable { /* Handle Ask All Memories */ }
                                .padding(vertical = 12.dp, horizontal = 20.dp)
                                .background(Color(0xFFF5F5F5)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.magnifying), // Use your search icon
                                contentDescription = null,
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Ask All Memories", color = Color(0xFF1976D2))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    // 30% width Surface
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF1976D2),
                        modifier = Modifier
                            .weight(0.3f)
                            .clickable {
                                viewModel.startSession("my_meeting")
                                navController.navigate("meeting")
                            }
                    ) {
                        Row(
                            Modifier.padding(vertical = 12.dp, horizontal = 0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.microphone), // Use your mic icon
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Capture", color = Color.White)
                        }
                    }
                }
            }
            },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize().background(Color.White)
        ) {
            // Progress Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation  = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White) // White card background

            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Capture 100 Hours to Unlock Features",
                        color = Color(0xFFFF9800),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Building Your Second Brain",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        AsyncImage(
                            model = "https://static.vecteezy.com/system/resources/previews/000/551/533/original/human-brain-hemispheres-vector-illustration.jpg",
                            placeholder = painterResource(R.drawable.ic_google),

                            contentDescription = "Profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.clip(CircleShape).size(64.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    val progressFraction = progress / progressGoal.toFloat()
                    LinearProgressIndicator(
                        progress = progressFraction.coerceIn(0f, 1f),
                        color = Color(0xFFFF9800),
                        trackColor = Color(0xFFFFE0B2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "$progress / $progressGoal hours",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                contentColor = Color(0xFF1A73E8), // blue for selected tab
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        height = 2.dp,
                        color = Color(0xFF1A73E8)
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab=index},
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) Color(0xFF1A73E8) else Color.Gray
                                )
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0->MemoryListScreen(memories,viewModel,navController)
                1 -> CalendarEventScreen(viewModel)

                else -> Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No content yet", color = Color.Gray)
                }
            }
        }
    }
}



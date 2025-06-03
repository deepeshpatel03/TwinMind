package com.example.twinmind2

import android.view.contentcapture.ContentCaptureSessionId
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.twinmind2.AuthViewModel
import kotlinx.serialization.Serializable

@Composable
fun TranscriptTabView(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {

    Column(modifier = Modifier.fillMaxWidth()) {

        val tabs = listOf("Questions", "Notes", "Transcript")
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
                    onClick = { onTabSelected(index) },
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen1(
    list: List<TranscriptMetaData>,
    viewModel3: AuthViewModel,
    date: String,
    sessionId: String
) {
    var selectedTab by remember { mutableStateOf(2) }
    val meet by viewModel3.meetingSummary.collectAsState()

    var questions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    var inputMessage by remember { mutableStateOf("") }
    var submittedMessage by remember { mutableStateOf<String?>(null) }
    var showEditField by remember { mutableStateOf(true) }
    var webSearchEnabled by remember { mutableStateOf(false) }
    val answer by viewModel3.answer.collectAsState()

    // Fetch questions once
    LaunchedEffect(sessionId) {
        isLoading = true
        viewModel3.fetchAllQuestions(date, sessionId) {
            questions = it
            isLoading = false
        }
    }


    // Bottom Sheet UI
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                submittedMessage = null
                showEditField = true
            },
            sheetState = sheetState,
            containerColor = Color(0xFFF2F2F7)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF2F2F7))
                    .padding(horizontal = 16.dp)
            ) {
                // Close Button
                Row(Modifier.fillMaxWidth(), horizontalArrangement =   Arrangement.SpaceBetween) {
                    IconButton(onClick = {
                        showSheet = false
                        submittedMessage = null
                        showEditField = true
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint =Color(0xFF1A73E8))
                    }



                    if(!showEditField){
                        IconButton(onClick = { showEditField = true
                            inputMessage=submittedMessage.orEmpty()}) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }}
                }

                if (submittedMessage != null && !showEditField) {
                    // Show submitted input as heading
                    Text(
                        text = submittedMessage.orEmpty(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Output Placeholder (mocked, replace with real answer)
                    answer?.let { FirestoreFormattedSummary(it) }


                } else {
                    // Input Field
                    TextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = {
                            Text(
                                text = "Ask anything about all memories...",
                                fontSize = 20.sp,
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(Color(0xFFF2F2F7), RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color(0xFFF2F2F7),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            color = Color.Black,
                            fontSize = 20.sp
                        ),
                        singleLine = true
                    )

                    // Suggestions
                    val suggestions = listOf(
                        "Memorable moments of this month?",
                        "List tasks from all meetings yesterday",
                        "Make a study guide from classes this week"
                    )
                    suggestions.forEach { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    handleSubmit(
                                        suggestion, date, sessionId, viewModel3,
                                        onRefresh = {
                                            questions = it
                                            isLoading = false
                                        },
                                        add=false
                                    )
                                    inputMessage = ""
                                    submittedMessage = suggestion
                                    showEditField = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = suggestion,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                color=Color.Gray
                            )
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Web Search toggle and Send button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Web", tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Web Search", modifier = Modifier.weight(1f))
                        Switch(
                            checked = webSearchEnabled,
                            onCheckedChange = { webSearchEnabled = it }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (inputMessage.isNotBlank()) {
                                    handleSubmit(
                                        inputMessage.trim(), date, sessionId, viewModel3,
                                        onRefresh = {
                                            questions = it
                                            isLoading = false
                                        },
                                        add=true
                                    )
                                    submittedMessage = inputMessage.trim()
                                    inputMessage = ""
                                    showEditField = false
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }
    }

    // Main UI
    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp)
                     ,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                            painter = painterResource(R.drawable.chatting), // Use your search icon
                            contentDescription = null,
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Chat with Transcript", color = Color(0xFF1976D2))
                    }
                }

            }
        },
        containerColor = Color.White,
        contentColor = contentColorFor(Color.White),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            TranscriptTabView(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

            when (selectedTab) {
                0 -> QuestionListScreen(questions, isLoading,{
                    showSheet = true
                    handleSubmit(
                        it.trim(), date, sessionId, viewModel3,
                        onRefresh = {
                            questions = it
                            isLoading = false
                        },
                        add=false
                    )

                    inputMessage=it
                    submittedMessage=it
                    showEditField = false
                })
                1 -> FirestoreFormattedSummary(meet)
                2->TranscriptList(list)
                else -> Text("No content")
            }
        }
    }
}

// Reusable submission handler
private fun handleSubmit(
    message: String,
    date: String,
    sessionId: String,
    viewModel: AuthViewModel,
    onRefresh: (List<String>) -> Unit,
    add:Boolean

    ) {
    if(add){
    viewModel.addQuestion(message, date, sessionId)}
    viewModel.askQuestionFromFullTranscript(message)
    viewModel.fetchAllQuestions(date, sessionId, onRefresh)


}



@Serializable
data class QuestionListScreen(
    val userId :String= " ",
    val date :String = " ",
    val sessionId:String  = " "
)

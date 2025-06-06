package com.example.twinmind2
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.format.DateTimeFormatter

import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingScreen(
    title: String = "untitle",
    viewModel: AuthViewModel,
) {
    var selectedTab by remember { mutableStateOf(0) }
    var isRecording by remember { mutableStateOf(true) }
    val tabs = if(isRecording) {
        listOf("Searches", "Notes", "Transcript")
    }else{
        listOf("Questions", "Notes", "Transcript")
    }
    val sessionId = viewModel.currentSessionId1





    val meet by viewModel.meetingSummary.collectAsState()

    var questions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    var inputMessage by remember { mutableStateOf("") }
    var submittedMessage by remember { mutableStateOf<String?>(null) }
    var showEditField by remember { mutableStateOf(true) }
    var webSearchEnabled by remember { mutableStateOf(false) }
    val answer by viewModel.answer.collectAsState()

    val calendar = Calendar.getInstance()
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val time = timeFormat.format(calendar.time)
    val date = dateFormat.format(calendar.time)
    // Fetch questions once
    LaunchedEffect(sessionId) {
        isLoading = true
        viewModel.fetchAllQuestions(date, sessionId) {
            questions = it
            isLoading = false
        }
    }

    var recordingTime by remember { mutableStateOf("")}

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
                        Icon(Icons.Default.Close, contentDescription = "Close")
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
                                    viewModel.handleSubmit(
                                        suggestion, date, sessionId,
                                        onRefresh = {
                                            questions = it
                                            isLoading = false
                                        },
                                        add = false
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
                                style = MaterialTheme.typography.bodyLarge
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
                                    viewModel.handleSubmit(
                                        inputMessage.trim(), date, sessionId,
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth()) {
                        // Left: Back Icon + Home
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF0056A3)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Home",
                                color = Color(0xFF0056A3),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Center: Timer with red dot


                        // Right: Share icon
                        IconButton(
                            onClick = { /* handle share */ },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.Black
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
        ,
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Outlined button (no background)
                    OutlinedButton(
                        onClick = {   showSheet = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF003366)
                        ),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color(0xFF003366))  // visible outline
                    ) {
                        Icon(painterResource(R.drawable.chatting), contentDescription = null,modifier=Modifier.size(32.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Chat with Transcript")
                    }

                    // Filled button with red background
                    Button(
                        onClick = {  if (!isRecording) {
                            viewModel.startSession("my_meeting")
                            viewModel.starttime= System.currentTimeMillis()
                        } else {
                            viewModel.stopSession()
                        }

                            isRecording = !isRecording
                           },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) Color(0xFFFFEDEE) else Color(0xFFE8F0F9),
                            contentColor = if (isRecording) Color.Red else Color(0xFF003366)
                        ),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.dp, Color.Transparent)
                    ) {
                        if (!isRecording) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint =  Color(0xFF003366),
                                modifier = Modifier.size(28.dp)
                            )
                        } else{
                            Icon(
                                painterResource(R.drawable.stop),
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(28.dp)
                            )
                    }
                    Spacer(Modifier.width(8.dp))
                        Text(if (isRecording) "Stop" else "Start")
                    }
                }
            }
        }
        ,
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                Text(text = title, fontSize = 22.sp, fontWeight = FontWeight.Bold,color=Color(0xFF1A73E8))
                Spacer(Modifier.height(4.dp))
                Text(text = "$date  •  $time", fontSize = 14.sp, color = Color.Gray)
                Spacer(Modifier.height(24.dp))

                // Pill Tabs
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFEFE), RoundedCornerShape(24.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    if (selectedTab == index) Color(0xFFE8F0F9)
                                    else Color(0xFFF5F5F5)
                                )
                                .clickable { selectedTab = index }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = tab,
                                color = if (selectedTab == index) Color(0xFF003366) else Color.Gray,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Mic Icon

                    when (selectedTab) {
                        0 -> {
                            if(isRecording){
                            Icon(
                                painterResource(R.drawable.audio),
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .size(48.dp),
                                tint = Color.Gray
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "TwinMind is listening in the background",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A237E),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Text(
                                text = "Leave it on during your ",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Text(
                                buildAnnotatedString {
                                    append("meeting")
                                    addStyle(
                                        style = SpanStyle(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        start = 0,
                                        end = 7
                                    )
                                },
                                fontSize = 14.sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )}else{
                                QuestionListScreen(questions, isLoading,{
                                    showSheet = true
                                    viewModel.handleSubmit(
                                        it.trim(), date, sessionId,
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
                            }

                        }
                        1 ->  FirestoreFormattedSummary(meet)
                        2 ->  TranscriptList(viewModel.transcriptList)
                    }

            }
        }
    )
}




fun formatTextWithHeadings(input: String): AnnotatedString {
    return buildAnnotatedString {
        val regex = Regex("""\*\*(.*?)\*\*""")  // Matches text between double asterisks

        var currentIndex = 0
        val matches = regex.findAll(input)

        for (match in matches) {

            val start = match.range.first
            val end = match.range.last + 1

            // Add text before heading (normal)
            if (start > currentIndex) {
                append(input.substring(currentIndex, start))
            }

            // Add heading with bold + larger font
            val headingText = match.groupValues[1]
            withStyle(style = SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)) {
                append(headingText)
            }

            currentIndex = end
        }

        // Add remaining text after last match
        if (currentIndex < input.length) {
            append(input.substring(currentIndex))
        }
    }
}


@Composable
fun FirestoreFormattedSummary(rawInput: String) {
    val formattedText = formatTextWithHeadings(rawInput)

    LazyColumn (modifier = Modifier.padding(16.dp)) {
        item{ Text(text = formattedText, style = TextStyle(fontSize = 16.sp, lineHeight = 22.sp))}
    }
}

@Composable
fun TranscriptList(
    transcriptList: List<TranscriptMetaData>,

) {
    LazyColumn(modifier = Modifier.padding(8.dp)) {
        items(transcriptList) { transcript ->
            TranscriptCard(transcript)
        }
    }
}

@Composable
fun TranscriptCard(transcript: TranscriptMetaData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Show only start time
            Text("${transcript.startTime}", fontWeight = FontWeight.Medium,color=Color(0xFF1A73E8))



            Text(
                text = transcript.transcript.ifEmpty { "[Pending transcription]" },
                style = MaterialTheme.typography.bodyMedium,
                color=Color.Gray

            )


        }
    }
}



@Composable
fun QuestionListScreen(
    question:List<String>,
    isLoading:Boolean,
    onClick:(String)->Unit
) {
    val questions = question.toMutableList()
    questions += listOf(
        "What is the main idea of the transcript?",
        "Can you explain the key point discussed?",
        "What important fact is mentioned?",
        "Why is this information significant?"
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            if (questions.isEmpty()) {
                Text("No questions available", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(8.dp)

                ) {
                    items(questions) { question ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(vertical = 4.dp)
                                .clickable { onClick(question) }
                                ,
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(12.dp),

                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                                , modifier = Modifier
                                    .background(Color.White)
                                    .padding(4.dp)
                                    .fillMaxWidth()


                            ){
                            Icon(
                                painter = painterResource(id = R.drawable.chatting),
                                contentDescription = "My icon",
                                tint = Color(0xFF1A73E8),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = question,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )}
                        }
                    }
                }
            }
        }
    }
}

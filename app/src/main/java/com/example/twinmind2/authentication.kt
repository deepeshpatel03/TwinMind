package com.example.twinmind2

import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

import kotlinx.coroutines.launch


import android.Manifest
import android.app.Activity
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.app.ActivityCompat
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull.content
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.HashMap
import java.util.Locale


import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.auth.ktx.auth
import com.stevdzasan.onetap.GoogleUser
import com.stevdzasan.onetap.getUserFromTokenId
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel( ) {


    var userId = MutableStateFlow("")
    private var username = MutableStateFlow("")
  var profilePictureUrl = MutableStateFlow("")
    private val _tokenId = mutableStateOf<String?>(null)


    val tokenId: String? get() = _tokenId.value

    // Public setter
    fun setTokenId(token: String) {
        _tokenId.value = token

    }
    val google = mutableStateOf<GoogleUser?>(null)
  fun getgoogle(){
      google.value= tokenId?.let { getUserFromTokenId(it) }
      username.value=google.value?.fullName.toString()
      profilePictureUrl.value=google.value?.picture.toString()
      firebaseAuthWithGoogle()
  }
    fun firebaseAuthWithGoogle() {
      try{
        val credential = GoogleAuthProvider.getCredential(tokenId, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    userId.value= user?.uid.toString()
                    Log.d("LOG", "signInWithCredential:success. User: ${user?.uid}")
                    call( userId.value)
                } else {
                    Log.w("LOG", "signInWithCredential:failure", task.exception)
                }
            }
      }catch (e:Exception){

      }finally {

      }
    }

    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events

   fun handleSubmit(
        message: String,
        date: String,
        sessionId: String,

        onRefresh: (List<String>) -> Unit,
        add:Boolean

    ) {
        if(add){
           addQuestion(message, date, sessionId)}
         askQuestionFromFullTranscript(message)
         fetchAllQuestions(date, sessionId, onRefresh)


    }
    fun loadEvents(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val calendar = java.util.Calendar.getInstance()
                val now = calendar.timeInMillis
                calendar.add(java.util.Calendar.MONTH, 2)
                val twoMonthsLater = calendar.timeInMillis

                val projection = arrayOf(
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DESCRIPTION,
                    CalendarContract.Events.DTSTART,
                    CalendarContract.Events.DTEND
                )

                val cursor = context.contentResolver.query(
                    CalendarContract.Events.CONTENT_URI,
                    projection,
                    "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?",
                    arrayOf(now.toString(), twoMonthsLater.toString()),
                    "${CalendarContract.Events.DTSTART} ASC"
                )

                val tempList = mutableListOf<CalendarEvent>()
                cursor?.use {
                    while (it.moveToNext()) {
                        val title = it.getString(0) ?: "No Title"
                        val description = it.getString(1)
                        val startTime = it.getLong(2)
                        val endTime = it.getLong(3)

                        tempList.add(
                            CalendarEvent(
                                title = title,
                                description = description,
                                startTime = startTime,
                                endTime = endTime
                            )
                        )
                    }
                }
                _events.value = tempList
            } catch (e: SecurityException) {
                _events.value = emptyList()
            } catch (e: Exception) {
                _events.value = emptyList()
            }
        }
    }
    var tittle: String = ""
        get() = field           // Getter: returns the current value
        set(value) {            // Setter: sets the new value
            field = value
        }
    var ttime: Long =0L
        get() = field           // Getter: returns the current value
        set(value) {            // Setter: sets the new value
            field = value
        }


    var tdate: String = ""
        get() = field           // Getter: returns the current value
        set(value) {            // Setter: sets the new value
            field = value
        }


    //access permission
    private fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val _eventsgroup = MutableStateFlow<Map<String, List<CalendarEvent>>>(emptyMap())
    val eventsgroup: StateFlow<Map<String, List<CalendarEvent>>> = _eventsgroup
  //group event by date
    fun groupEventsByDate(events: List<CalendarEvent>) {
        val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        val grouped = events.groupBy { event ->
            dateFormat.format(Date(event.startTime))
        }
        _eventsgroup.value = grouped
    }


    //get and set event by seesion id for future use

    private val _date = MutableStateFlow("")
    val date: StateFlow<String> get() = _date

    private val _sessionId = MutableStateFlow ("")
    val sessionId: StateFlow<String> get() = _sessionId

    fun setDate(newDate: String) {
        _date.value = newDate
    }

    fun setSessionId(id: String) {
        _sessionId.value = id
    }

    private val _memoryList = MutableStateFlow<List<MemoryMetaData>>(emptyList())
    val memoryList: StateFlow<List<MemoryMetaData>> = _memoryList
    fun formatDate(date: String): String {
        // Convert "2025-05-12" to "Mon, May 12"
        return try {
            val parsed = LocalDate.parse(date)
            parsed.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
        } catch (e: Exception) {
            date
        }
    }

    fun formatTime(timestamp: Long): String {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(DateTimeFormatter.ofPattern("h:mm a"))
    }

    fun formatDuration(startTime: Long, endTime: Long): String {
        val durationMillis = endTime - startTime
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
        val hours = minutes / 60
        val remainingMinutes = minutes % 60

        return if (hours > 0) "${hours}h ${remainingMinutes}m" else "${remainingMinutes}m"
    }


    /* Core state */
    private val firestore = FirebaseFirestore.getInstance()
    private val handler = Handler(Looper.getMainLooper())

    private var isRecording = false
    private var chunkIndex = 0
    private var recordingRunnable: Runnable? = null
    private var mediaRecorder: MediaRecorder? = null

    private var currentChunkStartTime = 0L
    private var currentChunkFile: File? = null

    private var currentDate = ""
    private var currentSessionId = "abc123"

    // Public getter
    val currentSessionId1: String
        get() = currentSessionId
    var starttime=0L
    var end=0L
    /* UI data */
    var transcriptList = mutableStateListOf<TranscriptMetaData>( )
    val transcriptList1: SnapshotStateList<TranscriptMetaData> = transcriptList
    private val _meetingSummary = MutableStateFlow("")
    var meetingSummary: StateFlow<String> = _meetingSummary


    private var pendingWatcherRunning = false

    fun call(userId: String) {

       if (!userId.isNullOrBlank()){
        startPendingSyncWatcher()
        syncFirebaseMemoriesToLocal(context,Firebase.firestore,userId)


    }
    }




    /**
     * Start a new recording session.
     */
    fun startSession(sessionName: String) {
        if (isRecording) stopSession()

        currentDate = getTodayDate()
        currentSessionId = "${sessionName.trim().lowercase(Locale.getDefault())}_${System.currentTimeMillis()}"
        starttime=System.currentTimeMillis()
        listenToUserTranscripts(currentDate, currentSessionId)

        chunkIndex = 0
        isRecording = true
        recordNextChunk()

        if (!pendingWatcherRunning) startPendingSyncWatcher()
    }

    /**
     * Stop the active recording session.
     */
    fun stopSession() {
        if (!isRecording) return
        isRecording = false

        recordingRunnable?.let { handler.removeCallbacks(it) }

        val lastFile = currentChunkFile
        val lastStart = currentChunkStartTime
        stopCurrentRecorder()

        if (lastFile != null && lastFile.exists()) {
            processChunk(lastFile, lastStart)
        }
        end=System.currentTimeMillis()
        generateSessionSummary()
    }

    /* Recording loop */

    private fun recordNextChunk() {
        if (!isRecording) return
        stopCurrentRecorder()

        currentChunkStartTime = System.currentTimeMillis()
        val outputFile = getNextFilePath(currentChunkStartTime)
        currentChunkFile = outputFile

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(outputFile.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            prepare()
            start()
        }

        Log.d("Recorder", "Recording chunk $chunkIndex -> ${outputFile.name}")
        chunkIndex++


        recordingRunnable = Runnable {
            stopCurrentRecorder()
            processChunk(outputFile, currentChunkStartTime)
            recordNextChunk()
        }
        handler.postDelayed(recordingRunnable!!, 30_000)
    }
    private fun getNextFilePath(startTime: Long): File {
        val dir = File(context.getExternalFilesDir(null), "recordings")
        if (!dir.exists()) dir.mkdirs()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date(startTime))
        return File(dir, "chunk_${chunkIndex}_$timeStamp.mp4")
    }

    private fun stopCurrentRecorder() {
        try {
            mediaRecorder?.stop()
        } catch (_: Exception) { /* ignore */ }
        mediaRecorder?.reset()
        mediaRecorder?.release()
        mediaRecorder = null
    }

    /* Process each chunk */

    private fun processChunk(file: File, startTime: Long) {
        val endTime = System.currentTimeMillis()
        val recordId = file.nameWithoutExtension

        val metadata = hashMapOf<String, Any>(
            "fileName" to file.name,
            "startTime" to startTime,
            "endTime" to endTime,
            "date" to currentDate,
            "status" to "pending"
        )

        if (isNetworkAvailable()) {
            transcribeAndSync(file, metadata, recordId)
        } else {
            savePendingLocally(file, metadata)
        }
    }


    private fun transcribeAndSync(
        audioFile: File,
        metadata: HashMap<String, Any>,
        recordId: String,
        onSuccess: (() -> Unit)? = null,
        onFailure: (() -> Unit)? = null
    ) = viewModelScope.launch {
        try {
            val transcript = transcribeWithGeminiFlash(audioFile)
            metadata["status"] = "synced"
            metadata["transcript"] = transcript
            metadata["syncedAt"] = Timestamp.now()

            firestore.chunksRef().document(recordId)
                .set(metadata)
                .addOnSuccessListener { onSuccess?.invoke() }
                .addOnFailureListener { onFailure?.invoke() }

            saveChunkLocally(metadata, transcript, recordId)
        } catch (e: Exception) {
            Log.e("Transcribe", e.message ?: "unknown error")
            metadata["status"] = "failed"
            savePendingLocally(audioFile, metadata)
            onFailure?.invoke()
        }
    }

    /* Offline persistence */

    private fun savePendingLocally(file: File, meta: Map<String, Any>) {
        val pendingDir = File(context.getExternalFilesDir(null), "pending")
        if (!pendingDir.exists()) pendingDir.mkdirs()

        val metadataFile = File(pendingDir, "${file.nameWithoutExtension}.json")
        metadataFile.writeText(JSONObject(meta).toString())

        file.copyTo(File(pendingDir, file.name), overwrite = true)
        Log.d("Recorder", "Saved ${file.name} as pending")
    }

    private fun saveChunkLocally(meta: Map<String, Any>, transcript: String, recordId: String) {
        val chunkDir = sessionDir1().resolve(recordId).apply { mkdirs() }

        File(chunkDir, "metadata.json").writeText(JSONObject(meta).toString())
        File(chunkDir, "transcript.txt").writeText(transcript)
    }
    fun readLocalTranscripts(date: String, sessionId: String): List<TranscriptMetaData> {
        val sessionDir = File(context.getExternalFilesDir(null), "recordings")
            .resolve(userId.value)
            .resolve(date)
            .resolve(sessionId)

        if (!sessionDir.exists()) return emptyList()

        val transcripts = mutableListOf<TranscriptMetaData>()

        sessionDir.listFiles()?.forEach { recordDir ->
            if (recordDir.isDirectory) {
                try {
                    val metaFile = File(recordDir, "metadata.json")
                    val transcriptFile = File(recordDir, "transcript.txt")

                    if (metaFile.exists() && transcriptFile.exists()) {
                        val meta = JSONObject(metaFile.readText())
                        val transcript = transcriptFile.readText()

                        transcripts.add(
                            TranscriptMetaData(
                                fileName = meta.optString("fileName"),
                                startTime = meta.optString("startTime"),
                                endTime = meta.optString("endTime"),
                                date = meta.optString("date"),
                                transcript = transcript,
                                status = meta.optString("status")
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e("LocalTranscript", "Error reading file: ${e.message}")
                }
            }
        }

        return transcripts
    }



    private fun sessionDir1(): File {
        val baseDir = File(context.getExternalFilesDir(null), "recordings")
        return File(baseDir, "${userId}_${currentDate}_${currentSessionId}")
    }


    /* Generate session summary */

    private fun generateSessionSummary() = viewModelScope.launch {
        listenToUserTranscripts(currentDate,currentSessionId)

        val transcripts =transcriptList.joinToString("\n\n") { it.transcript }
        Log.d("transcripts",transcripts )
        val summary = transcripts


        val prompt = content {
            text(
                """
                The following are transcript segments from a meeting:

                $summary

                Please generate a clear, concise, and structured summary including key points, decisions made, and any action items.
            """.trimIndent()
            )
        }

        val response = model.generateContent(prompt)
        val summaryText = response.text?.takeIf { it.isNotBlank() } ?: "[No summary generated]"

// Now generate a title from the summary
        val titlePrompt = "Generate a short, meaningful title based on this summary in single line no other text:\n$summaryText"
        val titleResponse = model.generateContent(titlePrompt)
        val title = titleResponse.text?.takeIf { it.isNotBlank() } ?: "[No title generated]"

        saveMemoryMetadata(
            context = context,
            date = currentDate,
            sessionId = currentSessionId,
            title = title,
            firestore = Firebase.firestore,
            userId = userId.value
        )

        Log.d("recordervf", "Title generated: $title")


        _meetingSummary.value = summaryText
        val summaryFile = File(sessionDir1(), "summary.txt")
        summaryFile.parentFile?.mkdirs() // Ensures parent folders exist
        summaryFile.writeText(summaryText)


        firestore.sessionDoc().set(
            mapOf(
                "summary" to summaryText,
                "sessionId" to currentSessionId,
                "date" to currentDate
            )
        )
    }

    fun listenToUserTranscripts( date: String, sessionId: String) {
        if (date.isBlank() || sessionId.isBlank()) return

        if (isNetworkAvailable()) {
            firestore.collection("users").document(userId.value)
                .collection(date)
                .document(sessionId)
                .collection("transcripts")
                .orderBy("startTime", Query.Direction.DESCENDING)
                .addSnapshotListener { snap, e ->
                    if (e != null || snap == null) return@addSnapshotListener

                    transcriptList.clear()
                    snap.documents
                        .filter { it.id != "summary" }
                        .forEach { doc ->
                            val data = doc.data ?: return@forEach
                            transcriptList += TranscriptMetaData(
                                fileName = data["fileName"]?.toString() ?: "",
                                startTime = formatTime(data["startTime"]),
                                endTime = formatTime(data["endTime"]),
                                date = data["date"]?.toString() ?: "",
                                transcript = data["transcript"]?.toString() ?: "",
                                status = data["status"]?.toString() ?: ""
                            )
                        }
                }
        } else {
            // No network, read from local folder
            val localTranscripts = readLocalTranscripts(date, sessionId)
            transcriptList.clear()
            transcriptList.addAll(localTranscripts)
        }
    }

    private fun startPendingSyncWatcher() {
        if (pendingWatcherRunning) return
        pendingWatcherRunning = true

        viewModelScope.launch {
            while (true) {
                if (isNetworkAvailable()) {
                    syncPendingChunks()
                }
                delay(60_000)
            }
        }
    }

    private fun syncPendingChunks() {
        val pendingDir = File(context.getExternalFilesDir(null), "pending")
        if (!pendingDir.exists()) return

        val jsonFiles = pendingDir.listFiles { _, name -> name.endsWith(".json") } ?: return

        for (metaFile in jsonFiles) {
            try {
                val json = JSONObject(metaFile.readText())
                val fileName = json.getString("fileName")
                val audioFile = File(pendingDir, fileName)

                if (audioFile.exists()) {
                    val metadata = hashMapOf<String, Any>().apply {
                        json.keys().forEach { key -> put(key, json.get(key)!!) }
                    }
                    val recordId = audioFile.nameWithoutExtension
                    transcribeAndSync(audioFile, metadata,recordId,
                        onSuccess = {
                            metaFile.delete()
                            audioFile.delete()
                            Log.d("Recorder", "$fileName synced and deleted from pending")
                        },
                        onFailure = {
                            Log.e("Recorder", "Failed to sync $fileName, keeping for retry")
                        }
                    )
                } else {
                    Log.e("Sync", "Audio file not found: $fileName")
                }
            } catch (e: Exception) {
                Log.e("Sync", "Failed to parse pending metadata: ${e.message}")
            }finally {
                if(!isRecording){
                    generateSessionSummary()
                }

            }
        }
    }

    /* Gemini transcription */

    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.0-flash")
    }

    private suspend fun transcribeWithGeminiFlash(audioFile: File): String = withContext(Dispatchers.IO) {
        val prompt = content {
            inlineData(audioFile.readBytes(), "audio/mp4")
            text("Transcribe what's said in this audio recording in English caption")
        }
        model.generateContent(prompt).text ?: "[No transcription]"
    }

    /* Utils */

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun formatTime(value: Any?): String =
        value?.toString()?.toLongOrNull()?.let {
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(it))
        } ?: ""

    private fun getTodayDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    /* File path helpers */

    private fun localChunkFile(start: Long): File {
        val dir = sessionDir()
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date(start))
        return File(dir, "chunk_${chunkIndex}_$ts.mp4")
    }

    private fun sessionDir(): File =
        File(context.getExternalFilesDir(null), "$userId/$currentDate/sessions/$currentSessionId")

    private fun pendingDir(recordId: String): File =
        sessionDir().resolve(recordId).resolve("pending")

    private fun pendingRootDir(): File =
        File(context.getExternalFilesDir(null), "$userId/$currentDate/sessions/$currentSessionId")

    /* Firestore references */

    private fun FirebaseFirestore.chunksRef() =
        collection("users").document(userId.value)
            .collection(currentDate)
            .document(currentSessionId)
            .collection("transcripts")

    private fun FirebaseFirestore.sessionDoc() =
        collection("users").document(userId.value)
            .collection(currentDate)
            .document(currentSessionId)


    fun saveMemoryMetadata(
        context: Context,
        date: String,
        sessionId: String,
        title: String,
        firestore: FirebaseFirestore,
        userId: String
    ) {
        try {
            val memoryMeta = MemoryMetaData(date, sessionId, starttime ,end,title)

//            // 1. Save to local storage
            val fileName = "${date}_$sessionId.json"
            val file = File(context.getExternalFilesDir(null), "memories/$fileName")
            file.parentFile?.mkdirs()
            file.writeText(Json.encodeToString(memoryMeta))

            Log.d("fMemory ghfhf",title)
            // 2. Try saving to Firebase
            firestore.collection("user").document(userId)
                .collection("memories")
                .document("$date-$sessionId")
                .set(memoryMeta)
                .addOnSuccessListener {
                    Log.d("Memory", "Uploaded to Firebase: $fileName")
                }
                .addOnFailureListener {
                    Log.w("Memory", "Failed to upload to Firebase: $fileName")
                }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    fun loadAllLocalMemories() {


        val memoryDir = File( context.getExternalFilesDir(null), "memories")
        if (!memoryDir.exists() || !memoryDir.isDirectory) {
            // Directory doesn't exist, no memories to load
            _memoryList.value = emptyList()
            return
        }
        val memories = mutableListOf<MemoryMetaData>()

        memoryDir.listFiles()?.forEach { file ->
            if (file.extension == "json") {
                try {
                    val json = file.readText()
                    val memory = Json.decodeFromString<MemoryMetaData>(json)
                    memories += memory
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        _memoryList.value = memories
    }
    fun syncFirebaseMemoriesToLocal(
        context: Context,
        firestore: FirebaseFirestore,
        userId: String
    ) {
        firestore.collection("user").document(userId)
            .collection("memories")
            .get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.forEach { doc ->
                    try {
                        val memory = doc.toObject(MemoryMetaData::class.java) ?: return@forEach
                        val fileName = "${memory.date}_${memory.sessionId}.json"
                        val file = File(context.getExternalFilesDir(null), "memories/$fileName")

                        if (!file.exists()) {
                            file.parentFile?.mkdirs()
                            file.writeText(Json.encodeToString(MemoryMetaData.serializer(), memory))
                            Log.d("MemorySync", "Saved locally: $fileName")
                        } else {
                            Log.d("MemorySync", "File already exists: $fileName")
                        }
                    } catch (e: Exception) {
                        Log.e("MemorySync", "Error saving memory: ${doc.id}", e)
                    }finally {
                        loadAllLocalMemories()
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("MemorySync", "Failed to fetch Firebase memories", e)
            }
    }


    fun acesssummary(sessionId: String,date: String){
        val docRef = firestore.collection("users")
            .document(userId.value)
            .collection(date)
            .document(sessionId)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val meetingData = document.data
                    val summary = meetingData?.get("summary") as? String ?: "No summary available"
                    _meetingSummary.value = summary
                } else {
                    // Document does not exist
                }
            }
            .addOnFailureListener { exception ->
                // Handle error
                Log.d("summary",exception.toString())
            }

    }


    private val _answer = MutableStateFlow<String?>(null)
    val answer: StateFlow<String?> = _answer

    fun askQuestionFromFullTranscript( question: String) {
        val combinedTranscript = getAllTranscriptText(transcriptList)
        Log.d("SUMMARY", combinedTranscript)
        viewModelScope.launch {
            try {
                if(isNetworkAvailable()){
                val response = model.generateContent("$combinedTranscript\n\n$question")
                val summary = response.text?.takeIf { it.isNotBlank() } ?: "[No summary generated]"
                _answer.value = summary
            }else{
                    _answer.value="no internet"
            }
        }catch (e: Exception){
            _answer.value=e.toString()
        }
        }
    }

    private fun getAllTranscriptText(list: SnapshotStateList<TranscriptMetaData>): String {
        return list.joinToString("\n") { it.transcript }
    }


    fun addQuestion(   question: String,date: String, sessionId: String) {
        val questionData = mapOf(
            "question" to question,
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("users")
            .document(userId.value)
            .collection(date)
            .document(sessionId)
            .collection("questions")
            .add(questionData)
            .addOnSuccessListener {
                Log.d("addQuestion", "Question added successfully $date $sessionId")

            }
            .addOnFailureListener { e ->
                Log.e("addQuestion", "Error adding question", e)
            }
    }


    fun fetchAllQuestions(date: String, sessionId: String, onResult: (List<String>) -> Unit) {
        firestore.collection("users")
            .document(userId.value)
            .collection(date)
            .document(sessionId)
            .collection("questions")
            .orderBy("timestamp") // Optional: get questions in order
            .get()
            .addOnSuccessListener { querySnapshot ->
                val questions = querySnapshot.documents.mapNotNull {
                    it.getString("question")
                }
                onResult(questions)
            }
            .addOnFailureListener { e ->
                Log.e("fetchQuestions", "Error fetching questions", e)
                onResult(emptyList())
            }
    }








}



@Serializable
data class MemoryMetaData(
    val date: String = "",
    val sessionId: String = "",
    val startTime: Long=0L,
    val endtime:Long=0L,
    val title: String = ""
)
data class TranscriptMetaData(
    val fileName: String,
    val startTime: String,
    val endTime: String,
    val date: String,
    val transcript: String,
    val status: String
)
package com.example.twinmind2

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun AppNavigation(viewModel: AuthViewModel) {
    val navController = rememberNavController()
    val user = Firebase.auth.currentUser
    val photoUrl = user?.photoUrl
    val date by viewModel .date.collectAsState()
    val sessionId by viewModel .sessionId.collectAsState()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            TwinMindScreen(
                profileImageUrl = photoUrl.toString(),
                onHelpClick = { /* Handle help click */ },
                progress = 159,
                progressGoal = 100,
                viewModel = viewModel,
                navController = navController
            )

        }
        composable("login") {
            TwinMindLoginScreen(viewModel = viewModel,navController)
        }

        composable(route = "page") {
            LaunchedEffect(Unit) {
                viewModel .listenToUserTranscripts(date,sessionId)
                viewModel .acesssummary(sessionId,date)
            }
            val list = viewModel.transcriptList1
            DinnerDiscussionScreenScaffold(list,viewModel ,date,sessionId,navController)
        }

        composable("meeting") {
            MeetingScreen(viewModel = viewModel)
        }
    }
}
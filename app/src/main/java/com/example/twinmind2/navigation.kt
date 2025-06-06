package com.example.twinmind2

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AppNavigation(  ) {
    val navController = rememberNavController()


    val authViewModel: AuthViewModel = hiltViewModel()
    val date by authViewModel .date.collectAsState()
    val sessionId by authViewModel .sessionId.collectAsState()

    NavHost(navController = navController, startDestination = "login") {
        composable("home") {
            authViewModel.getgoogle()
            TwinMindScreen(
                profileImageUrl = "",
                onHelpClick = {  },
                progress = 159,
                progressGoal = 100,
                viewModel = authViewModel,
                navController = navController
            )

        }
        composable("login") {
            TwinMindLoginScreen(){
                authViewModel.setTokenId(it)
                navController.navigate("home")
            }
        }

        composable(route = "page") {
            LaunchedEffect(Unit) {
                authViewModel .listenToUserTranscripts(date,sessionId)
                authViewModel .acesssummary(sessionId,date)
            }
            val list = authViewModel.transcriptList1
            DinnerDiscussionScreenScaffold(list,authViewModel ,date,sessionId,navController)
        }

        composable("meeting") {
            MeetingScreen(viewModel = authViewModel)
        }
    }
}
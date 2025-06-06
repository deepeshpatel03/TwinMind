package com.example.twinmind2

import android.app.Activity.RESULT_OK

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsActions.Dismiss
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.stevdzasan.onetap.GoogleButtonTheme
import com.stevdzasan.onetap.GoogleUser
import com.stevdzasan.onetap.OneTapGoogleButton
import com.stevdzasan.onetap.OneTapSignInWithGoogle
import com.stevdzasan.onetap.getUserFromTokenId
import com.stevdzasan.onetap.rememberOneTapSignInState



@Composable
fun TwinMindLoginScreen( onClick:(String )->Unit ) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF436C86),
            Color(0xFFD1A87C)
        )
    )
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient),
        contentAlignment = Alignment.BottomStart
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp)) // Top padding

            // Logo
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("tw")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFFFFA726),
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("i")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("n")
                        }
                    },
                    fontSize = 48.sp
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("   ")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("m")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFFFFA726),
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("!")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("n")
                        }

                        withStyle(
                            style = SpanStyle(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("d")
                        }
                    },
                    fontSize = 48.sp
                )
            }
            // Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OneTapGoogleButton(
                    clientId = context.getString(R.string.web_client_id),
                    onTokenIdReceived = { onClick(it) },
                    theme = GoogleButtonTheme.Neutral
                )

                Spacer(modifier = Modifier.height(64.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Privacy Policy",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { }
                    )
                    Text(
                        text = "Terms of Service",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { }
                    )
                }
            }
        }
    }
}


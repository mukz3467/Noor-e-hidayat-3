package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InAppYouTubePlayer(
    videoId: String,
    modifier: Modifier = Modifier,
    autoplay: Boolean = true,
    onFallbackRequested: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var hasError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isRendererCrashed by remember { mutableStateOf(false) }
    var reloadCount by remember { mutableIntStateOf(0) }

    val cleanVideoId = remember(videoId) {
        when {
            videoId.contains("watch?v=") -> videoId.substringAfter("watch?v=").substringBefore("&")
            videoId.contains("embed/") -> videoId.substringAfter("embed/").substringBefore("?")
            videoId.contains("youtu.be/") -> videoId.substringAfter("youtu.be/").substringBefore("?")
            else -> videoId.trim()
        }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
        ) {
            if (!isRendererCrashed) {
                key(cleanVideoId, reloadCount) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setBackgroundColor(android.graphics.Color.BLACK)

                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                    mediaPlaybackRequiresUserGesture = false
                                    cacheMode = WebSettings.LOAD_DEFAULT
                                    allowContentAccess = true
                                    allowFileAccess = false
                                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun getDefaultVideoPoster(): Bitmap? {
                                        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        val url = request?.url?.toString() ?: ""
                                        // Use the public embedded player without a forced account sign-in flow.
                                        return false
                                    }

                                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                        super.onReceivedError(view, request, error)
                                        if (request?.isForMainFrame == true) {
                                            hasError = true
                                            isLoading = false
                                        }
                                    }

                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        super.onPageStarted(view, url, favicon)
                                        hasError = false
                                        isLoading = true
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        isLoading = false
                                    }

                                    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                        try {
                                            val parent = view?.parent as? ViewGroup
                                            parent?.removeView(view)
                                            view?.stopLoading()
                                            view?.destroy()
                                        } catch (_: Exception) {}
                                        isRendererCrashed = true
                                        hasError = true
                                        isLoading = false
                                        return true
                                    }
                                }

                                val autoplayVal = if (autoplay) "1" else "0"
                                // Load standard official YouTube embed URL directly without synthetic parent iframe
                                val directEmbedUrl = "https://www.youtube.com/embed/$cleanVideoId?autoplay=$autoplayVal&playsinline=1&rel=0&modestbranding=1&controls=1"
                                loadUrl(directEmbedUrl)
                            }
                        },
                        onRelease = { wv ->
                            try {
                                wv.stopLoading()
                                wv.loadUrl("about:blank")
                                wv.clearHistory()
                                wv.removeAllViews()
                                wv.destroy()
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Loading spinner while player is connecting to stream
            if (isLoading && !hasError && !isRendererCrashed) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Connecting to Video Stream...",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Stream Fallback & Sign-In Card (If renderer or connection errors occur)
            if (hasError || isRendererCrashed) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Stream Ready",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "This broadcast could not be loaded. It may be offline, restricted, or unavailable in your region. Try another stream.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    hasError = false
                                    isRendererCrashed = false
                                    reloadCount++
                                    onFallbackRequested?.invoke()
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retry In-App", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

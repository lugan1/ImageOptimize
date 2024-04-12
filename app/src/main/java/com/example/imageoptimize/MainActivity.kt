package com.example.imageoptimize

import android.Manifest
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.imageoptimize.domain.file.BitmapUtil
import com.example.imageoptimize.domain.file.CustomFileProvider
import com.example.imageoptimize.domain.file.ImageUri
import com.example.imageoptimize.domain.file.RealPathUtil
import com.example.imageoptimize.ui.theme.ImageOptimizeTheme
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageOptimizeTheme {
                TestScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CustomFileProvider.clearCache(this)
    }
}


@Composable
fun TestScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current
        var cameraFile by remember { mutableStateOf<ImageUri?>(null) }
        val realPathUtil = remember { RealPathUtil(context) }
        val files = remember { mutableStateListOf<File>() }

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
            onResult = { success: Boolean ->
                if(success) {
                    cameraFile?.let {
                        val origin = BitmapFactory.decodeFile(it.file.absolutePath)
                        val bitmap = BitmapUtil.resize(origin)
                        BitmapUtil.write(bitmap, it.file)
                        bitmap.recycle()
                        files.add(it.file)
                    }
                }
            }
        )

        var showPermissionActivity by remember { mutableStateOf(false) }
        if(showPermissionActivity) {
            PermissionActivity(
                permission = Manifest.permission.CAMERA,
                onResult = { isGranted ->
                    showPermissionActivity = false
                    if(isGranted) {
                        // 파일이 저장될 uri 와 File 객체를 반환
                        cameraFile = CustomFileProvider.getImageUri(context)
                        val uri = requireNotNull(cameraFile).uri
                        cameraLauncher.launch(uri)
                    }
                    else {
                        //TODO: 카메라 촬영 권한 거부
                    }
                }
            )
        }

        val albumLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri ->
                uri?.let {
                    realPathUtil.getRealPath(uri)?.let {
                        val file = File(it)
                        val target = CustomFileProvider.getImageUri(context)
                        val origin = BitmapFactory.decodeFile(file.absolutePath)
                        val bitmap = BitmapUtil.resize(origin)
                        BitmapUtil.write(bitmap, target.file)?.let { resizedFile ->
                            files.add(resizedFile)
                        }
                        bitmap.recycle()
                    }
                }
            }
        )

        Button(onClick = {
            showPermissionActivity = true
        }) {
            Text(text = "카메라로 촬영하기")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            albumLauncher.launch("image/*")
        }) {
            Text(text = "앨범에서 가져오기")
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth().height(200.dp).border(1.dp, color = Color.Black),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(files, key = { it.path }) { file ->
                AsyncImage(
                    modifier = Modifier.size(100.dp),
                    model = file.path,
                    contentDescription = "이미지"
                )
            }
        }
    }
}

@Composable
fun PermissionActivity(
    permission: String,
    onResult: (Boolean) -> Unit = {}
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult
    )

    LaunchedEffect(key1 = launcher) {
        launcher.launch(permission)
    }
}

fun getSize(file: File): BigDecimal {
    return file.length().toDouble()
        .let { byte -> byte / 1024 / 1024 }
        .let { imageMB -> BigDecimal(imageMB).setScale(2, RoundingMode.HALF_EVEN) }
}
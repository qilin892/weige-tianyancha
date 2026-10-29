package com.weige.tianyancha

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weige.tianyancha.ui.CompanyScreen
import com.weige.tianyancha.ui.DimensionScreen
import com.weige.tianyancha.ui.SearchScreen
import com.weige.tianyancha.ui.WeigeTheme
import com.weige.tianyancha.vm.MainViewModel
import com.weige.tianyancha.vm.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeigeTheme(dark = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppRoot()
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun AppRoot(vm: MainViewModel = viewModel()) {
    val context = LocalContext.current

    // 统一 toast 出口
    LaunchedEffect(vm.toast) {
        val msg = vm.toast ?: return@LaunchedEffect
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        vm.consumeToast()
    }

    // 返回键：先逐级回退，到搜索页才退出 App
    BackHandler(enabled = true) {
        if (!vm.back()) {
            (context as? ComponentActivity)?.finish()
        }
    }

    Box(Modifier.fillMaxSize()) {
        when (val s = vm.screen) {
            Screen.Search -> SearchScreen(vm)
            is Screen.Company -> CompanyScreen(vm) { vm.back() }
            is Screen.Dimension -> DimensionScreen(vm, s.name) { vm.back() }
        }
    }
}

package com.weige.tianyancha

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weige.tianyancha.ui.CompanyScreen
import com.weige.tianyancha.ui.DimensionScreen
import com.weige.tianyancha.ui.SearchScreen
import com.weige.tianyancha.ui.ThemePref
import com.weige.tianyancha.ui.WeigeTheme
import com.weige.tianyancha.vm.MainViewModel
import com.weige.tianyancha.vm.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 默认白天模式；用户切换后记到 SharedPreferences
            var dark by remember { mutableStateOf(ThemePref.isDark(this)) }
            val ctx = this

            WeigeTheme(dark = dark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppRoot(
                        dark = dark,
                        onToggleTheme = {
                            dark = !dark
                            ThemePref.setDark(ctx, dark)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AppRoot(dark: Boolean, onToggleTheme: () -> Unit, vm: MainViewModel = viewModel()) {
    val context = LocalContext.current

    // 统一 toast 出口
    LaunchedEffect(vm.toast) {
        val msg = vm.toast ?: return@LaunchedEffect
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        vm.consumeToast()
    }

    // 返回键只在「有上一级」时拦截；搜索页交给系统默认处理
    // （否则键盘弹起时按返回会直接退出 App，而不是先收起键盘）
    BackHandler(enabled = vm.screen !is Screen.Search) {
        vm.back()
    }

    Box(Modifier.fillMaxSize()) {
        when (val s = vm.screen) {
            Screen.Search -> SearchScreen(vm, dark, onToggleTheme)
            is Screen.Company -> CompanyScreen(vm) { vm.back() }
            is Screen.Dimension -> DimensionScreen(vm, s.name) { vm.back() }
        }
    }
}

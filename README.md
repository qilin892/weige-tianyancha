# 伟哥天眼查 · Android 客户端

一个**打开就能查**的企业信息查询 App：输入公司名 → 秒出工商信息、17 类企业维度、可导出/分享 Excel。
后端自建 Cookie 池 + MySQL 缓存，扛住上游风鸟（riskbird）「单账号每天 50 次」的额度限制。

- 包名：`com.weige.tianyancha`
- 版本：`1.0.0`（versionCode 1）
- 最低支持：Android 8.0（API 26），目标 API 35
- 后端：`https://dify.eborry.com/riskbird/api`（Go live 见 `D:/riskbird-server`）

---

## 一、六条需求的落地方式

| # | 需求 | 实现 |
|---|---|---|
| 1 | 无需登录，打开即可查任意公司 | App 端**零账号体系**；后端用服务端 Cookie 池代用户访问上游，用户侧不感知。首次打开即出「热门查询」，输入即搜 |
| 2 | Cookie 池按已用频次轮替 | 后端 `cookie_pool.acquire()` 按 `used_today ASC, last_used_at ASC` 取「最闲」的 Cookie，取用完 `used_today+1`；额度耗尽自动跳过；JWT 剩 10 分钟时用 `refreshToken` 续期，池子可无限自续 |
| 3 | 查询结果落 MySQL + 定期分批刷新 | 首查写 `companies` / `dimensions`，之后再查直接读本地库（实测 33–43 ms）；`scheduler` 每 15 分钟取「被看得多、抓得少」的企业分批回源刷新，并保留 `quota_reserve` 额度给实时查询 |
| 4 | 搜索框 autocomplete | 调研出上游 `GET /riskbird-api/api/v1/companies/fast-search`——**匿名可达、不消耗每日额度**。App 输入防抖 260 ms 即调后端 `/api/autocomplete`，返回已切好的高亮分段，前端直接渲染文案 |
| 5 | 应用名与图标 | 名称 `伟哥天眼查`；图标见下文「图标设计」，已生成全套 mipmap + 自适应图标（`docs/icon_512.png` 可预览） |
| 6 | 导出/分享 Excel 到手机 | 后端 openpyxl 现拼 xlsx（每维度一个 sheet）；App 端 Android 10+ 用 MediaStore 直写「下载/伟哥天眼查/」**零权限**，8/9 写私有目录；分享走 FileProvider + 系统分享面板 |

---

## 二、代码结构

```
app/src/main/java/com/weige/tianyancha/
├── MainActivity.kt            单 Activity + Compose，BackHandler 逐级回退
├── net/
│   ├── Models.kt              Seg / Suggestion / Company / DimItem / DimensionPage / Stats
│   └── Api.kt                 OkHttp + org.json 手解析（不引 Gson，减依赖）
├── ui/
│   ├── Theme.kt               暗色主题，琥珀 #FF7A18
│   ├── Fields.kt              上游英文键 → 中文标签、核心字段排序、状态配色
│   ├── Components.kt          StatusChip / 高亮文本 / SectionCard / KeyValueRow
│   ├── SearchScreen.kt        品牌头 + 搜索框 + 补全面板 + 热门 + 统计
│   ├── CompanyScreen.kt       名片 + 工商信息（12 项预览可展开）+ 维度分组
│   └── DimensionScreen.kt     维度记录卡片列表 + 「加载更多」
├── vm/MainViewModel.kt        Screen 密封接口做导航；防抖、加载、错误、toast
└── export/ExcelExporter.kt    MediaStore 导出 + FileProvider 分享
```

导航没有用 Navigation 库，靠 `Screen` 密封接口 + `when` 派发，三屏切换足够且不引入额外依赖。

---

## 三、构建与安装

```bash
cd D:/weige-tianyancha
./gradlew assembleRelease        # 已配好签名，产物见下
./gradlew assembleDebug          # 调试包
```

产物：

- `app/build/outputs/apk/release/app-release.apk`（约 12 MB，已签名）
- `dist/weige-tianyancha-v1.0.0.apk`（可直接发手机安装）

签名：`keystore/weige-release.jks`，别名 `weige`，口令在仓库根 `keystore.properties`
（**两者都已进 `.gitignore`，不要提交**；换机打包请保留，否则无法覆盖升级）。

环境：JDK 17、Gradle 8.8、AGP 8.5.2、Kotlin 2.0.21、`sdk.dir=D:/android-sdk`。

换后端地址：改 `app/build.gradle.kts` 里的 `buildConfigField("String","API_BASE", ...)` 后重编。

---

## 四、界面

- **搜索页**：品牌头（Canvas 画的天眼标）、搜索框（补全、回车搜索、清空）、热门查询、收录统计。
  补全项直接展示「命中片段高亮 + 统一社会信用代码 + 经营状态标签」。
- **企业页**：名片（名称/信用代码/状态/维度数/已缓存数/数据条数）→ 工商信息（先给 12 项，可展开全部 62 项，标签全部中文化）→ 企业维度（按「基本信息 / 法律诉讼 / 经营风险 / …」分组，右侧显示条数与已缓存条数）→ 底部抓取时间与被查看次数。
- **维度页**：一条记录一张卡，自动挑一个「最能代表这行」的字段当标题，默认展开 3 个字段，点击展开全部；底部显示「当前显示 X / Y 条」与「加载更多」。

导出与分享在右上角：⬇ 导出（落「下载/伟哥天眼查/」）、↗ 分享（微信/QQ/邮件）。

---

## 五、图标设计

三层抽象，都在一个标记里：

1. **杏仁形眼眶** —— 「天眼查」的"看"，一眼看穿企业底细；
2. **琥珀色虹膜 + 光晕** —— 伟哥系列品牌色，也是"聚光/洞察"；
3. **虹膜里的深色 W 折线** —— 既是 WeiGe 的 W，也像一条数据趋势线；左右眼角两枚琥珀数据节点收尾。

生成脚本 `tools/gen_icon.py`（Pillow，1024 超采样 → LANCZOS 缩放），输出 5 档密度的
`ic_launcher.png` / `ic_launcher_round.png` / `ic_launcher_foreground.png`，
另有 `docs/icon_512.png`、`docs/icon_adaptive_{rounded,circle}.png` 用于预览。

> 自适应图标前景按 2/3 缩放：眼眶宽度 = 108dp 画布的 48%，正好是 72dp 可见区的 72%，
> 与普通图标视觉重量一致，圆形/方形掩码下都不切边。

---

## 六、已知限制

- **上游限流**：单账号每天 50 次详情抓取，且连续请求会触发 `operation:too:fast`（全局冷却 120 s）。
  因此维度数据是「缓存优先 + 按需扩展」：点开维度先给缓存，`canLoadMore` 时按 500 条一档向上游追加，
  超大维度（如岗位 16 万条）不会一次性拉全，这是设计取舍而非缺陷。
- **未在真机验证**：本次交付在本机完成编译与签名（`aapt2` 校验包名/图标/label 通过），
  但构建环境无模拟器与真机，**安装后的实机表现需你上手确认**。
- 经营状态配色按业内习惯：在营/存续绿、迁出/清算琥珀、注销/吊销灰（不套用股价「涨红跌绿」语义）。

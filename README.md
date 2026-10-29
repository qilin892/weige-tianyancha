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
│   ├── Theme.kt               浅色（默认）+ 深色双主题，琥珀 #FF7A18，SharedPreferences 记忆
│   ├── Fields.kt              约 300 条英文键 → 中文标签、图标映射、值类型判定、内部字段过滤
│   ├── Components.kt          StatusChip / FieldRow（按值类型渲染）/ SectionCard / 高亮文本
│   ├── SearchScreen.kt        品牌头 + 主题切换 + 搜索框 + 补全面板 + 热门查询
│   ├── CompanyScreen.kt       名片（4 条要点）+ 工商信息（12 项预览可展开）+ 维度分组
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

> 原则：**只留用户查企业要看的东西**。已收录企业数、维度总数、缓存条数、抓取时间、被查看次数、平台声明、
> 状态附加标记（`companyTag` / `tags` / `riskTags` / `dataSource` / `subjectType`）等一律不展示，
> 内部 id、图片字段、HTML 富文本、机器码同样过滤掉；所有字段名一律中文（约 300 条词表 + 逐词兜底翻译）。

- **搜索页**：品牌头（Canvas 画的天眼标）、**右上角主题切换**（默认白天 ☀️/🌙 一键切夜间，重启后记忆）、
  搜索框（防抖补全、回车搜索、清空）。补全项展示「命中片段高亮 + 统一社会信用代码 + 经营状态标签」。
- **企业页**：名片只给 4 条关键要点（法定代表人 / 注册资本 / 成立日期 / 注册地址，各带语义图标，
  注册资本橙色加粗）→ 工商信息（先给 12 项，可展开全部）→ 企业维度（按「基本信息 / 法律诉讼 / 经营风险 / …」
  分组，每行带维度图标、条数、「>」箭头）。
- **维度页**：一条记录一张卡，自动挑「最能代表这行」的字段当标题（名称型 > 事件型 > 兜底），
  默认展开 3 个字段，点击卡片展开全部；底部「当前显示 X / Y 条」与「加载更多」。

**按值类型选展示方式**（`valueKind()` 判定）：

| 类型 | 展示 |
|---|---|
| 经营状态 / 许可状态 | 圆角彩标（在营存续绿、迁出清算琥珀、注销吊销灰） |
| 电话 / 邮箱 / 网址 | 橙色可点，尾随外链图标，点击直接拨号 / 发信 / 开浏览器 |
| 金额 | 琥珀色加粗强调 |
| 是 / 否 | ✓ / ✗ 图标 + 文字 |
| 长文本（> 48 字） | 折叠两行，由外层卡片统一「展开全部 / 收起」 |
| 普通文本 / 数字 / 日期 | 常规排版，行首字段图标 + 中式标签 |

导出与分享在右上角：⬇ 导出（落「下载/伟哥天眼查/」）、↗ 分享（微信/QQ/邮件）。
Excel 同样是中文表头：`企业概览` 只留企业名称 / 统一社会信用代码 / 经营状态 / 导出时间；
`工商信息` 键值两列全中文；每维度一个 sheet，同名表格列自动合并（如 `altAf` 与 `altAfClean` 合成「变更后」），
数组字段用「、」连接，超链接样式与 HTML 标签在写入前清洗掉。

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
- **真机验证**：已在 HUAWEI BRA-AL00（Android 12 / 1216×2688）完成全链路实测：启动 → 输入 `huawei`
  补全出企业 → 进入详情 → 维度列表 → 「变更信息」记录卡展开全文 → 导出 Excel 落 `/sdcard/Download/伟哥天眼查/`
  → 分享面板弹出 → 白天/夜间主题切换并重启保持。
- 经营状态配色按业内习惯：在营/存续绿、迁出/清算琥珀、注销/吊销灰（不套用股价「涨红跌绿」语义）。

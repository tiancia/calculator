# 本地计算器

原生 Kotlin 安卓应用，界面使用 Android View。支持 Android 8.0（API 26）及以上。
输入表达式后立即计算，结果显示在表达式下方、数字键盘上方，无需点击等号。白色背景、橙色操作键。

## 本次交付状态

已编写项目配置、界面、计算逻辑和单元测试用例，并补齐官方 Gradle Wrapper。
本次没有执行 Gradle 同步、编译、单元测试或模拟器/真机测试，也没有生成 APK；运行效果需要按下文验证。

## 使用方式

- 使用应用内数字键盘输入，运算顺序为先乘除、后加减。
- `12+3×4` 会实时显示 `= 24`；`12+` 尚未完成时隐藏结果。
- `AC` 清空，`⌫` 删除最后一个字符，长按 `⌫` 清空。
- `±` 切换当前数的正负号；`%` 将当前数除以 100。
- `=` 将结果放回输入区；此后输入数字开始新计算，输入运算符继续计算。
- 除以零显示提示；重复小数点不会插入；连续运算符通常替换前一个，乘除后允许负号。
- 长表达式、长结果可左右滑动查看；横屏和小窗口可上下滚动页面。旋转屏幕保留当前输入。

百分比采用固定数学含义：`200×10% = 20`，`200+10% = 200.1`。本版本不实现商业计算器的“加价百分比”规则。
运算采用 BigDecimal，算术运算按 16 位有效数字舍入，例如 `1÷3 = 0.3333333333333333`；输入最多 120 个字符。
应用没有联网权限，不保存计算历史，不使用后端或数据库。安装后的计算功能可以离线使用，首次构建需要联网下载依赖。

## 在 Android Studio 中打开

1. 启动 Android Studio，选择 **Open**，打开 `A:\explore\calculator` 整个目录。
2. 等待 Gradle Sync 完成。项目使用 AGP `9.1.1`、Gradle `9.3.1`，Kotlin 由 AGP 内置支持，不需要另装 Kotlin 插件。
3. 在 **Settings → Build, Execution, Deployment → Build Tools → Gradle** 中，Gradle JDK 选择 Android Studio 自带 JDK：`A:\Android\Android Studio\jbr`。
4. SDK 路径已写入本机 `local.properties`：`A:/Android/Sdk`。更换电脑时，在 Android Studio 重新选择本机 SDK，或修改这个文件；不要提交它。
5. **Tools → SDK Manager** 中确认安装 Android SDK Platform API 37 和 Android SDK Build-Tools 36.0.0。项目已匹配这台电脑上检测到的组件。

若首次同步失败，先检查网络能否访问 Google Maven、Maven Central 和 Gradle 下载服务。需要代理时在 Android Studio 的 HTTP Proxy 设置中配置自己的代理，不要把账户或密码写入项目。

## 运行和测试

### 使用模拟器（无需连接手机）

1. 打开 **Tools → Device Manager**，选择 **Create Virtual Device**（部分版本为 `+` → Create Virtual Device）。
2. 选择一款手机设备，如 Pixel，下载 API 26 或以上的正式版系统镜像。SDK Platform 与模拟器系统镜像是两个不同组件。
3. 创建并启动模拟器。
4. 顶部运行配置选择 `app`，设备选择刚启动的模拟器，点击绿色 **Run ▶**。
5. 首次运行会编译并安装应用。除常规尺寸外，建议再检查横屏、分屏和较大系统字体。

### 使用手机（可选）

1. 在安卓手机上开启开发者选项和 USB 调试，用支持数据传输的 USB 线连接电脑。
2. 在手机上允许此电脑的 USB 调试授权；如手机未识别，检查线缆、USB 模式及厂商驱动。
3. Android Studio 顶部选择手机，点击 **Run ▶**。小米手机如提示禁止安装，还需根据系统提示开启 USB 安装。

### 自动测试与静态检查

在 PowerShell 中运行以下命令。这些命令是供后续使用的，本次未执行：

```powershell
Set-Location 'A:\explore\calculator'
$env:JAVA_HOME = 'A:\Android\Android Studio\jbr'
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug
```

不需要连接设备。测试覆盖优先级、小数、负数、百分比、除零、输入未完成、退格、等号后的继续计算、状态恢复及长度限制。
单元测试报告：`app\build\reports\tests\testDebugUnitTest\index.html`。
Lint 报告：`app\build\reports\lint-results-debug.html`。
这些检查不能代替界面测试。

### 手工验收清单

| 操作 | 预期 |
| --- | --- |
| 输入 `12+3×4`，不按等号 | 输入下方、键盘上方立即显示 `= 24` |
| 输入 `0.1+0.2` | `= 0.3` |
| 输入 `1÷3` | `= 0.3333333333333333` |
| 输入 `8÷0` | 显示“不能除以零”，不闪退 |
| 输入 `12+3`，再退格 | 输入变为 `12+`，隐藏旧结果 |
| 输入 `12+3`，按 `±` | `12+−3`，结果 `= 9` |
| 输入 `200×10%` | `= 20` |
| 输入 `2+3` 后按 `=`，再输入 `×4` | `= 20` |
| 按 `=` 后输入 `7` | 开始新的计算 `7` |
| 连按两个小数点 | 只保留一个小数点 |
| 旋转屏幕、返回应用 | 旋转时输入保留；按键可访问、底部不被系统导航栏挡住 |
| 输入长表达式 | 能横向滑动查看，超出 120 字符有提示 |
| 开飞行模式后计算 | 功能正常 |

## 打包 APK

### 调试 APK：自己安装和测试

无需手机或模拟器，PowerShell 执行：

```powershell
Set-Location 'A:\explore\calculator'
$env:JAVA_HOME = 'A:\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug
```

生成位置：`A:\explore\calculator\app\build\outputs\apk\debug\app-debug.apk`。
这个 APK 自动使用调试密钥签名，可以复制到手机并允许对应文件管理器安装未知应用后安装。
也可以用已授权 USB 调试的设备安装：

```powershell
& 'A:\Android\Sdk\platform-tools\adb.exe' devices
& 'A:\Android\Sdk\platform-tools\adb.exe' install -r '.\app\build\outputs\apk\debug\app-debug.apk'
```

### 正式 APK：用于分发

1. Android Studio 菜单选择 **Build → Generate Signed App Bundle or APK**（有些版本显示 Generate Signed Bundle / APK）。
2. 选择 **APK**，模块选择 `app`。
3. 首次点击 **Create new** 创建签名密钥，保存到项目目录之外，并妥善保存密钥文件、别名和密码。
4. 选择 **release**，设置输出位置，完成构建后点击 **Locate** 查看生成的 APK。
5. 用该正式 APK 再做一次上面的手工验收。

本项目没有内置正式签名配置；单独运行 `assembleRelease` 不会得到可直接分发的正式签名包。
后续升级必须继续使用同一应用 ID 和签名密钥，并增加 `app/build.gradle.kts` 中的 `versionCode`。
调试包和正式包签名不同，直接覆盖可能失败；测试手机可先卸载调试包再装正式包。

## 文件位置

- `app/src/main/java/com/local/calculator/MainActivity.kt`：界面、键盘、实时结果刷新、屏幕旋转恢复。
- `app/src/main/java/com/local/calculator/Calculator.kt`：十进制计算和键盘输入处理。
- `app/src/test/java/com/local/calculator/CalculatorTest.kt`：可后续运行的单元测试。
- `app/src/main/res/`：文案、主题和图标。
- `app/build.gradle.kts`、根目录 Gradle 文件与 `gradle/wrapper/`：构建配置与启动工具。

版本匹配与操作参考：[AGP 9.1.1 兼容性](https://developer.android.com/build/releases/agp-9-1-0-release-notes)、[命令行构建](https://developer.android.com/build/building-cmdline)、[应用签名](https://developer.android.com/studio/publish/app-signing)。

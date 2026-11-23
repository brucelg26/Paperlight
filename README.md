# 纸间 · Paperlight — Android APK

手机端极简学术论文阅读器：本地 PDF 打开、单栏阅读、原版 PDF 对照、字号/配色、阅读进度保存。Android App 包含离线 PDF.js，**无需部署 GitHub Pages**。

## 获取安装包

打开 [GitHub Actions](https://github.com/brucelg26/Paperlight/actions/workflows/android-apk.yml)，选择最新成功的 **Build Paperlight APK**，在 **Artifacts** 下载 **Paperlight-Android-APK**，解压后得到 `app-debug.apk`，将其安装到安卓手机。

首次构建需下载 Android SDK、Gradle 与 pdfjs-dist，Actions 运行环境需联网。最终 APK 中打包好本地阅读资源，**阅读本地 PDF 不依赖网络**。

## 注意

- 当前为调试版 APK，未经 Android 实机验证，不是 Google Play 发布包。
- GitHub Actions 不同运行生成的调试签名可能不同，未来升级若提示签名不一致，需要卸载旧版重新安装（可能丢失本地阅读历史）。生产更新需要固定签名密钥。
- 原版双栏论文重排、脚注、表格、公式可能出现乱序，请随时切换至原版 PDF 核对。
- PDF 保存在设备本地（WebView IndexedDB，受空间限制），不会上传论文。
- 没有包含任何 ResearchLab 论文 PDF，也没有 Zotero 云同步。

## 开发

项目为原版 HTML Paperlight 的安卓 WebView 封装，`app/src/main/assets/index.html` 是阅读界面。首次构建从 npm 固定版本的 PDF.js 打包 `pdf.min.js` 和 `pdf.worker.min.js`。原网页界面还未做原生 Android 无障碍与大文件性能专项测试。

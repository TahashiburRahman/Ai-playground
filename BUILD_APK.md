# Get your APK (no local SDK needed)
1. Create a free GitHub repo, upload ALL files in this zip (keep .github folder).
2. (Optional) Repo Settings > Secrets > Actions > add GEMINI_API_KEY with your real key.
3. Go to Actions tab > "Build APK" > wait ~5 min > download artifact "AI-Studio-debug-apk" (contains app-debug.apk).
4. Unzip, install the APK on your phone (allow unknown sources).

Local build (Android Studio): open this folder, set GEMINI_API_KEY in .env, Build > Build APK.

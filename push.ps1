# Первая публикация в https://github.com/saidsafikhon/leaphouse-remote-ios
# Перед запуском создайте на GitHub пустой репозиторий leaphouse-remote-ios
# (без README/.gitignore). Запуск: ! powershell -File "D:/PROJECT/leaphouse-remote-ios/push.ps1"
Set-Location D:\PROJECT\leaphouse-remote-ios
if (-not (Test-Path .git)) { git init -b main }
git config user.name "saidsafikhon"
git config user.email "saidsafikhon@gmail.com"
git add -A
git commit -m @'
Leaphouse Remote iOS 0.38.19 (75): SwiftUI-порт Android-клиента + CI

Все 13 экранов Android-версии: вход/регистрация/сброс, ворота парка,
привязка по QR, подключение с побудкой, главная, климат+сиденья, сцены,
расписание, голос, карта, настройки. Backend leapmotor.evon.uz, та же карта
команд C16. Workflow ios.yml собирает под симулятор и неподписанный .ipa.

Co-Authored-By: Claude Opus 5 (1M context) <noreply@anthropic.com>
'@
git remote remove origin 2>$null
git remote add origin https://github.com/saidsafikhon/leaphouse-remote-ios.git
git push -u origin main

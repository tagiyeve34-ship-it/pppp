# Ailə Nəzarəti — Native Panel APK

Bu, valideyn idarə panelinin **tam native** Android tətbiqidir (WebView yoxdur).
Kotlin ilə yazılıb, serverdən JSON API vasitəsilə data çəkir, xəritə üçün
osmdroid (native, API açarı tələb etməyən) istifadə edir.

## 1. Server tərəfi (əvvəlcə bunu edin)

Bu APK-nın işləməsi üçün serverinizdə JSON API endpoint-ləri olmalıdır
(ayrıca `server-native-api.zip` paketində göndərilib):

1. `migration_api_sessions.sql`-i phpMyAdmin-də işə salın
2. `api/` qovluğunu `webpanel/api/` içinə yükləyin

## 2. APK-nı server ünvanınıza bağlayın

`app/build.gradle` faylını açın, bu sətri öz ünvanınızla dəyişin:

```groovy
buildConfigField "String", "API_BASE_URL", "\"https://hesabat.site/usaq/webpanel/api\""
```

## 3. GitHub-a qoyun və build edin

1. Yeni GitHub repo yaradın
2. Bu qovluğun içindəkiləri (`.github`, `.gitignore`, `app`, `build.gradle`,
   `settings.gradle`, `gradle.properties`) yükləyin
3. "Actions" sekmesinə keçin — build avtomatik başlayacaq
4. Bitəndə "Artifacts" bölməsindən `aile-nezareti-panel-apk` faylını yükləyin,
   içində `app-debug.apk` olacaq

## Ekranlar

- **Giriş** — e-poçt/şifrə ilə (eyni hesab, dashboard-dakı kimi)
- **Ana səhifə** — canlı status kartı, bugünkü qeyd sayı, oxunmamış xəbərdarlıq,
  son fəaliyyət lenti
- **Xəritə** — native osmdroid xəritəsi, marşrut xətti, tarix filtri (3 saat/6 saat/bugün/3 gün/7 gün/hamısı)
- **Zənglər** — zəng tarixçəsi siyahısı
- **Xəbərlər** — xəbərdarlıqlar, toxunaraq "oxundu" işarələmək

Üstdə uşaq çipləri ilə profillər arasında keçid edilir, seçim bütün ekranlara tətbiq olunur.

## Qeyd

Bu tətbiq yalnız **valideyn panelinə** aiddir — uşağın telefonundakı izləyici
APK-ya heç bir təsiri yoxdur, tamamilə ayrı layihədir.

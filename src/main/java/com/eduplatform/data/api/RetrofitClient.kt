package com.eduplatform.data.api

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

// Change this to your backend URL (must end with /api/v1/)
const val BASE_URL = "https://eduplatform-production-8603.up.railway.app/api/v1/"

/**
 * The backend issues accessToken/refreshToken as httpOnly cookies (see API docs §3) and
 * never echoes a real JWT in the JSON body (AuthData.accessToken stays null — see
 * EduRepository, which falls back to a local "cookie-session" marker).
 *
 * That means the cookies ARE the session. The previous implementation
 * (InMemoryCookieJar) kept them only in a Kotlin Map living in process memory.
 * That's fine until the process dies — which on Android happens constantly and
 * silently (user swipes the app away, OS reclaims memory in the background,
 * etc.), not just on an explicit "log out". On the next cold start a brand new
 * process spins up with an empty cookie store, while the "cookie-session"
 * marker in DataStore (TokenStorage) survives and still says "logged in".
 * AppNavigation then calls refreshSession() -> POST /auth/refresh, which has no
 * cookies to send, the backend replies 401 ("Токен авторизації відсутній..."),
 * and the app force-logs-out the user even though their refresh cookie was
 * still perfectly valid server-side. This is exactly the bug in the logs:
 * "Loading 0 cookies ... []" right before the 401.
 *
 * Fix: mirror cookies into SharedPreferences synchronously on every save, and
 * rehydrate them in the constructor (i.e. before the first request of a new
 * process ever goes out). SharedPreferences is used instead of DataStore
 * because CookieJar's methods are synchronous and called directly on the
 * network thread — DataStore's Flow/suspend API would mean either blocking
 * that thread or risking a request going out before the read completes.
 */
@Singleton
class PersistentCookieJar @Inject constructor(
    @ApplicationContext context: Context
) : CookieJar {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("cookie_store", Context.MODE_PRIVATE)

    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()

    init {
        loadFromDisk()
    }

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return
        val host = url.host
        val existing = cookieStore.getOrPut(host) { mutableListOf() }
        val now = System.currentTimeMillis()
        for (cookie in cookies) {
            existing.removeAll { it.name == cookie.name }
            // A Set-Cookie with an already-past expiry is the server explicitly
            // telling us to delete the cookie (e.g. on logout) — honor that
            // instead of writing a dead cookie back to disk.
            if (cookie.expiresAt > now) {
                existing.add(cookie)
            }
        }
        android.util.Log.d("CookieJar", "Saved ${cookies.size} cookies for $host: ${cookies.map { it.name }}")
        persistToDisk()
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        val now = System.currentTimeMillis()
        // Collect cookies from exact host AND any parent domain matches
        val result = mutableListOf<Cookie>()
        var anyExpired = false
        for ((storedHost, cookies) in cookieStore) {
            if (host == storedHost || host.endsWith(".$storedHost") || storedHost.endsWith(".$host")) {
                val before = cookies.size
                cookies.removeAll { it.expiresAt in 1 until now }
                if (cookies.size != before) anyExpired = true
                result.addAll(cookies)
            }
        }
        if (anyExpired) persistToDisk()
        android.util.Log.d("CookieJar", "Loading ${result.size} cookies for $host: ${result.map { it.name }}")
        return result
    }

    @Synchronized
    fun clear() {
        cookieStore.clear()
        prefs.edit().clear().apply()
    }

    // ── persistence ──────────────────────────────────────────────────────────

    private fun persistToDisk() {
        val arr = JSONArray()
        cookieStore.values.flatten().forEach { cookie ->
            val obj = JSONObject()
            obj.put("name", cookie.name)
            obj.put("value", cookie.value)
            obj.put("domain", cookie.domain)
            obj.put("path", cookie.path)
            obj.put("expiresAt", cookie.expiresAt)
            obj.put("secure", cookie.secure)
            obj.put("httpOnly", cookie.httpOnly)
            obj.put("hostOnly", cookie.hostOnly)
            arr.put(obj)
        }
        // apply() is async/fire-and-forget — fine here, we already hold the
        // up-to-date state in `cookieStore` for any request in this process.
        prefs.edit().putString(KEY_COOKIES, arr.toString()).apply()
    }

    private fun loadFromDisk() {
        val raw = prefs.getString(KEY_COOKIES, null) ?: return
        val now = System.currentTimeMillis()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val expiresAt = obj.getLong("expiresAt")
                if (expiresAt in 1 until now) continue // skip already-expired entries

                val domain = obj.getString("domain")
                val hostOnly = obj.getBoolean("hostOnly")
                val builder = Cookie.Builder()
                    .name(obj.getString("name"))
                    .value(obj.getString("value"))
                    .path(obj.getString("path"))
                    .expiresAt(expiresAt)
                if (hostOnly) builder.hostOnlyDomain(domain) else builder.domain(domain)
                if (obj.getBoolean("secure")) builder.secure()
                if (obj.getBoolean("httpOnly")) builder.httpOnly()

                cookieStore.getOrPut(domain) { mutableListOf() }.add(builder.build())
            }
            android.util.Log.d("CookieJar", "Rehydrated ${arr.length()} cookies from disk")
        } catch (e: Exception) {
            android.util.Log.e("CookieJar", "Failed to load persisted cookies, starting empty", e)
        }
    }

    companion object {
        private const val KEY_COOKIES = "cookies"
    }
}

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        // Cookies are attached automatically by PersistentCookieJar. We additionally
        // send the Bearer header, but ONLY when we have a real JWT (three dot-separated
        // segments). The "cookie-session" placeholder used when the backend didn't
        // return a token in the JSON body must never be sent as a Bearer token —
        // the backend would try to parse it as a JWT and reject the whole request
        // with 401, even if a valid auth cookie was also attached.
        val token = tokenStorage.cachedAccessToken
        val isRealJwt = token != null && token.count { it == '.' } == 2
        val request = if (isRealJwt) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}

/**
 * Any request that comes back 401 means the access token (and/or refresh cookie)
 * is no longer valid — most commonly because it expired while the app was open
 * on some tab other than the catalog (which doesn't require auth). Previously
 * nothing reacted to this: the screen just showed whatever error message the
 * body carried, and the user stayed stuck on a broken tab.
 *
 * This interceptor force-clears the local session (DataStore token + cookies)
 * the moment a 401 comes back from any authenticated endpoint. AppNavigation's
 * root composable observes `accessToken` reactively, so as soon as it flips to
 * null the UI swaps straight to the Login screen — no extra plumbing needed in
 * every ViewModel.
 *
 * Login/register themselves can legitimately return 401 for "wrong password" —
 * that's not a session expiring, so those two endpoints are excluded.
 */
@Singleton
class SessionExpiredInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val cookieJar: PersistentCookieJar
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 401) {
            val path = request.url.encodedPath
            val isCredentialsCheck = path.endsWith("/auth/login") || path.endsWith("/auth/register")
            if (!isCredentialsCheck) {
                android.util.Log.d("SessionExpired", "401 on $path — clearing local session")
                kotlinx.coroutines.runBlocking { tokenStorage.clear() }
                cookieJar.clear()
            }
        }

        return response
    }
}

fun buildOkHttpClient(
    authInterceptor: AuthInterceptor,
    sessionExpiredInterceptor: SessionExpiredInterceptor,
    cookieJar: PersistentCookieJar
): OkHttpClient {
    val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    return OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor(authInterceptor)
        .addInterceptor(sessionExpiredInterceptor)
        .addInterceptor(logging)
        .build()
}

fun buildRetrofit(okHttpClient: OkHttpClient): Retrofit =
    Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

fun buildApiService(retrofit: Retrofit): EduApiService =
    retrofit.create(EduApiService::class.java)
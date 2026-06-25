package com.eduplatform.di;

import com.eduplatform.data.api.AuthInterceptor;
import com.eduplatform.data.api.PersistentCookieJar;
import com.eduplatform.data.api.SessionExpiredInterceptor;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class AppModule_ProvideOkHttpClientFactory implements Factory<OkHttpClient> {
  private final Provider<AuthInterceptor> authInterceptorProvider;

  private final Provider<SessionExpiredInterceptor> sessionExpiredInterceptorProvider;

  private final Provider<PersistentCookieJar> cookieJarProvider;

  public AppModule_ProvideOkHttpClientFactory(Provider<AuthInterceptor> authInterceptorProvider,
      Provider<SessionExpiredInterceptor> sessionExpiredInterceptorProvider,
      Provider<PersistentCookieJar> cookieJarProvider) {
    this.authInterceptorProvider = authInterceptorProvider;
    this.sessionExpiredInterceptorProvider = sessionExpiredInterceptorProvider;
    this.cookieJarProvider = cookieJarProvider;
  }

  @Override
  public OkHttpClient get() {
    return provideOkHttpClient(authInterceptorProvider.get(), sessionExpiredInterceptorProvider.get(), cookieJarProvider.get());
  }

  public static AppModule_ProvideOkHttpClientFactory create(
      Provider<AuthInterceptor> authInterceptorProvider,
      Provider<SessionExpiredInterceptor> sessionExpiredInterceptorProvider,
      Provider<PersistentCookieJar> cookieJarProvider) {
    return new AppModule_ProvideOkHttpClientFactory(authInterceptorProvider, sessionExpiredInterceptorProvider, cookieJarProvider);
  }

  public static OkHttpClient provideOkHttpClient(AuthInterceptor authInterceptor,
      SessionExpiredInterceptor sessionExpiredInterceptor, PersistentCookieJar cookieJar) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideOkHttpClient(authInterceptor, sessionExpiredInterceptor, cookieJar));
  }
}

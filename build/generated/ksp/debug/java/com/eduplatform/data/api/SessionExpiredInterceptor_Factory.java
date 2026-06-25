package com.eduplatform.data.api;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class SessionExpiredInterceptor_Factory implements Factory<SessionExpiredInterceptor> {
  private final Provider<TokenStorage> tokenStorageProvider;

  private final Provider<PersistentCookieJar> cookieJarProvider;

  public SessionExpiredInterceptor_Factory(Provider<TokenStorage> tokenStorageProvider,
      Provider<PersistentCookieJar> cookieJarProvider) {
    this.tokenStorageProvider = tokenStorageProvider;
    this.cookieJarProvider = cookieJarProvider;
  }

  @Override
  public SessionExpiredInterceptor get() {
    return newInstance(tokenStorageProvider.get(), cookieJarProvider.get());
  }

  public static SessionExpiredInterceptor_Factory create(
      Provider<TokenStorage> tokenStorageProvider,
      Provider<PersistentCookieJar> cookieJarProvider) {
    return new SessionExpiredInterceptor_Factory(tokenStorageProvider, cookieJarProvider);
  }

  public static SessionExpiredInterceptor newInstance(TokenStorage tokenStorage,
      PersistentCookieJar cookieJar) {
    return new SessionExpiredInterceptor(tokenStorage, cookieJar);
  }
}

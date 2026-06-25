package com.eduplatform.data.repository;

import com.eduplatform.data.api.EduApiService;
import com.eduplatform.data.api.PersistentCookieJar;
import com.eduplatform.data.api.TokenStorage;
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
public final class EduRepository_Factory implements Factory<EduRepository> {
  private final Provider<EduApiService> apiProvider;

  private final Provider<TokenStorage> tokenStorageProvider;

  private final Provider<PersistentCookieJar> cookieJarProvider;

  public EduRepository_Factory(Provider<EduApiService> apiProvider,
      Provider<TokenStorage> tokenStorageProvider,
      Provider<PersistentCookieJar> cookieJarProvider) {
    this.apiProvider = apiProvider;
    this.tokenStorageProvider = tokenStorageProvider;
    this.cookieJarProvider = cookieJarProvider;
  }

  @Override
  public EduRepository get() {
    return newInstance(apiProvider.get(), tokenStorageProvider.get(), cookieJarProvider.get());
  }

  public static EduRepository_Factory create(Provider<EduApiService> apiProvider,
      Provider<TokenStorage> tokenStorageProvider,
      Provider<PersistentCookieJar> cookieJarProvider) {
    return new EduRepository_Factory(apiProvider, tokenStorageProvider, cookieJarProvider);
  }

  public static EduRepository newInstance(EduApiService api, TokenStorage tokenStorage,
      PersistentCookieJar cookieJar) {
    return new EduRepository(api, tokenStorage, cookieJar);
  }
}

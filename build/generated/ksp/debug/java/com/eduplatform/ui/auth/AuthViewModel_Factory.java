package com.eduplatform.ui.auth;

import com.eduplatform.data.api.TokenStorage;
import com.eduplatform.data.repository.EduRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class AuthViewModel_Factory implements Factory<AuthViewModel> {
  private final Provider<EduRepository> repositoryProvider;

  private final Provider<TokenStorage> tokenStorageProvider;

  public AuthViewModel_Factory(Provider<EduRepository> repositoryProvider,
      Provider<TokenStorage> tokenStorageProvider) {
    this.repositoryProvider = repositoryProvider;
    this.tokenStorageProvider = tokenStorageProvider;
  }

  @Override
  public AuthViewModel get() {
    return newInstance(repositoryProvider.get(), tokenStorageProvider.get());
  }

  public static AuthViewModel_Factory create(Provider<EduRepository> repositoryProvider,
      Provider<TokenStorage> tokenStorageProvider) {
    return new AuthViewModel_Factory(repositoryProvider, tokenStorageProvider);
  }

  public static AuthViewModel newInstance(EduRepository repository, TokenStorage tokenStorage) {
    return new AuthViewModel(repository, tokenStorage);
  }
}

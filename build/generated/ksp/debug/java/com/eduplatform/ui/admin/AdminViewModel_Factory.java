package com.eduplatform.ui.admin;

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
public final class AdminViewModel_Factory implements Factory<AdminViewModel> {
  private final Provider<EduRepository> repositoryProvider;

  public AdminViewModel_Factory(Provider<EduRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public AdminViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static AdminViewModel_Factory create(Provider<EduRepository> repositoryProvider) {
    return new AdminViewModel_Factory(repositoryProvider);
  }

  public static AdminViewModel newInstance(EduRepository repository) {
    return new AdminViewModel(repository);
  }
}

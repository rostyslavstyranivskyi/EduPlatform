package com.eduplatform.ui.progress;

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
public final class ProgressViewModel_Factory implements Factory<ProgressViewModel> {
  private final Provider<EduRepository> repositoryProvider;

  public ProgressViewModel_Factory(Provider<EduRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ProgressViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static ProgressViewModel_Factory create(Provider<EduRepository> repositoryProvider) {
    return new ProgressViewModel_Factory(repositoryProvider);
  }

  public static ProgressViewModel newInstance(EduRepository repository) {
    return new ProgressViewModel(repository);
  }
}

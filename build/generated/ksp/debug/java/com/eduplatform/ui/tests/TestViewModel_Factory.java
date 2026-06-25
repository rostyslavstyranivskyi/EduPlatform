package com.eduplatform.ui.tests;

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
public final class TestViewModel_Factory implements Factory<TestViewModel> {
  private final Provider<EduRepository> repositoryProvider;

  public TestViewModel_Factory(Provider<EduRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public TestViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static TestViewModel_Factory create(Provider<EduRepository> repositoryProvider) {
    return new TestViewModel_Factory(repositoryProvider);
  }

  public static TestViewModel newInstance(EduRepository repository) {
    return new TestViewModel(repository);
  }
}

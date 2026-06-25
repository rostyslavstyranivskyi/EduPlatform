package com.eduplatform.ui.topics;

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
public final class TopicsViewModel_Factory implements Factory<TopicsViewModel> {
  private final Provider<EduRepository> repositoryProvider;

  public TopicsViewModel_Factory(Provider<EduRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public TopicsViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static TopicsViewModel_Factory create(Provider<EduRepository> repositoryProvider) {
    return new TopicsViewModel_Factory(repositoryProvider);
  }

  public static TopicsViewModel newInstance(EduRepository repository) {
    return new TopicsViewModel(repository);
  }
}

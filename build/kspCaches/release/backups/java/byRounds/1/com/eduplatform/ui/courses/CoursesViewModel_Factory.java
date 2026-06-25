package com.eduplatform.ui.courses;

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
public final class CoursesViewModel_Factory implements Factory<CoursesViewModel> {
  private final Provider<EduRepository> repositoryProvider;

  public CoursesViewModel_Factory(Provider<EduRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public CoursesViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static CoursesViewModel_Factory create(Provider<EduRepository> repositoryProvider) {
    return new CoursesViewModel_Factory(repositoryProvider);
  }

  public static CoursesViewModel newInstance(EduRepository repository) {
    return new CoursesViewModel(repository);
  }
}

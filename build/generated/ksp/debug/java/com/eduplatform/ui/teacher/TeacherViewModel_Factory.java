package com.eduplatform.ui.teacher;

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
public final class TeacherViewModel_Factory implements Factory<TeacherViewModel> {
  private final Provider<EduRepository> repositoryProvider;

  public TeacherViewModel_Factory(Provider<EduRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public TeacherViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static TeacherViewModel_Factory create(Provider<EduRepository> repositoryProvider) {
    return new TeacherViewModel_Factory(repositoryProvider);
  }

  public static TeacherViewModel newInstance(EduRepository repository) {
    return new TeacherViewModel(repository);
  }
}

package com.eduplatform;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.eduplatform.data.api.AuthInterceptor;
import com.eduplatform.data.api.EduApiService;
import com.eduplatform.data.api.PersistentCookieJar;
import com.eduplatform.data.api.SessionExpiredInterceptor;
import com.eduplatform.data.api.TokenStorage;
import com.eduplatform.data.repository.EduRepository;
import com.eduplatform.di.AppModule_ProvideApiServiceFactory;
import com.eduplatform.di.AppModule_ProvideOkHttpClientFactory;
import com.eduplatform.di.AppModule_ProvideRetrofitFactory;
import com.eduplatform.ui.admin.AdminViewModel;
import com.eduplatform.ui.admin.AdminViewModel_HiltModules;
import com.eduplatform.ui.auth.AuthViewModel;
import com.eduplatform.ui.auth.AuthViewModel_HiltModules;
import com.eduplatform.ui.courses.CoursesViewModel;
import com.eduplatform.ui.courses.CoursesViewModel_HiltModules;
import com.eduplatform.ui.lessons.LessonsViewModel;
import com.eduplatform.ui.lessons.LessonsViewModel_HiltModules;
import com.eduplatform.ui.progress.ProgressViewModel;
import com.eduplatform.ui.progress.ProgressViewModel_HiltModules;
import com.eduplatform.ui.teacher.TeacherViewModel;
import com.eduplatform.ui.teacher.TeacherViewModel_HiltModules;
import com.eduplatform.ui.tests.TestViewModel;
import com.eduplatform.ui.tests.TestViewModel_HiltModules;
import com.eduplatform.ui.topics.TopicsViewModel;
import com.eduplatform.ui.topics.TopicsViewModel_HiltModules;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class DaggerEduPlatformApp_HiltComponents_SingletonC {
  private DaggerEduPlatformApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public EduPlatformApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements EduPlatformApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public EduPlatformApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements EduPlatformApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public EduPlatformApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements EduPlatformApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public EduPlatformApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements EduPlatformApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public EduPlatformApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements EduPlatformApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public EduPlatformApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements EduPlatformApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public EduPlatformApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements EduPlatformApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public EduPlatformApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends EduPlatformApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends EduPlatformApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends EduPlatformApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends EduPlatformApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(8).put(LazyClassKeyProvider.com_eduplatform_ui_admin_AdminViewModel, AdminViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_eduplatform_ui_auth_AuthViewModel, AuthViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_eduplatform_ui_courses_CoursesViewModel, CoursesViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_eduplatform_ui_lessons_LessonsViewModel, LessonsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_eduplatform_ui_progress_ProgressViewModel, ProgressViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_eduplatform_ui_teacher_TeacherViewModel, TeacherViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_eduplatform_ui_tests_TestViewModel, TestViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_eduplatform_ui_topics_TopicsViewModel, TopicsViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_eduplatform_ui_tests_TestViewModel = "com.eduplatform.ui.tests.TestViewModel";

      static String com_eduplatform_ui_admin_AdminViewModel = "com.eduplatform.ui.admin.AdminViewModel";

      static String com_eduplatform_ui_courses_CoursesViewModel = "com.eduplatform.ui.courses.CoursesViewModel";

      static String com_eduplatform_ui_progress_ProgressViewModel = "com.eduplatform.ui.progress.ProgressViewModel";

      static String com_eduplatform_ui_topics_TopicsViewModel = "com.eduplatform.ui.topics.TopicsViewModel";

      static String com_eduplatform_ui_lessons_LessonsViewModel = "com.eduplatform.ui.lessons.LessonsViewModel";

      static String com_eduplatform_ui_auth_AuthViewModel = "com.eduplatform.ui.auth.AuthViewModel";

      static String com_eduplatform_ui_teacher_TeacherViewModel = "com.eduplatform.ui.teacher.TeacherViewModel";

      @KeepFieldType
      TestViewModel com_eduplatform_ui_tests_TestViewModel2;

      @KeepFieldType
      AdminViewModel com_eduplatform_ui_admin_AdminViewModel2;

      @KeepFieldType
      CoursesViewModel com_eduplatform_ui_courses_CoursesViewModel2;

      @KeepFieldType
      ProgressViewModel com_eduplatform_ui_progress_ProgressViewModel2;

      @KeepFieldType
      TopicsViewModel com_eduplatform_ui_topics_TopicsViewModel2;

      @KeepFieldType
      LessonsViewModel com_eduplatform_ui_lessons_LessonsViewModel2;

      @KeepFieldType
      AuthViewModel com_eduplatform_ui_auth_AuthViewModel2;

      @KeepFieldType
      TeacherViewModel com_eduplatform_ui_teacher_TeacherViewModel2;
    }
  }

  private static final class ViewModelCImpl extends EduPlatformApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AdminViewModel> adminViewModelProvider;

    private Provider<AuthViewModel> authViewModelProvider;

    private Provider<CoursesViewModel> coursesViewModelProvider;

    private Provider<LessonsViewModel> lessonsViewModelProvider;

    private Provider<ProgressViewModel> progressViewModelProvider;

    private Provider<TeacherViewModel> teacherViewModelProvider;

    private Provider<TestViewModel> testViewModelProvider;

    private Provider<TopicsViewModel> topicsViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.adminViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.authViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.coursesViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.lessonsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.progressViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.teacherViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.testViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.topicsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(8).put(LazyClassKeyProvider.com_eduplatform_ui_admin_AdminViewModel, ((Provider) adminViewModelProvider)).put(LazyClassKeyProvider.com_eduplatform_ui_auth_AuthViewModel, ((Provider) authViewModelProvider)).put(LazyClassKeyProvider.com_eduplatform_ui_courses_CoursesViewModel, ((Provider) coursesViewModelProvider)).put(LazyClassKeyProvider.com_eduplatform_ui_lessons_LessonsViewModel, ((Provider) lessonsViewModelProvider)).put(LazyClassKeyProvider.com_eduplatform_ui_progress_ProgressViewModel, ((Provider) progressViewModelProvider)).put(LazyClassKeyProvider.com_eduplatform_ui_teacher_TeacherViewModel, ((Provider) teacherViewModelProvider)).put(LazyClassKeyProvider.com_eduplatform_ui_tests_TestViewModel, ((Provider) testViewModelProvider)).put(LazyClassKeyProvider.com_eduplatform_ui_topics_TopicsViewModel, ((Provider) topicsViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_eduplatform_ui_progress_ProgressViewModel = "com.eduplatform.ui.progress.ProgressViewModel";

      static String com_eduplatform_ui_auth_AuthViewModel = "com.eduplatform.ui.auth.AuthViewModel";

      static String com_eduplatform_ui_teacher_TeacherViewModel = "com.eduplatform.ui.teacher.TeacherViewModel";

      static String com_eduplatform_ui_admin_AdminViewModel = "com.eduplatform.ui.admin.AdminViewModel";

      static String com_eduplatform_ui_tests_TestViewModel = "com.eduplatform.ui.tests.TestViewModel";

      static String com_eduplatform_ui_topics_TopicsViewModel = "com.eduplatform.ui.topics.TopicsViewModel";

      static String com_eduplatform_ui_lessons_LessonsViewModel = "com.eduplatform.ui.lessons.LessonsViewModel";

      static String com_eduplatform_ui_courses_CoursesViewModel = "com.eduplatform.ui.courses.CoursesViewModel";

      @KeepFieldType
      ProgressViewModel com_eduplatform_ui_progress_ProgressViewModel2;

      @KeepFieldType
      AuthViewModel com_eduplatform_ui_auth_AuthViewModel2;

      @KeepFieldType
      TeacherViewModel com_eduplatform_ui_teacher_TeacherViewModel2;

      @KeepFieldType
      AdminViewModel com_eduplatform_ui_admin_AdminViewModel2;

      @KeepFieldType
      TestViewModel com_eduplatform_ui_tests_TestViewModel2;

      @KeepFieldType
      TopicsViewModel com_eduplatform_ui_topics_TopicsViewModel2;

      @KeepFieldType
      LessonsViewModel com_eduplatform_ui_lessons_LessonsViewModel2;

      @KeepFieldType
      CoursesViewModel com_eduplatform_ui_courses_CoursesViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.eduplatform.ui.admin.AdminViewModel 
          return (T) new AdminViewModel(singletonCImpl.eduRepositoryProvider.get());

          case 1: // com.eduplatform.ui.auth.AuthViewModel 
          return (T) new AuthViewModel(singletonCImpl.eduRepositoryProvider.get(), singletonCImpl.tokenStorageProvider.get());

          case 2: // com.eduplatform.ui.courses.CoursesViewModel 
          return (T) new CoursesViewModel(singletonCImpl.eduRepositoryProvider.get());

          case 3: // com.eduplatform.ui.lessons.LessonsViewModel 
          return (T) new LessonsViewModel(singletonCImpl.eduRepositoryProvider.get());

          case 4: // com.eduplatform.ui.progress.ProgressViewModel 
          return (T) new ProgressViewModel(singletonCImpl.eduRepositoryProvider.get());

          case 5: // com.eduplatform.ui.teacher.TeacherViewModel 
          return (T) new TeacherViewModel(singletonCImpl.eduRepositoryProvider.get());

          case 6: // com.eduplatform.ui.tests.TestViewModel 
          return (T) new TestViewModel(singletonCImpl.eduRepositoryProvider.get());

          case 7: // com.eduplatform.ui.topics.TopicsViewModel 
          return (T) new TopicsViewModel(singletonCImpl.eduRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends EduPlatformApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends EduPlatformApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends EduPlatformApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<TokenStorage> tokenStorageProvider;

    private Provider<AuthInterceptor> authInterceptorProvider;

    private Provider<PersistentCookieJar> persistentCookieJarProvider;

    private Provider<SessionExpiredInterceptor> sessionExpiredInterceptorProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<Retrofit> provideRetrofitProvider;

    private Provider<EduApiService> provideApiServiceProvider;

    private Provider<EduRepository> eduRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.tokenStorageProvider = DoubleCheck.provider(new SwitchingProvider<TokenStorage>(singletonCImpl, 2));
      this.authInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<AuthInterceptor>(singletonCImpl, 1));
      this.persistentCookieJarProvider = DoubleCheck.provider(new SwitchingProvider<PersistentCookieJar>(singletonCImpl, 4));
      this.sessionExpiredInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<SessionExpiredInterceptor>(singletonCImpl, 3));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 0));
      this.provideRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 7));
      this.provideApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<EduApiService>(singletonCImpl, 6));
      this.eduRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<EduRepository>(singletonCImpl, 5));
    }

    @Override
    public void injectEduPlatformApp(EduPlatformApp eduPlatformApp) {
      injectEduPlatformApp2(eduPlatformApp);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private EduPlatformApp injectEduPlatformApp2(EduPlatformApp instance) {
      EduPlatformApp_MembersInjector.injectOkHttpClient(instance, provideOkHttpClientProvider.get());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // okhttp3.OkHttpClient 
          return (T) AppModule_ProvideOkHttpClientFactory.provideOkHttpClient(singletonCImpl.authInterceptorProvider.get(), singletonCImpl.sessionExpiredInterceptorProvider.get(), singletonCImpl.persistentCookieJarProvider.get());

          case 1: // com.eduplatform.data.api.AuthInterceptor 
          return (T) new AuthInterceptor(singletonCImpl.tokenStorageProvider.get());

          case 2: // com.eduplatform.data.api.TokenStorage 
          return (T) new TokenStorage(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.eduplatform.data.api.SessionExpiredInterceptor 
          return (T) new SessionExpiredInterceptor(singletonCImpl.tokenStorageProvider.get(), singletonCImpl.persistentCookieJarProvider.get());

          case 4: // com.eduplatform.data.api.PersistentCookieJar 
          return (T) new PersistentCookieJar(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 5: // com.eduplatform.data.repository.EduRepository 
          return (T) new EduRepository(singletonCImpl.provideApiServiceProvider.get(), singletonCImpl.tokenStorageProvider.get(), singletonCImpl.persistentCookieJarProvider.get());

          case 6: // com.eduplatform.data.api.EduApiService 
          return (T) AppModule_ProvideApiServiceFactory.provideApiService(singletonCImpl.provideRetrofitProvider.get());

          case 7: // retrofit2.Retrofit 
          return (T) AppModule_ProvideRetrofitFactory.provideRetrofit(singletonCImpl.provideOkHttpClientProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}

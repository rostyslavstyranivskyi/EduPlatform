package com.eduplatform.data.api;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class PersistentCookieJar_Factory implements Factory<PersistentCookieJar> {
  private final Provider<Context> contextProvider;

  public PersistentCookieJar_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PersistentCookieJar get() {
    return newInstance(contextProvider.get());
  }

  public static PersistentCookieJar_Factory create(Provider<Context> contextProvider) {
    return new PersistentCookieJar_Factory(contextProvider);
  }

  public static PersistentCookieJar newInstance(Context context) {
    return new PersistentCookieJar(context);
  }
}

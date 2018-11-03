package dagger.reflect;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import dagger.Binds;
import dagger.BindsOptionalOf;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoMap;
import dagger.multibindings.IntoSet;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

import static dagger.reflect.DaggerReflect.notImplemented;
import static dagger.reflect.Reflection.findQualifier;
import static dagger.reflect.Reflection.findScope;
import static java.lang.reflect.Modifier.ABSTRACT;
import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.STATIC;

final class ReflectiveModuleParser {
  static void parse(Class<?> moduleClass, @Nullable Object instance,
      BindingGraph.Builder graphBuilder) {
    Class<?> target = moduleClass;
    while (target != Object.class) {
      for (Method method : target.getDeclaredMethods()) {
        if ((method.getModifiers() & PRIVATE) != 0) {
          throw new IllegalArgumentException("Private module methods are not allowed: " + method);
        }

        Binding<?> binding;
        Type type = method.getGenericReturnType();

        if ((method.getModifiers() & ABSTRACT) != 0) {
          if (method.getAnnotation(Binds.class) != null) {
            binding = new Binding.UnlinkedBinds(method);
          } else if (method.getAnnotation(BindsOptionalOf.class) != null) {
            throw notImplemented("@BindsOptionalOf");
          } else {
            continue;
          }
        } else {
          if ((method.getModifiers() & STATIC) == 0 && instance == null) {
            throw new IllegalArgumentException(
                "Module methods must be static or abstract: " + method);
          }

          if (method.getAnnotation(Provides.class) != null) {
            binding = new Binding.UnlinkedProvides(instance, method);
          } else {
            continue;
          }
        }

        if (method.getAnnotation(IntoSet.class) != null) {
          type = setOf(TypeToken.of(type)).getType();
          binding = new Binding.UnlinkedIntoSet(binding);
        }
        if (method.getAnnotation(ElementsIntoSet.class) != null) {
          throw notImplemented("@ElementsIntoSet");
        }
        if (method.getAnnotation(IntoMap.class) != null) {
          throw notImplemented("@IntoMap");
        }

        Annotation[] annotations = method.getAnnotations();
        Key key = Key.of(findQualifier(annotations), type);

        Annotation scope = findScope(annotations);
        if (scope != null) {
          // TODO check correct scope against BindingGraph.
          throw notImplemented("Scoped bindings");
        }

        graphBuilder.add(key, binding);
      }
      target = target.getSuperclass();
    }
  }

  @SuppressWarnings("UnstableApiUsage") // TODO replace with something more lightweight
  static <E> TypeToken<Set<E>> setOf(TypeToken<E> elementType) {
    return new TypeToken<Set<E>>() {}
        .where(new TypeParameter<E>() {}, elementType);
  }
}

package com.example;

import dagger.internal.DaggerCodegen;
import dagger.reflect.DaggerReflect;

enum Frontend {
  REFLECT {
    @Override <C> C create(Class<C> componentClass) {
      return DaggerReflect.create(componentClass);
    }

    @Override <C, B> B builder(Class<C> componentClass, Class<B> builderClass) {
      return DaggerReflect.builder(componentClass, builderClass);
    }
  },
  CODEGEN {
    @Override <C> C create(Class<C> componentClass) {
      return DaggerCodegen.create(componentClass);
    }

    @Override <C, B> B builder(Class<C> componentClass, Class<B> builderClass) {
      return DaggerCodegen.builder(componentClass, builderClass);
    }
  };

  abstract <C> C create(Class<C> componentClass);
  abstract <C, B> B builder(Class<C> componentClass, Class<B> builderClass);
}

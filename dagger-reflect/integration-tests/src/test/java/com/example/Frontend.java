package com.example;

import dagger.internal.DaggerCodegen;
import dagger.reflect.DaggerReflect;

enum Frontend {
  REFLECT {
    @Override <C> C create(Class<C> componentClass) {
      return DaggerReflect.create(componentClass);
    }

    @Override <B> B builder(Class<B> builderClass) {
      return DaggerReflect.builder(builderClass);
    }
  },
  CODEGEN {
    @Override <C> C create(Class<C> componentClass) {
      return DaggerCodegen.create(componentClass);
    }

    @Override <B> B builder(Class<B> builderClass) {
      return DaggerCodegen.builder(builderClass);
    }
  };

  abstract <C> C create(Class<C> componentClass);
  abstract <B> B builder(Class<B> builderClass);
}

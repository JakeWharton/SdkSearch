package dagger.reflect;

import java.io.IOException;
import java.lang.annotation.Annotation;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

public class ReflectionTest {

  private static Annotation qualifier() {
    return Annotations.named("qualifier" + Math.random());
  }

  private static Annotation scope() {
    return Annotations.singleton();
  }

  /**
   * Nothing special (not qualifier, not scope, not related to Dagger).
   */
  private static Annotation other() {
    return Annotations.test();
  }

  @Test public void findQualifier_empty() {
    Annotation result = Reflection.findQualifier(new Annotation[0]);

    assertThat(result).isNull();
  }

  @Test public void findQualifier_single() {
    Annotation qualifier = qualifier();

    Annotation result = Reflection.findQualifier(new Annotation[]{qualifier});

    assertThat(result).isEqualTo(qualifier);
  }

  @Test public void findQualifier_none() {
    Annotation result = Reflection.findQualifier(new Annotation[]{other()});

    assertThat(result).isNull();
  }

  @Test public void findQualifier_unique() {
    Annotation qualifier = qualifier();

    Annotation result = Reflection.findQualifier(new Annotation[]{other(), qualifier, other()});

    assertThat(result).isEqualTo(qualifier);
  }

  @Test public void findQualifier_duplicate() {
    Annotation qualifier1 = qualifier();
    Annotation qualifier2 = qualifier();

    try {
      Reflection.findQualifier(new Annotation[]{qualifier1, other(), qualifier2});
      fail();
    } catch (IllegalArgumentException ex) {
      assertThat(ex).hasMessageThat().contains(qualifier1.toString());
      assertThat(ex).hasMessageThat().contains(qualifier2.toString());
    }
  }

  @Test public void findScope_empty() {
    Annotation result = Reflection.findScope(new Annotation[0]);

    assertThat(result).isNull();
  }

  @Test public void findScope_single() {
    Annotation scope = scope();

    Annotation result = Reflection.findScope(new Annotation[]{scope});

    assertThat(result).isEqualTo(scope);
  }

  @Test public void findScope_none() {
    Annotation result = Reflection.findScope(new Annotation[]{other()});

    assertThat(result).isNull();
  }

  @Test public void findScope_unique() {
    Annotation scope = scope();

    Annotation result = Reflection.findScope(new Annotation[]{other(), scope, other()});

    assertThat(result).isEqualTo(scope);
  }

  @Test public void findScope_duplicate() {
    Annotation scope1 = scope();
    Annotation scope2 = scope();

    try {
      Reflection.findScope(new Annotation[]{scope1, other(), scope2});
      fail();
    } catch (IllegalArgumentException ex) {
      assertThat(ex).hasMessageThat().contains(scope1.toString());
      assertThat(ex).hasMessageThat().contains(scope2.toString());
    }
  }

  static class FieldsInstance {
    public String publicField = "original";
    protected String protectedField = "original";
    String defaultField = "original";
    private String privateField = "original";
  }

  @Test public void trySet_instance() throws NoSuchFieldException {
    FieldsInstance instance = new FieldsInstance();

    Reflection.trySet(instance, FieldsInstance.class.getDeclaredField("publicField"), "public");
    Reflection.trySet(instance, FieldsInstance.class.getDeclaredField("protectedField"), "protected");
    Reflection.trySet(instance, FieldsInstance.class.getDeclaredField("defaultField"), "default");
    Reflection.trySet(instance, FieldsInstance.class.getDeclaredField("privateField"), "private");

    assertThat(instance.publicField).isEqualTo("public");
    assertThat(instance.protectedField).isEqualTo("protected");
    assertThat(instance.defaultField).isEqualTo("default");
    assertThat(instance.privateField).isEqualTo("private");
  }

  static class FieldsStatic {
    public static String publicField = "original";
    protected static String protectedField = "original";
    static String defaultField = "original";
    private static String privateField = "original";
  }

  @Test public void trySet_static() throws NoSuchFieldException {
    Reflection.trySet(null, FieldsStatic.class.getDeclaredField("publicField"), "public");
    Reflection.trySet(null, FieldsStatic.class.getDeclaredField("protectedField"), "protected");
    Reflection.trySet(null, FieldsStatic.class.getDeclaredField("defaultField"), "default");
    Reflection.trySet(null, FieldsStatic.class.getDeclaredField("privateField"), "private");

    assertThat(FieldsStatic.publicField).isEqualTo("public");
    assertThat(FieldsStatic.protectedField).isEqualTo("protected");
    assertThat(FieldsStatic.defaultField).isEqualTo("default");
    assertThat(FieldsStatic.privateField).isEqualTo("private");
  }

  static class MethodsInstance {
    static String ARG_S = "example arg";
    static int ARG_I = 42;
    static double RET = 3.14;

    boolean publicInvoked = false;
    public double publicMethod(String s, int i) {
      assertThat(s).isEqualTo(ARG_S);
      assertThat(i).isEqualTo(ARG_I);
      publicInvoked = true;
      return RET;
    }

    boolean protectedInvoked = false;
    protected double protectedMethod(String s, int i) {
      assertThat(s).isEqualTo(ARG_S);
      assertThat(i).isEqualTo(ARG_I);
      protectedInvoked = true;
      return RET;
    }

    boolean defaultInvoked = false;
    double defaultMethod(String s, int i) {
      assertThat(s).isEqualTo(ARG_S);
      assertThat(i).isEqualTo(ARG_I);
      defaultInvoked = true;
      return RET;
    }

    boolean privateInvoked = false;
    private double privateMethod(String s, int i) {
      assertThat(s).isEqualTo(ARG_S);
      assertThat(i).isEqualTo(ARG_I);
      privateInvoked = true;
      return RET;
    }

    boolean throwsInvoked = false;
    private double throwsMethod(String s, int i) throws IOException {
      assertThat(s).isEqualTo(ARG_S);
      assertThat(i).isEqualTo(ARG_I);
      throwsInvoked = true;
      throw new IOException("test");
    }
  }

  @Test public void tryInvoke_instance() throws NoSuchMethodException {
    MethodsInstance instance = new MethodsInstance();

    Object[] args = {MethodsInstance.ARG_S, MethodsInstance.ARG_I};
    Object publicReturn = Reflection.tryInvoke(instance, MethodsInstance.class.getDeclaredMethod(
        "publicMethod", String.class, int.class), args);
    Object protectedReturn = Reflection.tryInvoke(instance, MethodsInstance.class.getDeclaredMethod(
        "protectedMethod", String.class, int.class), args);
    Object defaultReturn = Reflection.tryInvoke(instance, MethodsInstance.class.getDeclaredMethod(
        "defaultMethod", String.class, int.class), args);
    Object privateReturn = Reflection.tryInvoke(instance, MethodsInstance.class.getDeclaredMethod(
        "privateMethod", String.class, int.class), args);

    assertThat(instance.publicInvoked).isTrue();
    assertThat(publicReturn).isEqualTo(MethodsInstance.RET);

    assertThat(instance.protectedInvoked).isTrue();
    assertThat(protectedReturn).isEqualTo(MethodsInstance.RET);

    assertThat(instance.defaultInvoked).isTrue();
    assertThat(defaultReturn).isEqualTo(MethodsInstance.RET);

    assertThat(instance.privateInvoked).isTrue();
    assertThat(privateReturn).isEqualTo(MethodsInstance.RET);
  }

  @Test public void tryInvoke_instance_throws() throws NoSuchMethodException {
    MethodsInstance instance = new MethodsInstance();
    try {
      Reflection.tryInvoke(instance, MethodsInstance.class.getDeclaredMethod(
          "throwsMethod", String.class, int.class), MethodsStatic.ARG_S, MethodsStatic.ARG_I);
      fail();
    } catch (RuntimeException ex) {
      assertThat(ex).hasCauseThat().hasMessageThat().isEqualTo("test");
    }
  }

  @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
  static class MethodsStatic {
    static String ARG_S = "example arg";
    static int ARG_I = 42;
    static double RET = 3.14;

    static boolean publicInvoked = false;
    public static double publicMethod(String s, int i) {
      assertThat(s).isEqualTo(ARG_S);
      assertThat(i).isEqualTo(ARG_I);
      publicInvoked = true;
      return RET;
    }

    static boolean protectedInvoked = false;
    protected static double protectedMethod(String s, int i) {
      assertThat(s).isEqualTo(ARG_S);
      assertThat(i).isEqualTo(ARG_I);
      protectedInvoked = true;
      return RET;
    }

    static boolean defaultInvoked = false;
    static double defaultMethod(String s, int i) {
      assertThat(s).isEqualTo(ARG_S);
      assertThat(i).isEqualTo(ARG_I);
      defaultInvoked = true;
      return RET;
    }

    static boolean privateInvoked = false;
    private static double privateMethod(String s, int i) {
      assertThat(s).isEqualTo(ARG_S);
      assertThat(i).isEqualTo(ARG_I);
      privateInvoked = true;
      return RET;
    }

    static boolean throwsInvoked = false;
    private static double throwsMethod(String s, int i) throws IOException {
      assertThat(s).isEqualTo(ARG_S);
      assertThat(i).isEqualTo(ARG_I);
      throwsInvoked = true;
      throw new IOException("test");
    }
  }

  @Test public void tryInvoke_static() throws NoSuchMethodException {
    Object[] args = {MethodsStatic.ARG_S, MethodsStatic.ARG_I};
    Object publicReturn = Reflection.tryInvoke(null, MethodsStatic.class.getDeclaredMethod(
        "publicMethod", String.class, int.class), args);
    Object protectedReturn = Reflection.tryInvoke(null, MethodsStatic.class.getDeclaredMethod(
        "protectedMethod", String.class, int.class), args);
    Object defaultReturn = Reflection.tryInvoke(null, MethodsStatic.class.getDeclaredMethod(
        "defaultMethod", String.class, int.class), args);
    Object privateReturn = Reflection.tryInvoke(null, MethodsStatic.class.getDeclaredMethod(
        "privateMethod", String.class, int.class), args);

    assertThat(MethodsStatic.publicInvoked).isTrue();
    assertThat(publicReturn).isEqualTo(MethodsStatic.RET);

    assertThat(MethodsStatic.protectedInvoked).isTrue();
    assertThat(protectedReturn).isEqualTo(MethodsStatic.RET);

    assertThat(MethodsStatic.defaultInvoked).isTrue();
    assertThat(defaultReturn).isEqualTo(MethodsStatic.RET);

    assertThat(MethodsStatic.privateInvoked).isTrue();
    assertThat(privateReturn).isEqualTo(MethodsStatic.RET);
  }

  @Test public void tryInvoke_static_throws() throws NoSuchMethodException {
    try {
      Reflection.tryInvoke(null, MethodsStatic.class.getDeclaredMethod(
          "throwsMethod", String.class, int.class), MethodsStatic.ARG_S, MethodsStatic.ARG_I);
      fail();
    } catch (RuntimeException ex) {
      assertThat(ex).hasCauseThat().hasMessageThat().isEqualTo("test");
    }
  }

  static class Constructors {
    static String ARG_S = "example arg";
    static int ARG_I = 42;

    static class Public {
      boolean invoked;
      public Public(String s, int i) {
        assertThat(s).isEqualTo(ARG_S);
        assertThat(i).isEqualTo(ARG_I);
        invoked = true;
      }
    }

    static class Protected {
      boolean invoked;
      protected Protected(String s, int i) {
        assertThat(s).isEqualTo(ARG_S);
        assertThat(i).isEqualTo(ARG_I);
        invoked = true;
      }
    }

    static class Default {
      boolean invoked;
      Default(String s, int i) {
        assertThat(s).isEqualTo(ARG_S);
        assertThat(i).isEqualTo(ARG_I);
        invoked = true;
      }
    }

    static class Private {
      boolean invoked;
      private Private(String s, int i) {
        assertThat(s).isEqualTo(ARG_S);
        assertThat(i).isEqualTo(ARG_I);
        invoked = true;
      }
    }

    static class Throws {
      private Throws(String s, int i) throws IOException {
        assertThat(s).isEqualTo(ARG_S);
        assertThat(i).isEqualTo(ARG_I);
        throw new IOException("test");
      }
    }
  }

  @Test public void tryInstantiate() throws NoSuchMethodException {
    Object[] args = {Constructors.ARG_S, Constructors.ARG_I};
    Constructors.Public publicInstance = Reflection.tryInstantiate(
        Constructors.Public.class.getDeclaredConstructor(String.class, int.class), args);
    Constructors.Protected protectedInstance = Reflection.tryInstantiate(
        Constructors.Protected.class.getDeclaredConstructor(String.class, int.class), args);
    Constructors.Default defaultInstance = Reflection.tryInstantiate(
        Constructors.Default.class.getDeclaredConstructor(String.class, int.class), args);
    Constructors.Private privateInstance = Reflection.tryInstantiate(
        Constructors.Private.class.getDeclaredConstructor(String.class, int.class), args);

    assertThat(publicInstance.invoked).isTrue();
    assertThat(protectedInstance.invoked).isTrue();
    assertThat(defaultInstance.invoked).isTrue();
    assertThat(privateInstance.invoked).isTrue();
  }

  @Test public void tryInstantiate_throws() throws NoSuchMethodException {
    try {
      Reflection.tryInstantiate(
          Constructors.Throws.class.getDeclaredConstructor(String.class, int.class),
          new Object[]{Constructors.ARG_S, Constructors.ARG_I});
      fail();
    } catch (RuntimeException ex) {
      assertThat(ex).hasCauseThat().hasMessageThat().isEqualTo("test");
    }
  }
}

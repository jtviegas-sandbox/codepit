/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.xbean.finder.Annotated;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.AsynchronousInheritanceAnnotationFinder;
import org.apache.xbean.finder.IAnnotationFinder;
import org.apache.xbean.finder.UrlSet;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.apache.xbean.finder.archive.ClasspathArchive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class FinderFactory {

    private static final FinderFactory factory = new FinderFactory();
    public static final String TOMEE_JAXRS_DEPLOY_UNDECLARED_PROP = "tomee.jaxrs.deploy.undeclared";
    public static final String ASYNC_SCAN = "openejb.scanning.inheritance.asynchronous";
    public static final String SKIP_LINK = "openejb.finder.skip.link";
    public static final String FORCE_LINK = "openejb.finder.force.link";

    private static FinderFactory get() {
        final FinderFactory factory = SystemInstance.get().getComponent(FinderFactory.class);
        return factory != null ? factory: FinderFactory.factory;
    }

    public static IAnnotationFinder createFinder(final DeploymentModule module) throws Exception {
        return get().create(module);
    }

    public static AnnotationFinder getFinder(final ClassLoader classLoader, final URL url) {
        return newFinder(ClasspathArchive.archive(classLoader, url));
    }

    public IAnnotationFinder create(final DeploymentModule module) throws Exception {
        final AnnotationFinder finder;
        if (module instanceof WebModule) {
            final WebModule webModule = (WebModule) module;
            final AnnotationFinder annotationFinder = newFinder(new WebappAggregatedArchive(webModule, webModule.getScannableUrls()));
            enableFinderOptions(annotationFinder);
            finder = annotationFinder;
        } else if (module instanceof ConnectorModule) {
            final ConnectorModule connectorModule = (ConnectorModule) module;
            finder = newFinder(new ConfigurableClasspathArchive(connectorModule, connectorModule.getLibraries())).link();
        } else if (module instanceof AppModule) {
            final Collection<URL> urls = NewLoaderLogic.applyBuiltinExcludes(new UrlSet(AppModule.class.cast(module).getAdditionalLibraries())).getUrls();
            finder = newFinder(new WebappAggregatedArchive(module.getClassLoader(), module.getAltDDs(), urls));
        } else if (module.getJarLocation() != null) {
            final String location = module.getJarLocation();
            final File file = new File(location);

            URL url;
            if (file.exists()) {
                url = file.toURI().toURL();

                final File webInfClassesFolder = new File(file, "WEB-INF/classes"); // is it possible?? normally no
                if (webInfClassesFolder.exists() && webInfClassesFolder.isDirectory()) {
                    url = webInfClassesFolder.toURI().toURL();
                }
            } else {
                url = new URL(location);
            }

            if (module instanceof Module) {
                final DebugArchive archive = new DebugArchive(new ConfigurableClasspathArchive((Module) module, url));
                finder = newFinder(archive);
            } else {
                finder = newFinder(new DebugArchive(new ConfigurableClasspathArchive(module.getClassLoader(), url)));
            }
            if ("true".equals(SystemInstance.get().getProperty(FORCE_LINK, module.getProperties().getProperty(FORCE_LINK, "false")))) {
                finder.link();
            } else {
                finder.enableMetaAnnotations(); // needed to stay compliant
                enableSubclassing(finder);
            }
        } else {
            finder = new AnnotationFinder(new ClassesArchive());
        }

        return new ModuleLimitedFinder(finder);
    }

    private static AnnotationFinder newFinder(final Archive archive) {
        if ("true".equals(SystemInstance.get().getProperty(ASYNC_SCAN, "true"))) {
            return new AsynchronousInheritanceAnnotationFinder(archive);
        }
        return new AnnotationFinder(archive);
    }

    public static final class DebugArchive implements Archive {
        private final Archive archive;

        private DebugArchive(final Archive archive) {
            this.archive = archive;
        }

        @Override
        public Iterator<Entry> iterator() {
            return archive.iterator();
        }

        @Override
        public InputStream getBytecode(final String s) throws IOException, ClassNotFoundException {
            return archive.getBytecode(s);
        }

        @Override
        public Class<?> loadClass(final String s) throws ClassNotFoundException {
            try {
                return archive.loadClass(s);
            } catch (final ClassNotFoundException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    public static AnnotationFinder enableFinderOptions(final AnnotationFinder annotationFinder) {
        if (annotationFinder.hasMetaAnnotations()) {
            annotationFinder.enableMetaAnnotations();
        }
        enableSubclassing(annotationFinder);

        return annotationFinder;
    }

    private static void enableSubclassing(final AnnotationFinder annotationFinder) {
        if (enableFindSubclasses()) {
            // for @HandleTypes we need interface impl, impl of abstract classes too
            annotationFinder.enableFindSubclasses();
            annotationFinder.enableFindImplementations();
        }
    }

    private static boolean enableFindSubclasses() {
        return SystemInstance.get().getOptions().get(FORCE_LINK, false)
            || !SystemInstance.get().getOptions().get(SKIP_LINK, false)
                && (isTomEE() || isJaxRsInstalled() && SystemInstance.get().getOptions().get(TOMEE_JAXRS_DEPLOY_UNDECLARED_PROP, false));
    }

    public static boolean isTomEE() {
        try { // since Tomcat 7.0.47
            FinderFactory.class.getClassLoader().loadClass("javax.websocket.Endpoint");
            return true;
        } catch (final Throwable e) {
            return false;
        }
    }

    public static boolean isJaxRsInstalled() {
        try {
            FinderFactory.class.getClassLoader().loadClass("org.apache.openejb.server.rest.RsRegistry");
            return true;
        } catch (final Throwable e) {
            return false;
        }
    }

    public static class ModuleLimitedFinder implements IAnnotationFinder {
        private final IAnnotationFinder delegate;

        public ModuleLimitedFinder(final IAnnotationFinder delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean isAnnotationPresent(final Class<? extends Annotation> annotation) {
            return delegate.isAnnotationPresent(annotation);
        }

        @Override
        public List<String> getClassesNotLoaded() {
            return delegate.getClassesNotLoaded();
        }

        @Override
        public List<Package> findAnnotatedPackages(final Class<? extends Annotation> annotation) {
            return delegate.findAnnotatedPackages(annotation);
        }

        @Override
        public List<Class<?>> findAnnotatedClasses(final Class<? extends Annotation> annotation) {
            try {
                return filter(delegate.findAnnotatedClasses(annotation), new ClassPredicate<Object>(getAnnotatedClassNames()));
            } catch (final TypeNotPresentException tnpe) {
                throw handleException(tnpe, annotation);
            }
        }

        private RuntimeException handleException(final TypeNotPresentException tnpe, final Class<? extends Annotation> annotation) {
            try {
                final Method mtd = AnnotationFinder.class.getDeclaredMethod("getAnnotationInfos", String.class);
                mtd.setAccessible(true);
                final List<?> infos = (List<?>) mtd.invoke(delegate);
                for (final Object info : infos) {
                    if (info instanceof AnnotationFinder.ClassInfo) {
                        final AnnotationFinder.ClassInfo classInfo = (AnnotationFinder.ClassInfo) info;
                        try {
                            // can throw the exception
                            classInfo.get().isAnnotationPresent(annotation);
                        } catch (final TypeNotPresentException tnpe2) {
                            throw new OpenEJBRuntimeException("Missing type for annotation " + annotation.getName() + " on class " + classInfo.getName(), tnpe2);
                        } catch (final ThreadDeath ignored) {
                            // no-op
                        }
                    }
                }
            } catch (final Throwable th) {
                // no-op
            }
            return tnpe;
        }

        @Override
        public List<Class<?>> findInheritedAnnotatedClasses(final Class<? extends Annotation> annotation) {
            return filter(delegate.findInheritedAnnotatedClasses(annotation), new ClassPredicate<Object>(getAnnotatedClassNames()));
        }

        @Override
        public List<Method> findAnnotatedMethods(final Class<? extends Annotation> annotation) {
            return filter(delegate.findAnnotatedMethods(annotation), new MethodPredicate(getAnnotatedClassNames()));
        }

        @Override
        public List<Constructor> findAnnotatedConstructors(final Class<? extends Annotation> annotation) {
            return filter(delegate.findAnnotatedConstructors(annotation), new ConstructorPredicate(getAnnotatedClassNames()));
        }

        @Override
        public List<Field> findAnnotatedFields(final Class<? extends Annotation> annotation) {
            return filter(delegate.findAnnotatedFields(annotation), new FieldPredicate(getAnnotatedClassNames()));
        }

        @Override
        public List<Class<?>> findClassesInPackage(final String packageName, final boolean recursive) {
            return filter(delegate.findClassesInPackage(packageName, recursive), new ClassPredicate<Object>(getAnnotatedClassNames()));
        }

        @Override
        public <T> List<Class<? extends T>> findSubclasses(final Class<T> clazz) {
            return filter(delegate.findSubclasses(clazz), new ClassPredicate<T>(getAnnotatedClassNames()));
        }

        @Override
        public <T> List<Class<? extends T>> findImplementations(final Class<T> clazz) {
            return filter(delegate.findImplementations(clazz), new ClassPredicate<T>(getAnnotatedClassNames()));
        }

        @Override
        public List<Annotated<Method>> findMetaAnnotatedMethods(final Class<? extends Annotation> annotation) {
            return filter(delegate.findMetaAnnotatedMethods(annotation), new AnnotatedMethodPredicate(getAnnotatedClassNames()));
        }

        @Override
        public List<Annotated<Field>> findMetaAnnotatedFields(final Class<? extends Annotation> annotation) {
            return filter(delegate.findMetaAnnotatedFields(annotation), new AnnotatedFieldPredicate(getAnnotatedClassNames()));
        }

        @Override
        public List<Annotated<Class<?>>> findMetaAnnotatedClasses(final Class<? extends Annotation> annotation) {
            try {
                return filter(delegate.findMetaAnnotatedClasses(annotation), new AnnotatedClassPredicate(getAnnotatedClassNames()));
            } catch (final TypeNotPresentException tnpe) {
                throw handleException(tnpe, annotation);
            }
        }

        @Override
        public List<String> getAnnotatedClassNames() {
            return delegate.getAnnotatedClassNames();
        }

        private static <T> List<T> filter(final List<T> list, final Predicate<T> predicate) {
            final List<T> ts = new ArrayList<T>();
            for (final T t : list) {
                if (predicate.accept(t)) {
                    ts.add(t);
                }
            }
            return ts;
        }

        public IAnnotationFinder getDelegate() {
            return delegate;
        }

        private abstract static class Predicate<T> {
            protected final List<String> accepted;

            public Predicate(final List<String> list) {
                accepted = list;
            }

            protected boolean accept(final T t) {
                return accepted.contains(name(t));
            }

            protected abstract String name(T t);
        }

        private static class ClassPredicate<T> extends Predicate<Class<? extends T>> {
            public ClassPredicate(final List<String> list) {
                super(list);
            }

            @Override
            protected String name(final Class<? extends T> aClass) {
                return aClass.getName();
            }
        }

        private static class MethodPredicate extends Predicate<Method> {
            public MethodPredicate(final List<String> list) {
                super(list);
            }

            @Override
            protected String name(final Method method) {
                return method.getDeclaringClass().getName();
            }
        }

        private static class FieldPredicate extends Predicate<Field> {
            public FieldPredicate(final List<String> list) {
                super(list);
            }

            @Override
            protected String name(final Field field) {
                return field.getDeclaringClass().getName();
            }
        }

        private static class ConstructorPredicate extends Predicate<Constructor> {
            public ConstructorPredicate(final List<String> list) {
                super(list);
            }

            @Override
            protected String name(final Constructor constructor) {
                return constructor.getDeclaringClass().getName();
            }
        }

        private static class AnnotatedClassPredicate extends Predicate<Annotated<Class<?>>> {
            public AnnotatedClassPredicate(final List<String> list) {
                super(list);
            }

            @Override
            protected String name(final Annotated<Class<?>> aClass) {
                return aClass.get().getName();
            }
        }

        private static class AnnotatedMethodPredicate extends Predicate<Annotated<Method>> {
            public AnnotatedMethodPredicate(final List<String> list) {
                super(list);
            }

            @Override
            protected String name(final Annotated<Method> method) {
                return method.get().getDeclaringClass().getName();
            }
        }

        private static class AnnotatedFieldPredicate extends Predicate<Annotated<Field>> {
            public AnnotatedFieldPredicate(final List<String> list) {
                super(list);
            }

            @Override
            protected String name(final Annotated<Field> field) {
                return field.get().getDeclaringClass().getName();
            }
        }
    }
}

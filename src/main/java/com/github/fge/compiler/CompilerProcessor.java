package com.github.fge.compiler;

import com.github.fge.jjschema.ClassHolder;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.github.fge.jsonschema.processing.Processor;
import com.github.fge.jsonschema.report.ProcessingMessage;
import com.github.fge.jsonschema.report.ProcessingReport;
import com.github.fge.jsonschema.util.ValueHolder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CompilerProcessor
    implements Processor<ValueHolder<String>, ClassHolder>
{
    private static final JavaCompiler COMPILER
        = ToolProvider.getSystemJavaCompiler();
    private static final StandardJavaFileManager FILE_MANAGER;

    static {
        FILE_MANAGER = COMPILER == null ? null
            : COMPILER.getStandardFileManager(null, Locale.getDefault(),
                Charset.forName("UTF-8"));
    }

    private static final String CANNOT_FIND_COMPILER
        = "cannot find system compiler (do you have a JDK installed?)";
    private static final String CANNOT_FIND_PACKAGENAME
        = "cannot extract package name from source";
    private static final String CANNOT_FIND_CLASSNAME
        = "cannot extract class name from source";
    private static final String CANNOT_BUILD_URI
        = "cannot build URI from class name";
    private static final String COMPILE_FAILURE = "compilation failed";

    private static final Pattern PACKAGE_PATTERN
        = Pattern.compile("^package\\s+(\\w+(\\.\\w+)*);", Pattern.MULTILINE);
    private static final Pattern CLASSNAME_PATTERN
        = Pattern.compile("^public\\s+(?:final\\s+)?class\\s+(\\w+)",
        Pattern.MULTILINE);

    @Override
    public ClassHolder process(final ProcessingReport report,
        final ValueHolder<String> input)
        throws ProcessingException
    {
        /*
         * Check for the presence of the compiler... We don't want to make this
         * check in a static initializer, since it would make the whole package
         * fail.
         */
        if (COMPILER == null)
            throw new CompilingException(CANNOT_FIND_COMPILER);

        final String source = input.getValue();

        /*
         * Extract package name and class name
         */
        final String packageName = extractPkgName(source);
        if (packageName == null)
            throw new CompilingException(CANNOT_FIND_PACKAGENAME);

        final String className = extractClassName(source);
        if (className == null)
            throw new CompilingException(CANNOT_FIND_CLASSNAME);


        final String fullName = packageName + '.' + className;

        /*
         * Create the JavaFileObject necessary for the compiler to run
         */
        final JavaFileObject fileObject;
        try {
            fileObject = new FromStringFileObject(fullName, source);
        } catch (URISyntaxException e) {
            throw new CompilingException(new ProcessingMessage()
                .message(CANNOT_BUILD_URI).put("className", fullName), e);
        }

        /*
         * Create the Iterable
         */
        final Set<JavaFileObject> set = ImmutableSet.of(fileObject);

        /*
         * Create the diagnostic listener
         */
        final DiagnosticsReporting reporting = new DiagnosticsReporting();

        /*
         * Create the compiler output directory and relevant options
         */
        final CompilerOutput output = new CompilerOutput(fullName);

        final List<String> options
            = ImmutableList.of("-d", output.getDirectory());

        final JavaCompiler.CompilationTask task = COMPILER.getTask(
            DevNull.getInstance(), FILE_MANAGER, reporting, options, null, set);

        final Boolean compilationSuccess = task.call();

        report.mergeWith(reporting.getReport());

        if (!compilationSuccess)
            throw new CompilingException(COMPILE_FAILURE);

        return new ClassHolder(output.getGeneratedClass());
    }

    private static String extractPkgName(final String source)
    {
        final Matcher m = PACKAGE_PATTERN.matcher(source);
        return m.find() ? m.group(1) : null;
    }

    private static String extractClassName(final String source)
    {
        final Matcher m = CLASSNAME_PATTERN.matcher(source);
        return m.find() ? m.group(1) : null;
    }
}

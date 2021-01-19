/*******************************************************************************
 * Copyright 2019 Viridian Software Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.viridiansoftware.desugar

import com.google.common.io.Files
import com.google.devtools.build.android.desugar.Desugar
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class DesugarTask extends DefaultTask {
    @Input
    @Optional
    boolean allowEmptyBootclasspath = false;
    @Input
    @Optional
    boolean bestEffortTolerateMissingDeps = true;
    @Input
    @Optional
    boolean copyBridgesFromClasspath = false;
    @Input
    @Optional
    boolean coreLibrary = false;
    @Input
    @Optional
    boolean desugarInterfaceMethodBodiesIfNeeded = true;
    @Input
    @Optional
    boolean desugarSupportedCoreLibs = false;
    @Input
    @Optional
    boolean desugarTryWithResourcesIfNeeded = true;
    @Input
    @Optional
    boolean desugarTryWithResourcesOmitRuntimeClasses = false;
    @Input
    @Optional
    boolean emitDependencyMetadataAsNeeded = false;
    @Input
    @Optional
    boolean legacyJacocoFix = false;
    @Input
    @Optional
    boolean onlyDesugarJavac9ForLint = false;
    @Input
    @Optional
    boolean rewriteCallsToLongCompare = false;
    @Input
    @Optional
    boolean verbose = false;
    @Input
    @Optional
    int minSdkVersion = 19;
    @Input
    @Optional
    List<String> dontRewriteCoreLibraryInvocation;
    @Input
    @Optional
    List<String> emulateCoreLibraryInterface;
    @Input
    @Optional
    List<String> retargetCoreLibraryMember;
    @Input
    @Optional
    List<String> rewriteCoreLibraryPrefix;
    @Input
    @Optional
    List<String> additionalJvmArgs;

    @InputDirectory
    @Optional
    DirectoryProperty inputDir;

    @InputFile
    @Optional
    File inputJar;

    @InputDirectory
    @Optional
    DirectoryProperty lambdaDir;

    FileCollection bootstrapClasspath = getProject().getLayout().files();
    FileCollection classpath = getProject().getLayout().files();

    @OutputDirectory
    @Optional
    DirectoryProperty outputDir;

    @OutputFile
    @Optional
    File outputJar;

    @TaskAction
    public void runTask() {
        if(inputJar == null && getInputDir() == null) {
            throw new GradleException("Either inputJar or inputDir (folder containing classes) must be declared");
        }
        if(outputJar == null && getOutputDir() == null) {
            throw new GradleException("Either outputJar or outputDir (folder for classes) must be declared");
        }

        try {
            List<String> desugarExecArgs = new ArrayList<String>();

            desugarExecArgs.add("--input");
            if(getInputDir() != null && getInputDir().isPresent()) {
                desugarExecArgs.add(getInputDir().get().asFile.getAbsolutePath());
            } else {
                desugarExecArgs.add(inputJar.getAbsolutePath());
            }

            desugarExecArgs.add("--output");
            if(getOutputDir() != null && getOutputDir().isPresent()) {
                desugarExecArgs.add(getOutputDir().get().asFile.getAbsolutePath());
            } else {
                desugarExecArgs.add(outputJar.getAbsolutePath());
            }

            File lambdaDirFile;
            if(lambdaDir != null) {
                lambdaDirFile = lambdaDir.get().asFile;
            } else {
                lambdaDirFile = Files.createTempDir();
            }
            if(!lambdaDirFile.exists()) {
                lambdaDirFile.mkdirs();
            }

            if(getClasspath() != null) {
                for(File classpathFile : getClasspath()) {
                    desugarExecArgs.add("--classpath_entry");
                    desugarExecArgs.add(classpathFile.getAbsolutePath());
                }
            }

            if(getBootstrapClasspath() != null) {
                for(File bootstrapFile : getBootstrapClasspath()) {
                    desugarExecArgs.add("--bootclasspath_entry");
                    desugarExecArgs.add(bootstrapFile.getAbsolutePath());
                }
            }

            desugarExecArgs.add("--min_sdk_version");
            desugarExecArgs.add(String.valueOf(minSdkVersion));

            desugarExecArgs.add("--" + (allowEmptyBootclasspath ? "" : "no") + "allow_empty_bootclasspath");
            desugarExecArgs.add("--" + (bestEffortTolerateMissingDeps ? "" : "no") + "best_effort_tolerate_missing_deps");
            desugarExecArgs.add("--" + (copyBridgesFromClasspath ? "" : "no") + "best_effort_tolerate_missing_deps");
            desugarExecArgs.add("--" + (coreLibrary ? "" : "no") + "core_library");
            desugarExecArgs.add("--" + (desugarInterfaceMethodBodiesIfNeeded ? "" : "no") + "desugar_interface_method_bodies_if_needed");
            desugarExecArgs.add("--" + (desugarSupportedCoreLibs ? "" : "no") + "desugar_supported_core_libs");
            desugarExecArgs.add("--" + (desugarTryWithResourcesIfNeeded ? "" : "no") + "desugar_try_with_resources_if_needed");
            desugarExecArgs.add("--" + (desugarTryWithResourcesOmitRuntimeClasses ? "" : "no") + "desugar_try_with_resources_omit_runtime_classes");
            desugarExecArgs.add("--" + (emitDependencyMetadataAsNeeded ? "" : "no") + "emit_dependency_metadata_as_needed");
            desugarExecArgs.add("--" + (legacyJacocoFix ? "" : "no") + "legacy_jacoco_fix");
            desugarExecArgs.add("--" + (onlyDesugarJavac9ForLint ? "" : "no") + "only_desugar_javac9_for_lint");
            desugarExecArgs.add("--" + (rewriteCallsToLongCompare ? "" : "no") + "rewrite_calls_to_long_compare");
            desugarExecArgs.add("--" + (verbose ? "" : "no") + "verbose");

            for(String dontRewriteInvoke: dontRewriteCoreLibraryInvocation) {
                desugarExecArgs.add("--dont_rewrite_core_library_invocation");
                desugarExecArgs.add(dontRewriteInvoke);
            }
            for(String emulate: emulateCoreLibraryInterface) {
                desugarExecArgs.add("--emulate_core_library_interface");
                desugarExecArgs.add(emulate);
            }
            for(String libraryMember: retargetCoreLibraryMember) {
                desugarExecArgs.add("--retarget_core_library_member");
                desugarExecArgs.add(libraryMember);
            }
            for(String libraryPrefix: rewriteCoreLibraryPrefix) {
                desugarExecArgs.add("--rewrite_core_library_prefix");
                desugarExecArgs.add(libraryPrefix);
            }

            def desugarExecClasspath = project.files(getJavaExecClasspath(project));

            if(additionalJvmArgs == null) {
                additionalJvmArgs = new ArrayList<String>();
            }

            project.javaexec {
                classpath(desugarExecClasspath)
                main = 'com.google.devtools.build.android.desugar.Desugar'
                jvmArgs = additionalJvmArgs
                args = desugarExecArgs
                systemProperties['jdk.internal.lambda.dumpProxyClasses'] = lambdaDirFile.getAbsolutePath()
            }
        } catch(Exception e) {
            e.printStackTrace()
            throw e;
        }
    }

    private List<File> getJavaExecClasspath(project, classpath=[]) {
        if(project == null || project == project.rootProject) {
            classpath.addAll(project.buildscript.configurations.classpath.getFiles())
            classpath
        } else {
            classpath.addAll(project.buildscript.configurations.classpath.getFiles())
            getJavaExecClasspath(project.rootProject, classpath)
        }
    }

    @InputFiles
    public FileCollection getBootstrapClasspath() {
        return this.bootstrapClasspath;
    }

    public void bootstrapClasspath(FileCollection bootstrapClasspath) {
        if(this.bootstrapClasspath == null) {
            this.bootstrapClasspath = bootstrapClasspath;
        } else {
            this.bootstrapClasspath = this.bootstrapClasspath.plus(bootstrapClasspath);
        }
    }

    @InputFiles
    public FileCollection getClasspath() {
        return this.classpath;
    }

    public void classpath(FileCollection classpath) {
        if(this.classpath == null) {
            this.classpath = classpath;
        } else {
            this.classpath = this.classpath.plus(classpath);
        }
    }
}

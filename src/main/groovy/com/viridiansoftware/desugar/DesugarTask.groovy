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
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
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
    ListProperty<String> dontRewriteCoreLibraryInvocation = project.objects.listProperty(String.class);
    @Input
    @Optional
    ListProperty<String> emulateCoreLibraryInterface = project.objects.listProperty(String.class);
    @Input
    @Optional
    ListProperty<String> retargetCoreLibraryMember = project.objects.listProperty(String.class);
    @Input
    @Optional
    ListProperty<String> rewriteCoreLibraryPrefix = project.objects.listProperty(String.class);
    @Input
    @Optional
    ListProperty<String> additionalJvmArgs = project.objects.listProperty(String.class);

    @InputDirectory
    @Optional
    DirectoryProperty inputDir = project.objects.directoryProperty();

    @InputFile
    @Optional
    RegularFileProperty inputJar = project.objects.fileProperty();

    @InputDirectory
    @Optional
    DirectoryProperty lambdaDir = project.objects.directoryProperty();

    @InputFiles
    @Optional
    ConfigurableFileCollection bootstrapClasspath = project.objects.fileCollection();

    @InputFiles
    @Optional
    ConfigurableFileCollection classpath = project.objects.fileCollection();

    @OutputDirectory
    @Optional
    DirectoryProperty outputDir = project.objects.directoryProperty();

    @OutputFile
    @Optional
    RegularFileProperty outputJar = project.objects.fileProperty();

    @TaskAction
    def runTask() {
        try {
            RegularFile inputJar = this.inputJar.getOrNull();
            RegularFile outputJar = this.outputJar.getOrNull();
            Directory inputDir = this.inputDir.getOrNull();
            Directory outputDir = this.outputDir.getOrNull();

            if(inputJar == null && inputDir == null) {
                throw new GradleException("Either inputJar or inputDir (folder containing classes) must be declared");
            }
            if(outputJar == null && outputDir == null) {
                throw new GradleException("Either outputJar or outputDir (folder for classes) must be declared");
            }

            List<String> desugarExecArgs = new ArrayList<String>();

            desugarExecArgs.add("--input");
            if (inputDir != null && inputDir.asFile.exists()) {
                desugarExecArgs.add(inputDir.asFile.getAbsolutePath());
            } else {
                desugarExecArgs.add(inputJar.getAsFile().getAbsolutePath());
            }

            desugarExecArgs.add("--output");
            if (outputDir != null && outputDir.asFile.exists()) {
                desugarExecArgs.add(outputDir.asFile.getAbsolutePath());
            } else {
                desugarExecArgs.add(outputJar.getAsFile().getAbsolutePath());
            }

            Directory lambdaDir = this.lambdaDir.getOrNull();
            File lambdaDirFile;
            if (lambdaDir != null) {
                lambdaDirFile = lambdaDir.asFile;
            } else {
                lambdaDirFile = Files.createTempDir();
            }
            if (!lambdaDirFile.exists()) {
                lambdaDirFile.mkdirs();
            }

            if (getClasspath() != null) {
                for (File classpathFile : getClasspath().getFiles()) {
                    desugarExecArgs.add("--classpath_entry");
                    desugarExecArgs.add(classpathFile.getAbsolutePath());
                }
            }

            if (getBootstrapClasspath() != null) {
                for (File bootstrapFile : getBootstrapClasspath().getFiles()) {
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

            for (String dontRewriteInvoke : dontRewriteCoreLibraryInvocation.getOrElse(new ArrayList<String>())) {
                desugarExecArgs.add("--dont_rewrite_core_library_invocation");
                desugarExecArgs.add(dontRewriteInvoke);
            }
            for (String emulate : emulateCoreLibraryInterface.getOrElse(new ArrayList<String>())) {
                desugarExecArgs.add("--emulate_core_library_interface");
                desugarExecArgs.add(emulate);
            }
            for (String libraryMember : retargetCoreLibraryMember.getOrElse(new ArrayList<String>())) {
                desugarExecArgs.add("--retarget_core_library_member");
                desugarExecArgs.add(libraryMember);
            }
            for (String libraryPrefix : rewriteCoreLibraryPrefix.getOrElse(new ArrayList<String>())) {
                desugarExecArgs.add("--rewrite_core_library_prefix");
                desugarExecArgs.add(libraryPrefix);
            }

            def desugarExecClasspath = project.files(getJavaExecClasspath(project));

            if (additionalJvmArgs == null) {
                additionalJvmArgs = new ArrayList<String>();
            }

            project.javaexec {
                classpath(desugarExecClasspath)
                main = 'com.google.devtools.build.android.desugar.Desugar'
                jvmArgs = additionalJvmArgs.getOrNull()
                args = desugarExecArgs
                systemProperties['jdk.internal.lambda.dumpProxyClasses'] = lambdaDirFile.getAbsolutePath()
            }
        } catch(Exception e) {
            project.logger.lifecycle(e.getMessage())
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
}

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
package com.viridiansoftware.desugar.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class DesugarTask extends DefaultTask {
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

    @InputDirectory
    abstract DirectoryProperty getInputDir()

    @InputDirectory
    abstract ConfigurableFileCollection getBootstrapClasspath()

    @InputDirectory
    abstract ConfigurableFileCollection getClasspath()

    @OutputDirectory
    abstract DirectoryProperty getOutputDir()

    @TaskAction
    public void runTask() {

    }
}

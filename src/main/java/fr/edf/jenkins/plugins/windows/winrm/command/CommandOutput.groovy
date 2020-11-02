/*
 * Copyright (C) 2011-2015 Aestas/IT, 2020 EDF Group and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.edf.jenkins.plugins.windows.winrm.command

/**
 * Results of the command execution:
 *
 * 1. exitCode
 * 2. output of the command execution
 * 3. error output in case of error occurred during command execution
 *
 * @author Andrey Adamovich
 * @author Mathieu Delrocq
 * @see https://github.com/sshoogr/groovy-winrm-client
 */
class CommandOutput {

    public static final String COMMAND_STILL_RUNNING = "The command is still running"

    int exitStatus
    String output
    String errorOutput
    Throwable exception

    CommandOutput(int exitStatus, String output, String errorOutput) {
        this.exitStatus = exitStatus
        this.output = output
        this.errorOutput = errorOutput
    }

    CommandOutput(int exitStatus, String output, String errorOutput, Throwable exception) {
        this.exitStatus = exitStatus
        this.output = output
        this.errorOutput = errorOutput
        this.exception = exception
    }

    /**
     * Convenience method to verify that the
     * command output has failed.
     *
     * @return true if the command has failed.
     */
    boolean failed() {
        this.exitStatus != 0
    }

    boolean isCommandRunning() {
        -1 == exitStatus && COMMAND_STILL_RUNNING == errorOutput
    }
}

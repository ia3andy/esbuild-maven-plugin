esbuild-maven-plugin
============

[![Build Status](https://img.shields.io/github/actions/workflow/status/mvnpm/esbuild-maven-plugin/maven.yml?label=Build&branch=main)](https://github.com/mvnpm/esbuild-maven-plugin/actions/workflows/maven.yml)
[![usefulness 100%](https://img.shields.io/badge/usefulness-100%25-success.svg?label=Usefulness)](https://www.google.com/search?q=pasta+machine)
[![Maven Central](https://img.shields.io/maven-central/v/io.mvnpm/esbuild-maven-plugin.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.mvnpm/esbuild-maven-plugin)
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)](https://www.apache.org/licenses/LICENSE-2.0)

## Usage

```xml
<plugin>
    <groupId>io.mvnpm</groupId>
    <artifactId>esbuild-maven-plugin</artifactId>
    <version>999-SNAPSHOT</version>
    <executions>
        <execution>
            <id>esbuild</id>
            <goals>
                <goal>esbuild</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <entryPoint>backoffice.js</entryPoint>
    </configuration>
    <dependencies>
        <!-- add web dependencies here -->
        <dependency>
            <groupId>org.mvnpm</groupId>
            <artifactId>bootstrap</artifactId>
            <version>5.3.3</version>
        </dependency>
        <dependency>
            <groupId>org.mvnpm.at.popperjs</groupId>
            <artifactId>core</artifactId>
            <version>2.11.8</version>
        </dependency>
        <dependency>
            <groupId>org.mvnpm</groupId>
            <artifactId>bootstrap-icons</artifactId>
            <version>1.11.3</version>
        </dependency>
    </dependencies>
</plugin>

```
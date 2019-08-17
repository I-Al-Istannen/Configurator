#!/bin/env bash

cd target

native-image -jar Configurator.jar --no-fallback --static --language:js Configurator_scripts
native-image -jar Configurator.jar --no-fallback --static Configurator_no_scripts

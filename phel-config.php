<?php
// Reference: https://phel-lang.org/documentation/configuration/
return Phel\Config\PhelConfig::forProject()
    ->withSrcDirs([])
    ->withTestDirs(['test'])
    ->withFormatDirs(['test'])
    ->withWarnDeprecations();
